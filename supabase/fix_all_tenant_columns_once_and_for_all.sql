-- =============================================================================
-- Corrección definitiva de approve_business_application con mapeo explícito de columnas y casting de ENUM
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

create or replace function public.approve_business_application(
    p_business_id uuid
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_business public.businesses;
    v_tenant public.tenants;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;

    select * into v_business from public.businesses
    where id = p_business_id for update;

    if not found then raise exception 'business application not found'; end if;
    if v_business.status <> 'PENDING' then raise exception 'business application is not pending'; end if;
    if v_business.submitted_by is null then raise exception 'application has no authenticated owner'; end if;

    -- Inserción explícita nombrando cada columna para evitar desajustes de orden
    insert into public.tenants (
        name,
        nombre,
        domicilio,
        telefono,
        email,
        phone,
        address,
        status,
        created_by,
        owner_id
    )
    values (
        coalesce(v_business.public_name, 'Clinica'),
        coalesce(v_business.public_name, 'Clinica'),
        coalesce(v_business.address, ''),
        coalesce(v_business.phone, ''),
        coalesce(v_business.email, ''),
        coalesce(v_business.phone, ''),
        coalesce(v_business.address, ''),
        'ACTIVE'::public.tenant_status,
        v_business.submitted_by,
        v_business.submitted_by
    )
    returning * into v_tenant;

    update public.businesses
    set tenant_id = v_tenant.id,
        status = 'APPROVED',
        approved_by = v_actor_id,
        approved_at = now(),
        updated_at = now()
    where id = p_business_id returning * into v_business;

    update public.profiles
    set role = 'VETERINARY_BUSINESS', tenant_id = v_tenant.id, updated_at = now()
    where id = v_business.submitted_by;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_actor_id, v_tenant.id, 'APPROVE_BUSINESS', 'business', p_business_id);

    return v_business;
end;
$$;

revoke all on function public.approve_business_application(uuid) from public, anon;
grant execute on function public.approve_business_application(uuid) to authenticated;
