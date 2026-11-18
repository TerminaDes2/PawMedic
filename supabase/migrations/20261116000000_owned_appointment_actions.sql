-- Customer appointment actions. Both operations are intentionally restricted
-- to appointments that are still awaiting confirmation.

create or replace function public.guard_appointment_updates()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() = old.booked_by or auth.uid() = old.owner_id then
        if old.status <> 'REQUESTED' then
            raise exception 'only pending appointments can be edited or cancelled';
        end if;
        if new.tenant_id is distinct from old.tenant_id
           or new.pet_id is distinct from old.pet_id
           or new.service_id is distinct from old.service_id
           or new.booked_by is distinct from old.booked_by
           or new.owner_id is distinct from old.owner_id
           or new.idempotency_key is distinct from old.idempotency_key
           or new.status not in ('REQUESTED', 'CANCELLED') then
            raise exception 'invalid customer appointment update';
        end if;
        return new;
    end if;
    if not public.is_superadmin() and not public.is_tenant_member(old.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if new.tenant_id is distinct from old.tenant_id
       or new.pet_id is distinct from old.pet_id
       or new.service_id is distinct from old.service_id
       or new.booked_by is distinct from old.booked_by
       or new.owner_id is distinct from old.owner_id
       or new.starts_at is distinct from old.starts_at
       or new.ends_at is distinct from old.ends_at
       or new.idempotency_key is distinct from old.idempotency_key
       or not (
           (old.status = 'REQUESTED' and new.status in ('CONFIRMED', 'CANCELLED'))
           or (old.status = 'CONFIRMED' and new.status in ('COMPLETED', 'NO_SHOW'))
       ) then
        raise exception 'unsupported appointment status transition';
    end if;
    return new;
end;
$$;

create or replace function public.cancel_owned_appointment(p_appointment_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then raise exception 'authentication required'; end if;
    update public.appointments
    set status = 'CANCELLED', updated_at = now()
    where id = p_appointment_id
      and (booked_by = v_user_id or owner_id = v_user_id)
      and status = 'REQUESTED';
    if not found then
        raise exception 'only pending appointments owned by the customer can be cancelled';
    end if;
end;
$$;

create or replace function public.update_owned_appointment(
    p_appointment_id uuid,
    p_date date,
    p_time time
) returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_appointment public.appointments;
    v_duration integer;
    v_time_zone text;
    v_starts_at timestamptz;
    v_ends_at timestamptz;
begin
    if v_user_id is null then raise exception 'authentication required'; end if;
    select a, s.duration_minutes, t.time_zone
    into v_appointment, v_duration, v_time_zone
    from public.appointments a
    join public.services s on s.id = a.service_id
    join public.tenants t on t.id = a.tenant_id
    where a.id = p_appointment_id
      and (a.booked_by = v_user_id or a.owner_id = v_user_id)
      and a.status = 'REQUESTED'
    for update;
    if not found then
        raise exception 'only pending appointments owned by the customer can be edited';
    end if;
    v_starts_at := (p_date + p_time) at time zone v_time_zone;
    v_ends_at := v_starts_at + make_interval(mins => v_duration);
    if v_starts_at <= now() then raise exception 'appointment must start in the future'; end if;
    if not exists (
        select 1 from public.get_available_slots(v_appointment.tenant_id, v_appointment.service_id, p_date)
        where starts_at = v_starts_at and ends_at = v_ends_at
    ) then
        raise exception 'requested time is no longer available';
    end if;
    update public.appointments
    set starts_at = v_starts_at, ends_at = v_ends_at, updated_at = now()
    where id = p_appointment_id;
end;
$$;

revoke all on function public.cancel_owned_appointment(uuid) from public, anon;
grant execute on function public.cancel_owned_appointment(uuid) to authenticated;
revoke all on function public.update_owned_appointment(uuid, date, time) from public, anon;
grant execute on function public.update_owned_appointment(uuid, date, time) to authenticated;
