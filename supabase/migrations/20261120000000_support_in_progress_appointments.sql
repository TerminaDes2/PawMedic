-- Migration: Support IN_PROGRESS appointment status transitions and grant table privileges
-- Allows CONFIRMED -> IN_PROGRESS and IN_PROGRESS -> COMPLETED (as well as cancellation/rejection).

create or replace function public.transition_appointment_status(
    p_appointment_id uuid,
    p_expected_status public.appointment_status,
    p_next_status public.appointment_status
) returns public.appointments
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_appointment public.appointments;
    v_allowed boolean;
begin
    if v_actor_id is null then
        raise exception 'authentication required';
    end if;

    select *
    into v_appointment
    from public.appointments
    where id = p_appointment_id
    for update;
    if not found then
        raise exception 'appointment not found';
    end if;
    if not public.is_tenant_member(v_appointment.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if v_appointment.status <> p_expected_status then
        raise exception 'appointment status changed; reload before retrying';
    end if;

    v_allowed :=
        (p_expected_status = 'REQUESTED' and p_next_status in ('CONFIRMED', 'CANCELLED', 'REJECTED'))
        or (p_expected_status = 'CONFIRMED' and p_next_status in ('IN_PROGRESS', 'COMPLETED', 'NO_SHOW', 'CANCELLED'))
        or (p_expected_status = 'IN_PROGRESS' and p_next_status in ('COMPLETED', 'CANCELLED', 'NO_SHOW'));

    if not v_allowed then
        raise exception 'unsupported appointment status transition';
    end if;

    update public.appointments
    set status = p_next_status, updated_at = now()
    where id = p_appointment_id
    returning * into v_appointment;

    insert into public.audit_logs (
        actor_id, tenant_id, action, entity_type, entity_id, metadata
    ) values (
        v_actor_id,
        v_appointment.tenant_id,
        'CHANGE_APPOINTMENT_STATUS',
        'appointment',
        p_appointment_id,
        jsonb_build_object('status', p_next_status)
    );
    return v_appointment;
end;
$$;

create or replace function public.guard_appointment_updates()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() = old.booked_by then
        raise exception 'customers cannot edit or cancel appointments in this MVP';
    end if;
    if not public.is_superadmin() and not public.is_tenant_member(old.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if new.tenant_id is distinct from old.tenant_id
       or new.pet_id is distinct from old.pet_id
       or new.service_id is distinct from old.service_id
       or new.booked_by is distinct from old.booked_by
       or new.starts_at is distinct from old.starts_at
       or new.ends_at is distinct from old.ends_at
       or new.idempotency_key is distinct from old.idempotency_key then
        raise exception 'appointment details cannot be changed after booking';
    end if;
    if not (
        (old.status = 'REQUESTED' and new.status in ('CONFIRMED', 'CANCELLED', 'REJECTED'))
        or (old.status = 'CONFIRMED' and new.status in ('IN_PROGRESS', 'COMPLETED', 'NO_SHOW', 'CANCELLED'))
        or (old.status = 'IN_PROGRESS' and new.status in ('COMPLETED', 'CANCELLED', 'NO_SHOW'))
    ) then
        raise exception 'unsupported appointment status transition';
    end if;
    return new;
end;
$$;

drop trigger if exists appointments_guard_updates on public.appointments;
create trigger appointments_guard_updates
before update on public.appointments
for each row execute function public.guard_appointment_updates();

grant select, insert, update on public.appointments to authenticated;
revoke all on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) from public, anon;
grant execute on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) to authenticated;
