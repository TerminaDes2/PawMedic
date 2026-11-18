ALTER TABLE public.appointments
    ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();

-- Appointment-aware availability overload. The existing appointment is excluded.
create or replace function public.get_available_slots(
    p_tenant_id uuid,
    p_service_id uuid,
    p_date date,
    p_excluded_appointment_id uuid
) returns table(starts_at timestamptz, ends_at timestamptz, local_time text)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_duration integer;
    v_time_zone text;
begin
    if auth.uid() is null or not exists (
        select 1
        from public.profiles
        where id = auth.uid() and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;

    if p_excluded_appointment_id is not null and not exists (
        select 1
        from public.appointments appointment
        where appointment.id = p_excluded_appointment_id
          and appointment.tenant_id = p_tenant_id
          and appointment.service_id = p_service_id
          and appointment.status = 'REQUESTED'
          and (
              appointment.owner_id = auth.uid()
              or (appointment.owner_id is null and appointment.booked_by = auth.uid())
          )
    ) then
        raise exception 'excluded appointment is not owned, in the requested tenant/service, or pending';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id
      and service.tenant_id = p_tenant_id
      and service.active
      and tenant.status = 'ACTIVE';

    if not found then
        raise exception 'active service or clinic not found';
    end if;

    if v_duration is null or v_duration <= 0 then
        raise exception 'service duration must be positive';
    end if;

    if p_date is null or p_date < (now() at time zone v_time_zone)::date then
        return;
    end if;

    return query
    select distinct slot.starts_at,
           slot.starts_at + make_interval(mins => v_duration),
           to_char(slot.starts_at at time zone v_time_zone, 'HH24:MI')
    from public.business_schedules schedule
    cross join lateral generate_series(
        (p_date + schedule.open_time) at time zone v_time_zone,
        ((p_date + schedule.close_time) at time zone v_time_zone)
            - make_interval(mins => v_duration),
        make_interval(mins => v_duration)
    ) as slot(starts_at)
    where schedule.tenant_id = p_tenant_id
      and schedule.day_of_week = extract(dow from p_date)::smallint
      and schedule.is_active
      and slot.starts_at > now()
      and not exists (
          select 1
          from public.availability_blocks block
          where block.tenant_id = p_tenant_id
            and block.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and block.ends_at > slot.starts_at
      )
      and not exists (
          select 1
          from public.appointments appointment
          where appointment.tenant_id = p_tenant_id
            and appointment.status in ('REQUESTED', 'CONFIRMED')
            and appointment.id is distinct from p_excluded_appointment_id
            and appointment.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and appointment.ends_at > slot.starts_at
      )
    order by slot.starts_at;
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
    if v_user_id is null then
        raise exception 'authentication required';
    end if;

    if not exists (
        select 1
        from public.profiles
        where id = v_user_id and role = 'USER'
    ) then
        raise exception 'customer account required';
    end if;

    if p_appointment_id is null or p_date is null or p_time is null then
        raise exception 'appointment, date and time are required';
    end if;

    perform pg_advisory_xact_lock(
        hashtextextended('owned-appointment:' || p_appointment_id::text, 0)
    );

    select appointment.*
    into v_appointment
    from public.appointments appointment
    join public.pets pet on pet.id = appointment.pet_id
    join public.services service on service.id = appointment.service_id
    join public.tenants tenant on tenant.id = appointment.tenant_id
    where appointment.id = p_appointment_id
      and pet.owner_id = v_user_id
      and (
          appointment.owner_id = v_user_id
          or (appointment.owner_id is null and appointment.booked_by = v_user_id)
      )
      and appointment.status = 'REQUESTED'
      and service.tenant_id = appointment.tenant_id
      and service.active
      and tenant.status = 'ACTIVE'
    for update of appointment;

    if not found then
        raise exception 'only pending appointments owned by the customer can be edited';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = v_appointment.service_id
      and service.tenant_id = v_appointment.tenant_id
      and service.active
      and tenant.status = 'ACTIVE';

    if not found or v_duration is null or v_duration <= 0 then
        raise exception 'service duration or clinic time zone is invalid';
    end if;

    v_starts_at := (p_date + p_time) at time zone v_time_zone;
    v_ends_at := v_starts_at + make_interval(mins => v_duration);

    if v_starts_at <= now() then
        raise exception 'appointment must start in the future';
    end if;

    perform pg_advisory_xact_lock(
        hashtextextended(
            v_appointment.tenant_id::text || ':' ||
            (v_starts_at at time zone v_time_zone)::date::text,
            0
        )
    );

    if not exists (
        select 1
        from public.get_available_slots(
            v_appointment.tenant_id,
            v_appointment.service_id,
            p_date,
            p_appointment_id
        ) slot
        where slot.starts_at = v_starts_at
          and slot.ends_at = v_ends_at
    ) then
        raise exception 'requested time is no longer available';
    end if;

    update public.appointments
    set starts_at = v_starts_at,
        ends_at = v_ends_at,
        updated_at = now()
    where id = p_appointment_id;

    insert into public.audit_logs (
        actor_id, tenant_id, action, entity_type, entity_id, metadata
    ) values (
        v_user_id,
        v_appointment.tenant_id,
        'RESCHEDULE_APPOINTMENT',
        'appointment',
        p_appointment_id,
        jsonb_build_object(
            'previous_starts_at', v_appointment.starts_at,
            'previous_ends_at', v_appointment.ends_at,
            'new_starts_at', v_starts_at,
            'new_ends_at', v_ends_at
        )
    );
end;
$$;

revoke all on function public.update_owned_appointment(uuid, date, time) from public, anon;
grant execute on function public.update_owned_appointment(uuid, date, time) to authenticated;

revoke all on function public.get_available_slots(uuid, uuid, date, uuid) from public, anon;
grant execute on function public.get_available_slots(uuid, uuid, date, uuid) to authenticated;
