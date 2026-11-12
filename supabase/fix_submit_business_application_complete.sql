-- Apply in the Supabase SQL editor after the English-column migration.
-- All changes below are transactional. A failed statement rolls back the repair.
begin;

alter table public.tenants
    add column if not exists owner_id uuid references public.profiles(id),
    add column if not exists address text not null default '',
    add column if not exists phone text not null default '',
    add column if not exists email text not null default '',
    add column if not exists operational_status text not null default 'ACTIVE';

create table if not exists public.business_applications (
    id uuid primary key default gen_random_uuid(),
    applicant_id uuid not null references public.profiles(id),
    business_name text not null,
    address text not null,
    city text not null,
    tax_id text not null default '',
    contact_phone text not null default '',
    veterinarian_name text not null default '',
    professional_license text not null default '',
    contact_email text not null default '',
    status text not null default 'PENDING',
    created_at timestamptz not null default now()
);

create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN');
$$;

create or replace function public.has_verified_email()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (select 1 from auth.users where id = auth.uid() and email_confirmed_at is not null);
$$;
revoke all on function public.has_verified_email() from public, anon;
grant execute on function public.has_verified_email() to authenticated, service_role;

-- Run after the existing profile-creation trigger, preserving its metadata handling.
create or replace function public.assign_desktop_veterinary_role()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    if new.raw_user_meta_data ->> 'role' = 'VETERINARY_BUSINESS' then
        update public.profiles set role = 'VETERINARY_BUSINESS' where id = new.id and role = 'USER';
        if not found then raise exception 'desktop veterinary profile was not created'; end if;
    end if;
    return new;
end;
$$;
drop trigger if exists zz_assign_desktop_veterinary_role on auth.users;
create trigger zz_assign_desktop_veterinary_role after insert on auth.users
for each row execute function public.assign_desktop_veterinary_role();

-- Permit only the applicant's verified transition and recovery of an already
-- approved tenant. Other privilege changes still require a superadministrator.
create or replace function public.guard_profile_privileges()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    if (new.role is distinct from old.role or new.tenant_id is distinct from old.tenant_id)
       and auth.uid() is not null
       and not exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'SUPERADMIN') then
        if old.id <> auth.uid()
           or (new.role is distinct from old.role and not (
               old.role = 'USER' and new.role = 'VETERINARY_BUSINESS'
               and exists (select 1 from auth.users u where u.id = old.id and u.email_confirmed_at is not null)
               and (exists (select 1 from public.business_applications a where a.applicant_id = old.id and a.status = 'PENDING')
                    or exists (select 1 from public.businesses b where b.submitted_by = old.id and b.status = 'PENDING'))
           ))
           or (new.tenant_id is distinct from old.tenant_id and not (
               old.tenant_id is null and new.role = 'VETERINARY_BUSINESS'
               and exists (
                   select 1 from public.businesses b join public.tenants t on t.id = b.tenant_id
                   where b.submitted_by = old.id and b.status = 'APPROVED'
                     and b.tenant_id = new.tenant_id and t.status = 'ACTIVE'
               )
           )) then
            raise exception 'profile role and tenant can only be changed by an administrator';
        end if;
    end if;
    return new;
end;
$$;
drop trigger if exists profiles_guard_privileges on public.profiles;
create trigger profiles_guard_privileges before update on public.profiles
for each row execute function public.guard_profile_privileges();

-- Remove legacy overloads, including functions with incompatible return types.
drop function if exists public.submit_business_application(text, text, text, text, text, text);
drop function if exists public.submit_business_application(text, text, text, text, text, text, text, text, text, text, text);
drop function if exists public.get_all_business_applications();
drop function if exists public.approve_business_application(uuid);
drop function if exists public.approve_business_application(uuid, uuid, uuid);
drop function if exists public.reject_business_application(uuid, text);
drop function if exists public.reject_business_application(uuid, uuid, uuid, text);

create function public.submit_business_application(
    p_applicant_name text default '',
    p_business_name text default '',
    p_tax_id text default '',
    p_phone text default '',
    p_address text default '',
    p_license_number text default '',
    p_veterinarian_name text default '',
    p_contact_phone text default '',
    p_professional_license text default '',
    p_contact_email text default '',
    p_city text default 'Mexico City'
) returns jsonb language plpgsql security definer set search_path = public as $$
declare
    v_user uuid := auth.uid();
    v_id uuid := gen_random_uuid();
    v_name text := coalesce(nullif(trim(p_veterinarian_name), ''), nullif(trim(p_applicant_name), ''));
    v_business text := nullif(trim(p_business_name), '');
    v_phone text := coalesce(nullif(trim(p_contact_phone), ''), nullif(trim(p_phone), ''));
    v_license text := coalesce(nullif(trim(p_professional_license), ''), nullif(trim(p_license_number), ''));
    v_email text;
    v_role public.profile_role;
begin
    if v_user is null then raise exception 'authentication required'; end if;
    select email into v_email from auth.users where id = v_user and email_confirmed_at is not null;
    if not found then raise exception 'verify the account email before submitting a business application'; end if;
    select role into v_role from public.profiles where id = v_user for update;
    if not found or v_role not in ('USER', 'VETERINARY_BUSINESS') then
        raise exception 'this account cannot submit a veterinary application';
    end if;
    if v_name is null or v_business is null or nullif(trim(p_tax_id), '') is null
       or v_phone is null or nullif(trim(p_address), '') is null or v_license is null
       or nullif(trim(p_city), '') is null then
        raise exception 'complete all required business application fields';
    end if;
    if exists (select 1 from public.business_applications where applicant_id = v_user and status in ('PENDING', 'APPROVED'))
       or exists (select 1 from public.businesses where submitted_by = v_user and status in ('PENDING', 'APPROVED', 'SUSPENDED')) then
        raise exception 'an active business application already exists for this account';
    end if;
    insert into public.business_applications (
        id, applicant_id, business_name, address, city, tax_id, contact_phone,
        veterinarian_name, professional_license, contact_email, status
    ) values (
        v_id, v_user, v_business, trim(p_address), trim(p_city), trim(p_tax_id), v_phone,
        v_name, v_license, coalesce(nullif(trim(p_contact_email), ''), v_email), 'PENDING'
    );
    insert into public.businesses (
        id, submitted_by, applicant_name, legal_name, public_name, tax_id, email, phone, address, license_number, status
    ) values (
        v_id, v_user, v_name, v_business, v_business, trim(p_tax_id),
        coalesce(nullif(trim(p_contact_email), ''), v_email), v_phone, trim(p_address), v_license, 'PENDING'
    );
    update public.profiles set role = 'VETERINARY_BUSINESS' where id = v_user;
    insert into public.audit_logs (actor_id, action, entity_type, entity_id)
    values (v_user, 'SUBMIT_BUSINESS_APPLICATION', 'business', v_id);
    return jsonb_build_object('id', v_id, 'status', 'PENDING');
end;
$$;

create function public.get_all_business_applications()
returns jsonb language plpgsql security definer set search_path = public as $$
declare v_result jsonb;
begin
    if not (coalesce(auth.role(), '') = 'service_role' or exists (
        select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN'
    )) then raise exception 'superadministrator access required'; end if;
    select coalesce(jsonb_agg(item order by created_at desc), '[]'::jsonb) into v_result
    from (
        select to_jsonb(a) || case when b.id is null then '{}'::jsonb else
            jsonb_build_object(
                'status', case when a.status = 'PENDING' then 'PENDING' else b.status::text end,
                'tenant_id', b.tenant_id, 'decision_reason', b.decision_reason
            ) end as item, a.created_at
        from public.business_applications a left join public.businesses b on b.id = a.id
        union all
        select to_jsonb(b), b.created_at from public.businesses b
        where not exists (select 1 from public.business_applications a where a.id = b.id)
    ) applications;
    return v_result;
end;
$$;

create function public.approve_business_application(
    p_business_id uuid default null,
    p_application_id uuid default null,
    p_id uuid default null
) returns jsonb language plpgsql security definer set search_path = public as $$
declare
    v_id uuid := coalesce(p_business_id, p_application_id, p_id);
    v_app public.business_applications%rowtype;
    v_business public.businesses%rowtype;
    v_owner uuid;
    v_tenant uuid;
    v_has_app boolean;
    v_has_business boolean;
begin
    if not (coalesce(auth.role(), '') = 'service_role' or exists (
        select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN'
    )) then raise exception 'superadministrator access required'; end if;
    if v_id is null or (p_business_id is not null and p_business_id <> v_id)
       or (p_application_id is not null and p_application_id <> v_id)
       or (p_id is not null and p_id <> v_id) then raise exception 'invalid application ID'; end if;
    select * into v_app from public.business_applications where id = v_id for update;
    v_has_app := found;
    select * into v_business from public.businesses where id = v_id for update;
    v_has_business := found;
    if not v_has_app and not v_has_business then raise exception 'business application not found'; end if;
    if (v_has_app and v_app.status <> 'PENDING') or (v_has_business and v_business.status <> 'PENDING') then
        raise exception 'business application is not pending';
    end if;
    v_owner := coalesce(v_app.applicant_id, v_business.submitted_by);
    if v_owner is null or (v_has_app and v_has_business and v_app.applicant_id is distinct from v_business.submitted_by) then
        raise exception 'application owner is missing or inconsistent';
    end if;
    perform 1 from public.profiles where id = v_owner for update;
    if not found then raise exception 'applicant profile not found'; end if;
    if not v_has_app then
        insert into public.business_applications (id, applicant_id, business_name, address, city, tax_id, contact_phone, veterinarian_name, professional_license, contact_email, status)
        values (v_id, v_owner, coalesce(nullif(v_business.public_name, ''), v_business.legal_name), v_business.address,
            'Mexico City', v_business.tax_id, v_business.phone, v_business.applicant_name, v_business.license_number, v_business.email, 'PENDING')
        returning * into v_app;
    end if;
    if not v_has_business then
        insert into public.businesses (id, submitted_by, legal_name, public_name, applicant_name, tax_id, email, phone, address, license_number, status)
        values (v_id, v_owner, v_app.business_name, v_app.business_name, v_app.veterinarian_name, v_app.tax_id,
            v_app.contact_email, v_app.contact_phone, v_app.address, v_app.professional_license, 'PENDING')
        returning * into v_business;
    end if;
    v_tenant := v_business.tenant_id;
    if v_tenant is null then
        select id into v_tenant from public.tenants where owner_id = v_owner order by created_at desc limit 1 for update;
    end if;
    if v_tenant is null then
        insert into public.tenants (owner_id, created_by, name, address, phone, email, status, operational_status)
        values (v_owner, v_owner, v_app.business_name, v_app.address, v_app.contact_phone, v_app.contact_email, 'ACTIVE', 'ACTIVE')
        returning id into v_tenant;
    else
        update public.tenants set owner_id = v_owner, name = v_app.business_name, address = v_app.address,
            phone = v_app.contact_phone, email = v_app.contact_email, status = 'ACTIVE', operational_status = 'ACTIVE'
        where id = v_tenant and (owner_id is null or owner_id = v_owner);
        if not found then raise exception 'tenant belongs to another owner'; end if;
    end if;
    update public.business_applications set status = 'APPROVED' where id = v_id;
    update public.businesses set tenant_id = v_tenant, status = 'APPROVED', approved_by = auth.uid(),
        approved_at = now(), updated_at = now(), public_name = v_app.business_name, address = v_app.address,
        phone = v_app.contact_phone, email = v_app.contact_email where id = v_id;
    update public.profiles set role = 'VETERINARY_BUSINESS', tenant_id = v_tenant, updated_at = now() where id = v_owner;
    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (auth.uid(), v_tenant, 'APPROVE_BUSINESS', 'business', v_id);
    return jsonb_build_object('id', v_id, 'tenant_id', v_tenant, 'status', 'APPROVED');
end;
$$;

create function public.reject_business_application(
    p_business_id uuid default null,
    p_application_id uuid default null,
    p_id uuid default null,
    p_reason text default ''
) returns jsonb language plpgsql security definer set search_path = public as $$
declare
    v_id uuid := coalesce(p_business_id, p_application_id, p_id);
    v_app public.business_applications%rowtype;
    v_business public.businesses%rowtype;
    v_has_app boolean;
    v_has_business boolean;
begin
    if not (coalesce(auth.role(), '') = 'service_role' or exists (
        select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN'
    )) then raise exception 'superadministrator access required'; end if;
    if v_id is null or (p_business_id is not null and p_business_id <> v_id)
       or (p_application_id is not null and p_application_id <> v_id)
       or (p_id is not null and p_id <> v_id) then raise exception 'invalid application ID'; end if;
    select * into v_app from public.business_applications where id = v_id for update;
    v_has_app := found;
    select * into v_business from public.businesses where id = v_id for update;
    v_has_business := found;
    if not v_has_app and not v_has_business then raise exception 'business application not found'; end if;
    if (v_has_app and v_app.status <> 'PENDING') or (v_has_business and v_business.status <> 'PENDING') then
        raise exception 'business application is not pending';
    end if;
    if (v_has_app and v_app.applicant_id is null) or (v_has_business and v_business.submitted_by is null)
       or (v_has_app and v_has_business and v_app.applicant_id is distinct from v_business.submitted_by) then
        raise exception 'application owner is missing or inconsistent';
    end if;
    if not v_has_app then
        insert into public.business_applications (id, applicant_id, business_name, address, city, tax_id, contact_phone, veterinarian_name, professional_license, contact_email, status)
        values (v_id, v_business.submitted_by, coalesce(nullif(v_business.public_name, ''), v_business.legal_name),
            v_business.address, 'Mexico City', v_business.tax_id, v_business.phone, v_business.applicant_name,
            v_business.license_number, v_business.email, 'PENDING');
    end if;
    if not v_has_business then
        insert into public.businesses (id, submitted_by, legal_name, public_name, applicant_name, tax_id, email, phone, address, license_number, status)
        values (v_id, v_app.applicant_id, v_app.business_name, v_app.business_name, v_app.veterinarian_name, v_app.tax_id,
            v_app.contact_email, v_app.contact_phone, v_app.address, v_app.professional_license, 'PENDING');
    end if;
    update public.business_applications set status = 'REJECTED' where id = v_id and status = 'PENDING';
    update public.businesses set status = 'REJECTED', decision_reason = nullif(trim(p_reason), ''),
        approved_by = auth.uid(), approved_at = now(), updated_at = now() where id = v_id and status = 'PENDING';
    insert into public.audit_logs (actor_id, action, entity_type, entity_id, metadata)
    values (auth.uid(), 'REJECT_BUSINESS', 'business', v_id, jsonb_build_object('reason', nullif(trim(p_reason), '')));
    return jsonb_build_object('id', v_id, 'status', 'REJECTED');
end;
$$;

create or replace function public.link_approved_tenant(p_tenant_id uuid)
returns uuid language plpgsql security definer set search_path = public as $$
declare v_user uuid := auth.uid(); v_business_id uuid;
begin
    if v_user is null then raise exception 'authentication required'; end if;
    perform 1 from public.profiles
        where id = v_user and role = 'VETERINARY_BUSINESS'
          and (tenant_id is null or tenant_id = p_tenant_id) for update;
    if not found then raise exception 'profile cannot be linked to this tenant'; end if;
    if not exists (select 1 from public.tenants where id = p_tenant_id and status = 'ACTIVE'
        and (owner_id = v_user or exists (
            select 1 from public.businesses b where b.submitted_by = v_user
                and b.tenant_id = p_tenant_id and b.status = 'APPROVED'
        ))) then raise exception 'active tenant not found for this account'; end if;
    select id into v_business_id from public.businesses
        where submitted_by = v_user and status = 'APPROVED'
          and (tenant_id is null or tenant_id = p_tenant_id)
        order by created_at desc limit 1 for update;
    if not found then raise exception 'approved business not found for this tenant'; end if;
    update public.businesses set tenant_id = p_tenant_id, updated_at = now() where id = v_business_id;
    update public.profiles set tenant_id = p_tenant_id, updated_at = now() where id = v_user;
    return p_tenant_id;
end;
$$;
revoke all on function public.link_approved_tenant(uuid) from public, anon;
grant execute on function public.link_approved_tenant(uuid) to authenticated, service_role;

revoke all on function public.submit_business_application(text,text,text,text,text,text,text,text,text,text,text) from public;
revoke all on function public.get_all_business_applications() from public;
revoke all on function public.approve_business_application(uuid,uuid,uuid) from public;
revoke all on function public.reject_business_application(uuid,uuid,uuid,text) from public;
grant execute on function public.submit_business_application(text,text,text,text,text,text,text,text,text,text,text) to authenticated, service_role, anon;
grant execute on function public.get_all_business_applications() to authenticated, service_role, anon;
grant execute on function public.approve_business_application(uuid,uuid,uuid) to authenticated, service_role, anon;
grant execute on function public.reject_business_application(uuid,uuid,uuid,text) to authenticated, service_role, anon;

alter table public.business_applications enable row level security;
alter table public.businesses enable row level security;
alter table public.tenants enable row level security;
drop policy if exists tenants_owner_recovery on public.tenants;
create policy tenants_owner_recovery on public.tenants for select to authenticated
    using (owner_id = auth.uid());
drop policy if exists business_applications_insert_own on public.business_applications;
drop policy if exists business_applications_select_policy on public.business_applications;
drop policy if exists business_applications_update_policy on public.business_applications;
create policy business_applications_insert_own on public.business_applications for insert to authenticated
    with check (applicant_id = auth.uid() and status = 'PENDING'
        and public.has_verified_email());
create policy business_applications_select_policy on public.business_applications for select to authenticated
    using (applicant_id = auth.uid() or exists (select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN'));
drop policy if exists businesses_insert_own on public.businesses;
drop policy if exists businesses_select_policy on public.businesses;
drop policy if exists businesses_update_policy on public.businesses;
create policy businesses_insert_own on public.businesses for insert to authenticated
    with check (submitted_by = auth.uid() and status = 'PENDING' and tenant_id is null
        and public.has_verified_email());
create policy businesses_select_policy on public.businesses for select to authenticated
    using (submitted_by = auth.uid() or exists (select 1 from public.profiles where id = auth.uid() and role = 'SUPERADMIN'));

create or replace view public.business_directory with (security_barrier = true) as
select b.tenant_id as id, b.public_name as name, b.address, b.phone
from public.businesses b join public.tenants t on t.id = b.tenant_id
where b.status = 'APPROVED' and t.status = 'ACTIVE';
revoke all on public.business_directory from public, anon;
grant select on public.business_directory to authenticated;

notify pgrst, 'reload schema';
commit;
