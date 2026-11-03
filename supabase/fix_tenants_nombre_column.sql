-- =============================================================================
-- Corrección de la columna "nombre" en la tabla tenants y el RPC approve_business_application
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Asegurar que la columna nombre en tenants sea opcional o tenga valor por defecto
alter table public.tenants
    add column if not exists nombre text not null default '';

do $$ begin
    alter table public.tenants alter column nombre drop not null;
    alter table public.tenants alter column nombre set default '';
exception when others then null; end $$;

-- 2. Actualizar approve_business_application para asignar name y nombre simultáneamente
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

    insert into public.tenants (name, nombre, status, created_by, owner_id)
    values (v_business.public_name, v_business.public_name, 'ACTIVE', v_business.submitted_by, v_business.submitted_by)
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
    if not found then raise exception 'applicant profile not found'; end if;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_actor_id, v_tenant.id, 'APPROVE_BUSINESS', 'business', p_business_id);
    return v_business;
end;
$$;

-- 3. Conceder permisos de ejecución a authenticated
revoke all on function public.approve_business_application(uuid) from public, anon;
grant execute on function public.approve_business_application(uuid) to authenticated;
