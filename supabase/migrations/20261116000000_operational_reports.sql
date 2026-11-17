-- Reports are always scoped to the signed-in veterinary account.
create or replace function public.get_operational_report(
    p_start_date date,
    p_end_date date
)
returns jsonb
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_tenant_id uuid;
    v_zone text;
    v_result jsonb;
begin
    if auth.uid() is null or p_start_date is null or p_end_date is null
       or p_start_date > p_end_date or not isfinite(p_start_date)
       or not isfinite(p_end_date) or p_end_date > date '9998-12-31' then
        raise exception 'valid dates and an authenticated session are required';
    end if;

    select p.tenant_id, t.time_zone into v_tenant_id, v_zone
    from public.profiles p
    join public.tenants t on t.id = p.tenant_id
    where p.id = auth.uid() and p.role = 'VETERINARY_BUSINESS'
      and t.status = 'ACTIVE';

    if v_tenant_id is null or not public.is_tenant_member(v_tenant_id) then
        raise exception 'active veterinary tenant membership required';
    end if;

    select jsonb_build_object(
        'total_appointments', count(*),
        'completed_appointments', count(*) filter (where a.status = 'COMPLETED'),
        'appointments', coalesce(jsonb_agg(
            jsonb_build_object(
                'id', a.id,
                'starts_at', a.starts_at,
                'pet_name', pet.name,
                'service_name', s.name
            ) order by a.starts_at
        ) filter (where a.status = 'COMPLETED'), '[]'::jsonb),
        'services', coalesce((
            select jsonb_agg(jsonb_build_object('name', grouped.name, 'total', grouped.total)
                order by grouped.name)
            from (
                select s2.name, count(*) as total
                from public.appointments a2
                join public.services s2 on s2.id = a2.service_id and s2.tenant_id = v_tenant_id
                where a2.tenant_id = v_tenant_id and a2.status = 'COMPLETED'
                  and a2.starts_at >= (p_start_date::timestamp at time zone v_zone)
                  and a2.starts_at < ((p_end_date + 1)::timestamp at time zone v_zone)
                group by s2.id, s2.name
            ) grouped
        ), '[]'::jsonb),
        'time_zone', v_zone
    ) into v_result
    from public.appointments a
    join public.pets pet on pet.id = a.pet_id
    join public.services s on s.id = a.service_id and s.tenant_id = v_tenant_id
    where a.tenant_id = v_tenant_id
      and a.starts_at >= (p_start_date::timestamp at time zone v_zone)
      and a.starts_at < ((p_end_date + 1)::timestamp at time zone v_zone);

    return v_result;
end;
$$;

revoke all on function public.get_operational_report(date, date) from public, anon;
grant execute on function public.get_operational_report(date, date) to authenticated;
