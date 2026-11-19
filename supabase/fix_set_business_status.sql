-- Crea/actualiza el RPC usado por el superadmin para suspender o reactivar veterinarias.
alter table public.tenants add column if not exists updated_at timestamptz not null default now();

create or replace function public.set_business_status(
    p_business_id uuid,
    p_status public.business_status
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_business public.businesses;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;
    if p_status not in ('APPROVED', 'SUSPENDED') then
        raise exception 'unsupported business status transition';
    end if;
    update public.businesses
    set status = p_status, updated_at = now()
    where id = p_business_id and status in ('APPROVED', 'SUSPENDED')
    returning * into v_business;
    if not found then raise exception 'business not found or status cannot be changed'; end if;

    update public.tenants
    set status = case when p_status = 'APPROVED' then 'ACTIVE'::public.tenant_status
                      else 'SUSPENDED'::public.tenant_status end,
        updated_at = now()
    where id = v_business.tenant_id;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id, metadata)
    values (
        v_actor_id, v_business.tenant_id, 'CHANGE_BUSINESS_STATUS', 'business', p_business_id,
        jsonb_build_object('status', p_status)
    );
    return v_business;
end;
$$;

revoke all on function public.set_business_status(uuid, public.business_status) from public, anon;
grant execute on function public.set_business_status(uuid, public.business_status) to authenticated;

notify pgrst, 'reload schema';
