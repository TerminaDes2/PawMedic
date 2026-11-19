-- Corrects appointment-aware availability for schedules that cross midnight.
-- This migration does not alter or replace the existing booking/state/notification functions.
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

    if not found or v_duration is null or v_duration <= 0 then
        raise exception 'active service or clinic not found, or service duration is invalid';
    end if;

    if p_date is null or p_date < (now() at time zone v_time_zone)::date then
        return;
    end if;

    return query
    with candidate_dates as (
        select p_date as schedule_date
        union all
        select p_date - 1
    ), candidate_schedules as (
        select schedule.tenant_id,
               schedule.day_of_week,
               candidate.schedule_date,
               (candidate.schedule_date + schedule.open_time)
                   at time zone v_time_zone as schedule_starts_at,
               (
                   candidate.schedule_date
                   + schedule.close_time
                   + case
                         when schedule.close_time <= schedule.open_time
                         then interval '1 day'
                         else interval '0'
                     end
               ) at time zone v_time_zone as schedule_ends_at
        from public.business_schedules schedule
        cross join candidate_dates candidate
        where schedule.tenant_id = p_tenant_id
          and schedule.is_active
          and schedule.day_of_week = extract(dow from candidate.schedule_date)::smallint
    )
    select distinct slot.starts_at,
           slot.starts_at + make_interval(mins => v_duration),
           to_char(slot.starts_at at time zone v_time_zone, 'HH24:MI')
    from candidate_schedules schedule
    cross join lateral generate_series(
        schedule.schedule_starts_at,
        schedule.schedule_ends_at
            - make_interval(mins => v_duration),
        make_interval(mins => v_duration)
    ) as slot(starts_at)
    where schedule.schedule_ends_at >= schedule.schedule_starts_at
      + make_interval(mins => v_duration)
      and (slot.starts_at at time zone v_time_zone)::date = p_date
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

revoke all on function public.get_available_slots(uuid, uuid, date, uuid) from public, anon;
grant execute on function public.get_available_slots(uuid, uuid, date, uuid) to authenticated;
