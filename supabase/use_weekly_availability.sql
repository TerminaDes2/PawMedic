-- Restore weekly availability while retaining date overrides for reference.
-- Does not remove schedules or appointments.
begin;

create or replace function public.get_available_slots(
    p_tenant_id uuid, p_service_id uuid, p_date date
) returns table(starts_at timestamptz, ends_at timestamptz, local_time text)
language plpgsql stable security definer set search_path = public
as $$
declare
    v_duration integer;
    v_time_zone text;
begin
    if auth.uid() is null or not exists (
        select 1 from public.profiles where id = auth.uid() and role = 'USER'
    ) then raise exception 'authenticated customer access required'; end if;

    select service.duration_minutes, tenant.time_zone into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id and service.tenant_id = p_tenant_id
      and service.active and tenant.status = 'ACTIVE';
    if not found then raise exception 'active service or clinic not found'; end if;
    if v_duration is null or v_duration <= 0 then raise exception 'service duration must be positive'; end if;
    if p_date < (now() at time zone v_time_zone)::date then return; end if;

    return query
    select distinct slot.starts_at,
           slot.starts_at + make_interval(mins => v_duration),
           to_char(slot.starts_at at time zone v_time_zone, 'HH24:MI')
    from public.business_schedules schedule
    cross join lateral generate_series(
        (date_trunc('hour', p_date + schedule.open_time)
            + case when schedule.open_time = date_trunc('hour', p_date + schedule.open_time)::time
                   then interval '0 hours' else interval '1 hour' end) at time zone v_time_zone,
        ((p_date + schedule.close_time) at time zone v_time_zone) - make_interval(mins => v_duration),
        interval '1 hour'
    ) as slot(starts_at)
    where schedule.tenant_id = p_tenant_id
      and schedule.day_of_week = extract(dow from p_date)::smallint
      and schedule.is_active and slot.starts_at > now()
      and not exists (
          select 1 from public.availability_blocks block
          where block.tenant_id = p_tenant_id
            and block.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and block.ends_at > slot.starts_at
      )
      and not exists (
          select 1 from public.appointments appointment
          where appointment.tenant_id = p_tenant_id
            and appointment.status::text in ('REQUESTED', 'CONFIRMED', 'IN_PROGRESS')
            and appointment.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and appointment.ends_at > slot.starts_at
      )
    order by slot.starts_at;
end;
$$;

notify pgrst, 'reload schema';
commit;
