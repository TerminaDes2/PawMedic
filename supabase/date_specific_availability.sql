-- Configure per-date availability without changing existing weekly shifts.
-- Run in Supabase SQL Editor after the booking availability migrations.
begin;

create table if not exists public.business_schedule_overrides (
    tenant_id uuid not null references public.tenants(id) on delete cascade,
    schedule_date date not null,
    hours integer[] not null default '{}',
    primary key (tenant_id, schedule_date),
    constraint business_schedule_override_hours_valid
        check (hours <@ array[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23]
            and array_position(hours, null) is null)
);

alter table public.business_schedule_overrides enable row level security;
drop policy if exists schedule_overrides_clinic_manage on public.business_schedule_overrides;
create policy schedule_overrides_clinic_manage on public.business_schedule_overrides for all to authenticated
using (exists (
    select 1 from public.profiles p where p.id = auth.uid()
      and p.role = 'VETERINARY_BUSINESS' and p.tenant_id = business_schedule_overrides.tenant_id
))
with check (exists (
    select 1 from public.profiles p where p.id = auth.uid()
      and p.role = 'VETERINARY_BUSINESS' and p.tenant_id = business_schedule_overrides.tenant_id
));
grant select, insert, update, delete on public.business_schedule_overrides to authenticated;

create or replace function public.get_available_slots(
    p_tenant_id uuid,
    p_service_id uuid,
    p_date date
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
        select 1 from public.profiles where id = auth.uid() and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id
      and service.tenant_id = p_tenant_id
      and service.active
      and tenant.status = 'ACTIVE';
    if not found then raise exception 'active service or clinic not found'; end if;
    if v_duration is null or v_duration <= 0 then raise exception 'service duration must be positive'; end if;
    if p_date < (now() at time zone v_time_zone)::date then return; end if;

    return query
    with override_row as (
        select o.hours
        from public.business_schedule_overrides o
        where o.tenant_id = p_tenant_id and o.schedule_date = p_date
    ),
    override_hours as (
        select distinct h.hour
        from override_row o cross join lateral unnest(o.hours) as h(hour)
    ),
    grouped_hours as (
        select hour, hour - row_number() over (order by hour) as group_id
        from override_hours
    ),
    ranges as (
        select
            (p_date::timestamp + make_interval(hours => min(hour))) at time zone v_time_zone as opens_at,
            (p_date::timestamp + make_interval(hours => max(hour) + 1)) at time zone v_time_zone as closes_at
        from grouped_hours
        group by group_id
        union all
        select
            (p_date + schedule.open_time) at time zone v_time_zone,
            (p_date + schedule.close_time) at time zone v_time_zone
        from public.business_schedules schedule
        where schedule.tenant_id = p_tenant_id
          and schedule.day_of_week = extract(dow from p_date)::smallint
          and schedule.is_active
          and not exists (select 1 from override_row)
    )
    select distinct slot.starts_at,
           slot.starts_at + make_interval(mins => v_duration),
           to_char(slot.starts_at at time zone v_time_zone, 'HH24:MI')
    from ranges r
    cross join lateral generate_series(
        r.opens_at,
        r.closes_at - make_interval(mins => v_duration),
        make_interval(mins => v_duration)
    ) as slot(starts_at)
    where slot.starts_at > now()
      and not exists (
          select 1 from public.availability_blocks block
          where block.tenant_id = p_tenant_id
            and block.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and block.ends_at > slot.starts_at
      )
      and not exists (
          select 1 from public.appointments appointment
          where appointment.tenant_id = p_tenant_id
            and appointment.status in ('REQUESTED', 'CONFIRMED')
            and appointment.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and appointment.ends_at > slot.starts_at
      )
    order by slot.starts_at;
end;
$$;

notify pgrst, 'reload schema';
commit;
