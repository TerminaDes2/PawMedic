-- Development draft: apply only after the device and outbox scripts.
begin;
set local lock_timeout = '3s';
set local statement_timeout = '30s';

do $$
begin
    if to_regclass('public.appointment_notification_deliveries') is not null
       or to_regprocedure('public.prepare_appointment_notification_deliveries(integer)') is not null
       or to_regprocedure('public.claim_appointment_notification_deliveries(integer)') is not null
       or to_regprocedure('public.finish_appointment_notification_delivery(uuid,uuid,text)') is not null then
        raise exception 'notification delivery objects already exist; inspect them before continuing';
    end if;
end;
$$;

create table public.appointment_notification_deliveries (
    id uuid primary key default gen_random_uuid(),
    outbox_id uuid not null references public.appointment_notification_outbox(id) on delete cascade,
    token text not null references public.notification_devices(token) on delete cascade,
    attempts integer not null default 0,
    next_attempt_at timestamptz not null default now(),
    locked_until timestamptz,
    lease_id uuid,
    completed_at timestamptz,
    unique (outbox_id, token)
);

alter table public.appointment_notification_deliveries enable row level security;
revoke all on public.appointment_notification_deliveries from public, anon, authenticated;

create function public.prepare_appointment_notification_deliveries(p_limit integer default 100)
returns integer
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    item public.appointment_notification_outbox;
    prepared integer := 0;
begin
    for item in
        select * from public.appointment_notification_outbox
        where sent_at is null and due_at <= now()
        order by due_at
        limit greatest(1, least(p_limit, 500))
        for update skip locked
    loop
        insert into public.appointment_notification_deliveries (outbox_id, token)
        select item.id, device.token
        from public.notification_devices device
        join public.appointments appointment on appointment.id = item.appointment_id
        join public.pets pet on pet.id = appointment.pet_id
        where device.user_id = item.recipient_id
          and pet.owner_id = item.recipient_id
          and (
              item.kind = 'status'
              or (
                  appointment.status::text in ('REQUESTED', 'CONFIRMED')
                  and appointment.starts_at = item.appointment_start
                  and appointment.starts_at > now()
              )
          )
        on conflict do nothing;
        update public.appointment_notification_outbox set sent_at = now() where id = item.id;
        prepared := prepared + 1;
    end loop;
    return prepared;
end;
$$;

create function public.claim_appointment_notification_deliveries(p_limit integer default 100)
returns table(delivery_id uuid, lease_id uuid, device_token text, message_type text, expires_at timestamptz)
language sql
security definer
set search_path = public, pg_temp
as $$
    with candidates as (
        select delivery.id
        from public.appointment_notification_deliveries delivery
        join public.appointment_notification_outbox item on item.id = delivery.outbox_id
        join public.notification_devices device on device.token = delivery.token
        join public.appointments appointment on appointment.id = item.appointment_id
        join public.pets pet on pet.id = appointment.pet_id
        where delivery.completed_at is null
          and delivery.next_attempt_at <= now()
          and (delivery.locked_until is null or delivery.locked_until < now())
          and device.user_id = item.recipient_id
          and pet.owner_id = item.recipient_id
          and (item.kind = 'status' or (
              appointment.status::text in ('REQUESTED', 'CONFIRMED')
              and appointment.starts_at = item.appointment_start
              and appointment.starts_at > now()
          ))
        order by delivery.next_attempt_at
        limit greatest(1, least(p_limit, 100))
        for update of delivery skip locked
    ), claimed as (
        update public.appointment_notification_deliveries delivery
        set locked_until = now() + interval '5 minutes',
            lease_id = gen_random_uuid(), attempts = delivery.attempts + 1
        from candidates
        where delivery.id = candidates.id
        returning delivery.id, delivery.lease_id, delivery.token, delivery.outbox_id
    )
    select claimed.id, claimed.lease_id, claimed.token,
           case item.kind
               when 'status' then 'appointment_status'
               when 'reminder_24h' then 'appointment_reminder_24h'
               else 'appointment_reminder_1h'
           end,
           case when item.kind = 'status' then null else item.appointment_start end
    from claimed
    join public.appointment_notification_outbox item on item.id = claimed.outbox_id;
$$;

create function public.finish_appointment_notification_delivery(
    p_delivery_id uuid, p_lease_id uuid, p_result text
) returns void
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    device_token text;
begin
    if p_result not in ('sent', 'invalid_token', 'retry', 'permanent_failure') then
        raise exception 'invalid delivery result';
    end if;
    select token into device_token from public.appointment_notification_deliveries
    where id = p_delivery_id and lease_id = p_lease_id and locked_until > now() for update;
    if not found then return; end if;
    if p_result = 'invalid_token' then
        delete from public.notification_devices where token = device_token;
    elsif p_result in ('sent', 'permanent_failure') then
        update public.appointment_notification_deliveries
        set completed_at = now(), locked_until = null, lease_id = null
        where id = p_delivery_id;
    else
        update public.appointment_notification_deliveries
        set next_attempt_at = now() + make_interval(secs => least(3600, 30 * power(2, least(attempts, 7))::integer)),
            locked_until = null, lease_id = null
        where id = p_delivery_id;
    end if;
end;
$$;

revoke all on function public.prepare_appointment_notification_deliveries(integer) from public, anon, authenticated;
revoke all on function public.claim_appointment_notification_deliveries(integer) from public, anon, authenticated;
revoke all on function public.finish_appointment_notification_delivery(uuid, uuid, text) from public, anon, authenticated;
grant execute on function public.prepare_appointment_notification_deliveries(integer) to service_role;
grant execute on function public.claim_appointment_notification_deliveries(integer) to service_role;
grant execute on function public.finish_appointment_notification_delivery(uuid, uuid, text) to service_role;

commit;
