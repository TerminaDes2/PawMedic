-- Development draft: installs a trigger affecting appointment writes.
begin;
set local lock_timeout = '3s';
set local statement_timeout = '30s';

do $$
begin
    if to_regclass('public.appointment_notification_outbox') is not null
       or to_regprocedure('public.queue_appointment_notifications()') is not null
       or exists (
           select 1 from pg_trigger
           where tgrelid = 'public.appointments'::regclass
             and tgname = 'queue_appointment_notifications'
             and not tgisinternal
       ) then
        raise exception 'appointment notification objects already exist; inspect them before continuing';
    end if;
end;
$$;

create table public.appointment_notification_outbox (
    id uuid primary key default gen_random_uuid(),
    appointment_id uuid not null references public.appointments(id) on delete cascade,
    recipient_id uuid not null references auth.users(id) on delete cascade,
    kind text not null check (kind in ('status', 'reminder_24h', 'reminder_1h')),
    appointment_start timestamptz not null,
    status text not null,
    due_at timestamptz not null,
    created_at timestamptz not null default now(),
    sent_at timestamptz
);

alter table public.appointment_notification_outbox enable row level security;
revoke all on public.appointment_notification_outbox from public, anon, authenticated;
create index if not exists appointment_notification_due_idx
    on public.appointment_notification_outbox(due_at)
    where sent_at is null;
create unique index if not exists appointment_notification_reminder_once_idx
    on public.appointment_notification_outbox(appointment_id, kind, appointment_start)
    where kind in ('reminder_24h', 'reminder_1h');

create function public.queue_appointment_notifications()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    recipient uuid;
begin
    select owner_id into recipient from public.pets where id = new.pet_id;
    if recipient is null then
        return new;
    end if;

    if tg_op = 'UPDATE' and (
        old.starts_at is distinct from new.starts_at
        or old.pet_id is distinct from new.pet_id
        or new.status::text not in ('REQUESTED', 'CONFIRMED')
    ) then
        delete from public.appointment_notification_outbox
        where appointment_id = new.id
          and kind in ('reminder_24h', 'reminder_1h')
          and sent_at is null;
    end if;

    if tg_op = 'UPDATE' and old.status is distinct from new.status then
        insert into public.appointment_notification_outbox
            (appointment_id, recipient_id, kind, appointment_start, status, due_at)
        values (new.id, recipient, 'status', new.starts_at, new.status::text, now())
        on conflict do nothing;
    end if;

    if new.status::text in ('REQUESTED', 'CONFIRMED') then
        insert into public.appointment_notification_outbox
            (appointment_id, recipient_id, kind, appointment_start, status, due_at)
        select new.id, recipient, item.kind, new.starts_at, new.status::text, item.due_at
        from (values
            ('reminder_24h', new.starts_at - interval '24 hours'),
            ('reminder_1h', new.starts_at - interval '1 hour')
        ) as item(kind, due_at)
        where item.due_at > now()
        on conflict do nothing;
    end if;
    return new;
end;
$$;

create trigger queue_appointment_notifications
after insert or update of status, starts_at, pet_id on public.appointments
for each row execute function public.queue_appointment_notifications();

insert into public.appointment_notification_outbox
    (appointment_id, recipient_id, kind, appointment_start, status, due_at)
select appointment.id, pet.owner_id, item.kind, appointment.starts_at,
       appointment.status::text, item.due_at
from public.appointments appointment
join public.pets pet on pet.id = appointment.pet_id
cross join lateral (values
    ('reminder_24h', appointment.starts_at - interval '24 hours'),
    ('reminder_1h', appointment.starts_at - interval '1 hour')
) as item(kind, due_at)
where appointment.status::text in ('REQUESTED', 'CONFIRMED')
  and item.due_at > now()
on conflict do nothing;

revoke all on function public.queue_appointment_notifications() from public, anon, authenticated;

commit;
