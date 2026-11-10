-- PawMedic: reparación segura del flujo de registro y aprobación.
--
-- Prerrequisitos:
--   * supabase/migrations/20261008000000_business_registration_workflows.sql
--   * public.is_superadmin()
--   * public.businesses, public.tenants, public.profiles y public.audit_logs
--
-- Este script no se ejecuta desde la aplicación. Revísalo en un entorno de
-- staging y ejecútalo desde Supabase SQL Editor con un usuario administrador.

create or replace function public.submit_business_application(
    p_applicant_name text,
    p_business_name text,
    p_tax_id text,
    p_phone text,
    p_address text,
    p_license_number text
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile public.profiles;
    v_business public.businesses;
begin
    if v_user_id is null then
        raise exception 'authentication required';
    end if;

    if not exists (
        select 1
        from auth.users
        where id = v_user_id
          and email_confirmed_at is not null
    ) then
        raise exception 'verify the account email before submitting a business application';
    end if;

    select * into v_profile
    from public.profiles
    where id = v_user_id
    for update;

    if not found or v_profile.role <> 'USER' then
        raise exception 'this account cannot submit a veterinary application';
    end if;

    if length(trim(coalesce(p_applicant_name, ''))) < 2
       or length(trim(coalesce(p_business_name, ''))) < 2
       or length(trim(coalesce(p_tax_id, ''))) < 3
       or length(trim(coalesce(p_phone, ''))) < 8
       or length(trim(coalesce(p_address, ''))) < 5
       or length(trim(coalesce(p_license_number, ''))) < 2 then
        raise exception 'complete all required business application fields';
    end if;

    if exists (
        select 1
        from public.businesses
        where submitted_by = v_user_id
          and status in ('PENDING', 'APPROVED')
    ) then
        raise exception 'an active business application already exists for this account';
    end if;

    insert into public.businesses (
        submitted_by, applicant_name, legal_name, public_name, tax_id,
        email, phone, address, license_number, status
    ) values (
        v_user_id,
        trim(p_applicant_name),
        trim(p_business_name),
        trim(p_business_name),
        trim(p_tax_id),
        coalesce(auth.jwt() ->> 'email', v_profile.email),
        trim(p_phone),
        trim(p_address),
        trim(p_license_number),
        'PENDING'
    )
    returning * into v_business;

    insert into public.audit_logs (actor_id, action, entity_type, entity_id)
    values (v_user_id, 'SUBMIT_BUSINESS_APPLICATION', 'business', v_business.id);

    return v_business;
end;
$$;

create or replace function public.get_all_business_applications()
returns jsonb
language sql
security definer
set search_path = public
as $$
    select coalesce(jsonb_agg(to_jsonb(b) order by b.created_at desc), '[]'::jsonb)
    from public.businesses b
    where public.is_superadmin();
$$;

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
    v_existing_tenant_id uuid;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;

    select * into v_business
    from public.businesses
    where id = p_business_id
    for update;

    if not found or v_business.status <> 'PENDING' then
        raise exception 'business application is missing or no longer pending';
    end if;
    if v_business.submitted_by is null then
        raise exception 'application has no authenticated owner';
    end if;
    if v_business.tenant_id is not null then
        raise exception 'business application is already linked to a tenant';
    end if;
    select id
    into v_existing_tenant_id
    from public.tenants
    where owner_id = v_business.submitted_by
    limit 1
    for update;
    if v_existing_tenant_id is not null then
        raise exception 'business owner already has a tenant';
    end if;

    insert into public.tenants (
        owner_id,
        name,
        address,
        phone,
        email,
        status,
        operational_status,
        created_by
    ) values (
        v_business.submitted_by,
        v_business.public_name,
        v_business.address,
        v_business.phone,
        v_business.email,
        'ACTIVE',
        'ACTIVE',
        v_business.submitted_by
    )
    returning * into v_tenant;

    update public.businesses
    set tenant_id = v_tenant.id,
        status = 'APPROVED',
        approved_by = v_actor_id,
        approved_at = now(),
        updated_at = now()
    where id = p_business_id
    returning * into v_business;

    update public.profiles
    set role = 'VETERINARY_BUSINESS',
        tenant_id = v_tenant.id,
        updated_at = now()
    where id = v_business.submitted_by;
    if not found then
        raise exception 'applicant profile not found';
    end if;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_actor_id, v_tenant.id, 'APPROVE_BUSINESS', 'business', p_business_id);

    return v_business;
end;
$$;

revoke all on function public.submit_business_application(
    text, text, text, text, text, text
) from public;
revoke all on function public.get_all_business_applications()
from public;
revoke all on function public.approve_business_application(uuid)
from public;

grant execute on function public.submit_business_application(
    text, text, text, text, text, text
) to authenticated;
grant execute on function public.get_all_business_applications()
to authenticated;
grant execute on function public.approve_business_application(uuid)
to authenticated;
revoke execute on function public.submit_business_application(text, text, text, text, text, text)
    from anon;
revoke execute on function public.get_all_business_applications()
    from anon;
revoke execute on function public.approve_business_application(uuid)
    from anon;

notify pgrst, 'reload schema';
