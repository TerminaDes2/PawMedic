-- =============================================================================
-- PawMedic Complete Consolidated Schema Migration
-- Execute this entire file in Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. PROFILES & ENUMS
create extension if not exists "pgcrypto";

do $$ begin
    create type public.profile_role as enum ('USER', 'VETERINARY_BUSINESS', 'SUPERADMIN');
exception
    when duplicate_object then null;
end $$;

create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null default '',
    display_name text not null default '',
    nombre text not null default '',
    apellidos text not null default '',
    num_tel text,
    role public.profile_role not null default 'USER',
    tenant_id uuid,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.profiles
    add column if not exists email text not null default '',
    add column if not exists display_name text not null default '',
    add column if not exists nombre text not null default '',
    add column if not exists apellidos text not null default '',
    add column if not exists num_tel text,
    add column if not exists role public.profile_role not null default 'USER',
    add column if not exists tenant_id uuid;

-- Drop NOT NULL constraints on legacy/additional columns if they exist
do $$ begin
    alter table public.profiles alter column nombre drop not null;
    alter table public.profiles alter column nombre set default '';
exception when others then null; end $$;

do $$ begin
    alter table public.profiles alter column apellidos drop not null;
    alter table public.profiles alter column apellidos set default '';
exception when others then null; end $$;

-- 2. TENANTS
do $$ begin
    create type public.tenant_status as enum ('PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED');
exception
    when duplicate_object then null;
end $$;

create table if not exists public.tenants (
    id uuid primary key default gen_random_uuid(),
    name text not null default '',
    status public.tenant_status not null default 'PENDING',
    created_by uuid references public.profiles(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.tenants
    add column if not exists name text not null default '',
    add column if not exists status public.tenant_status not null default 'PENDING',
    add column if not exists created_by uuid,
    add column if not exists time_zone text not null default 'America/Mexico_City';

alter table public.profiles
    drop constraint if exists profiles_tenant_fk,
    add constraint profiles_tenant_fk foreign key (tenant_id) references public.tenants(id);

create index if not exists profiles_tenant_id_idx on public.profiles(tenant_id);

-- 3. BUSINESSES
do $$ begin
    create type public.business_status as enum ('PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED');
exception
    when duplicate_object then null;
end $$;

create table if not exists public.businesses (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid unique references public.tenants(id) on delete cascade,
    submitted_by uuid references public.profiles(id),
    legal_name text not null default '',
    public_name text not null default '',
    status public.business_status not null default 'PENDING',
    approved_by uuid references public.profiles(id),
    approved_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.businesses
    add column if not exists tenant_id uuid,
    add column if not exists submitted_by uuid,
    add column if not exists legal_name text not null default '',
    add column if not exists public_name text not null default '',
    add column if not exists status public.business_status not null default 'PENDING',
    add column if not exists approved_by uuid,
    add column if not exists approved_at timestamptz,
    add column if not exists applicant_name text not null default '',
    add column if not exists tax_id text not null default '',
    add column if not exists email text not null default '',
    add column if not exists phone text not null default '',
    add column if not exists address text not null default '',
    add column if not exists license_number text not null default '',
    add column if not exists decision_reason text;

create index if not exists businesses_status_idx on public.businesses(status);

-- 4. PETS
create table if not exists public.pets (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid references public.profiles(id) on delete cascade,
    name text not null default '',
    species text not null default '',
    breed text,
    date_of_birth date,
    weight_grams integer,
    notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.pets
    add column if not exists owner_id uuid,
    add column if not exists name text not null default '',
    add column if not exists species text not null default '',
    add column if not exists breed text,
    add column if not exists date_of_birth date,
    add column if not exists weight_grams integer,
    add column if not exists notes text;

create index if not exists pets_owner_id_idx on public.pets(owner_id);

-- 5. SERVICES
create table if not exists public.services (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id) on delete cascade,
    name text not null default '',
    description text not null default '',
    duration_minutes integer not null default 30,
    price_cents integer not null default 0,
    active boolean not null default true,
    created_at timestamptz not null default now()
);

alter table public.services
    add column if not exists tenant_id uuid,
    add column if not exists name text not null default '',
    add column if not exists description text not null default '',
    add column if not exists duration_minutes integer not null default 30,
    add column if not exists price_cents integer not null default 0,
    add column if not exists active boolean not null default true;

create index if not exists services_tenant_id_idx on public.services(tenant_id);

-- 6. PRODUCTS
create table if not exists public.products (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id) on delete cascade,
    sku text not null default '',
    name text not null default '',
    description text not null default '',
    price_cents integer not null default 0,
    stock_quantity integer not null default 0,
    active boolean not null default true,
    created_at timestamptz not null default now()
);

alter table public.products
    add column if not exists tenant_id uuid,
    add column if not exists sku text not null default '',
    add column if not exists name text not null default '',
    add column if not exists description text not null default '',
    add column if not exists price_cents integer not null default 0,
    add column if not exists stock_quantity integer not null default 0,
    add column if not exists active boolean not null default true,
    add column if not exists image_url text not null default '',
    add column if not exists published boolean not null default false;

-- 7. BUSINESS SCHEDULES
create table if not exists public.business_schedules (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id) on delete cascade,
    weekday smallint not null default 0,
    opens_at time not null default '09:00:00',
    closes_at time not null default '18:00:00'
);

alter table public.business_schedules
    add column if not exists tenant_id uuid,
    add column if not exists weekday smallint not null default 0,
    add column if not exists opens_at time not null default '09:00:00',
    add column if not exists closes_at time not null default '18:00:00';

-- 8. AVAILABILITY BLOCKS
create table if not exists public.availability_blocks (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id) on delete cascade,
    starts_at timestamptz not null default now(),
    ends_at timestamptz not null default now(),
    reason text not null default '',
    created_by uuid references public.profiles(id)
);

alter table public.availability_blocks
    add column if not exists tenant_id uuid,
    add column if not exists starts_at timestamptz not null default now(),
    add column if not exists ends_at timestamptz not null default now(),
    add column if not exists reason text not null default '',
    add column if not exists created_by uuid;

create index if not exists availability_blocks_tenant_time_idx
on public.availability_blocks(tenant_id, starts_at, ends_at);

-- 9. APPOINTMENTS
do $$ begin
    create type public.appointment_status as enum ('REQUESTED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW');
exception
    when duplicate_object then null;
end $$;

create table if not exists public.appointments (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id),
    pet_id uuid references public.pets(id),
    service_id uuid references public.services(id),
    booked_by uuid references public.profiles(id),
    starts_at timestamptz not null default now(),
    ends_at timestamptz not null default now(),
    status public.appointment_status not null default 'REQUESTED',
    idempotency_key uuid unique,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.appointments
    add column if not exists tenant_id uuid,
    add column if not exists pet_id uuid,
    add column if not exists service_id uuid,
    add column if not exists booked_by uuid,
    add column if not exists starts_at timestamptz not null default now(),
    add column if not exists ends_at timestamptz not null default now(),
    add column if not exists status public.appointment_status not null default 'REQUESTED',
    add column if not exists idempotency_key uuid;

create index if not exists appointments_tenant_time_idx on public.appointments(tenant_id, starts_at);

-- 10. MEDICAL RECORDS
create table if not exists public.medical_records (
    id uuid primary key default gen_random_uuid(),
    pet_id uuid references public.pets(id) on delete cascade,
    tenant_id uuid references public.tenants(id),
    authored_by uuid references public.profiles(id),
    title text not null default '',
    content text not null default '',
    attachments jsonb not null default '[]'::jsonb,
    created_at timestamptz not null default now()
);

alter table public.medical_records
    add column if not exists pet_id uuid,
    add column if not exists tenant_id uuid,
    add column if not exists authored_by uuid,
    add column if not exists title text not null default '',
    add column if not exists content text not null default '',
    add column if not exists attachments jsonb not null default '[]'::jsonb;

create index if not exists medical_records_pet_id_idx on public.medical_records(pet_id);

-- 11. CLINICAL CONSULTATIONS
do $$ begin
    create type public.consultation_status as enum ('DRAFT', 'PUBLISHED', 'AMENDED');
exception
    when duplicate_object then null;
end $$;

create table if not exists public.clinical_consultations (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid references public.tenants(id),
    pet_id uuid references public.pets(id) on delete cascade,
    veterinarian_id uuid references public.profiles(id),
    appointment_id uuid references public.appointments(id),
    reason text not null default '',
    observations text not null default '',
    diagnosis text not null default '',
    plan text not null default '',
    status public.consultation_status not null default 'DRAFT',
    occurred_at timestamptz not null default now(),
    idempotency_key uuid unique,
    created_at timestamptz not null default now()
);

alter table public.clinical_consultations
    add column if not exists tenant_id uuid,
    add column if not exists pet_id uuid,
    add column if not exists veterinarian_id uuid,
    add column if not exists appointment_id uuid,
    add column if not exists reason text not null default '',
    add column if not exists observations text not null default '',
    add column if not exists diagnosis text not null default '',
    add column if not exists plan text not null default '',
    add column if not exists status public.consultation_status not null default 'DRAFT',
    add column if not exists occurred_at timestamptz not null default now(),
    add column if not exists idempotency_key uuid;

create index if not exists clinical_consultations_pet_idx on public.clinical_consultations(pet_id);

-- 12. AUDIT LOGS
create table if not exists public.audit_logs (
    id uuid primary key default gen_random_uuid(),
    actor_id uuid references public.profiles(id),
    tenant_id uuid references public.tenants(id),
    action text not null default '',
    entity_type text not null default '',
    entity_id uuid,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

alter table public.audit_logs
    add column if not exists actor_id uuid,
    add column if not exists tenant_id uuid,
    add column if not exists action text not null default '',
    add column if not exists entity_type text not null default '',
    add column if not exists entity_id uuid,
    add column if not exists metadata jsonb not null default '{}'::jsonb;

create index if not exists audit_logs_tenant_time_idx on public.audit_logs(tenant_id, created_at desc);

-- 13. RLS TEMPLATES AND AUTHORIZATION HELPERS
create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'SUPERADMIN'
    );
$$;

create or replace function public.is_tenant_member(target_tenant uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select public.is_superadmin() or exists (
        select 1 from public.profiles
        where id = auth.uid() and tenant_id = target_tenant
          and role = 'VETERINARY_BUSINESS'
    );
$$;

create or replace function public.can_access_pet(target_pet uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists (select 1 from public.pets where id = target_pet and owner_id = auth.uid())
        or exists (
            select 1 from public.medical_records mr
            where mr.pet_id = target_pet and public.is_tenant_member(mr.tenant_id)
        );
$$;

alter table public.profiles enable row level security;
alter table public.tenants enable row level security;
alter table public.businesses enable row level security;
alter table public.pets enable row level security;
alter table public.services enable row level security;
alter table public.products enable row level security;
alter table public.business_schedules enable row level security;
alter table public.availability_blocks enable row level security;
alter table public.appointments enable row level security;
alter table public.medical_records enable row level security;
alter table public.clinical_consultations enable row level security;
alter table public.audit_logs enable row level security;

-- 14. BUSINESS REGISTRATION WORKFLOW MIGRATIONS & TRIGGERS
create unique index if not exists businesses_active_applicant_unique
    on public.businesses (submitted_by)
    where submitted_by is not null and status in ('PENDING', 'APPROVED');

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name, nombre, role)
    values (
        new.id,
        coalesce(new.email, ''),
        coalesce(new.raw_user_meta_data ->> 'full_name', 'Usuario'),
        coalesce(new.raw_user_meta_data ->> 'full_name', 'Usuario'),
        'USER'::public.profile_role
    )
    on conflict (id) do nothing;
    return new;
exception
    when others then
        return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

create or replace function public.guard_profile_privileges()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is not null
       and (new.role is distinct from old.role or new.tenant_id is distinct from old.tenant_id)
       and not public.is_superadmin() then
        raise exception 'profile role and tenant can only be changed by an administrator';
    end if;
    return new;
end;
$$;

drop trigger if exists profiles_guard_privileges on public.profiles;
create trigger profiles_guard_privileges
before update on public.profiles
for each row execute function public.guard_profile_privileges();

-- RPC: submit_business_application
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
    if v_user_id is null then raise exception 'authentication required'; end if;
    if not exists (
        select 1 from auth.users
        where id = v_user_id and email_confirmed_at is not null
    ) then
        raise exception 'verify the account email before submitting a business application';
    end if;
    select * into v_profile from public.profiles where id = v_user_id for update;
    if not found or v_profile.role <> 'USER' then
        raise exception 'this account cannot submit a veterinary application';
    end if;
    if length(trim(p_applicant_name)) < 2
       or length(trim(p_business_name)) < 2
       or length(trim(p_tax_id)) < 3
       or length(trim(p_phone)) < 8
       or length(trim(p_address)) < 5
       or length(trim(p_license_number)) < 2 then
        raise exception 'complete all required business application fields';
    end if;
    if exists (
        select 1 from public.businesses
        where submitted_by = v_user_id and status in ('PENDING', 'APPROVED')
    ) then
        raise exception 'an active business application already exists for this account';
    end if;

    insert into public.businesses (
        submitted_by, applicant_name, legal_name, public_name, tax_id,
        email, phone, address, license_number, status
    ) values (
        v_user_id, trim(p_applicant_name), trim(p_business_name), trim(p_business_name),
        trim(p_tax_id), coalesce(auth.jwt() ->> 'email', v_profile.email),
        trim(p_phone), trim(p_address), trim(p_license_number), 'PENDING'
    ) returning * into v_business;

    insert into public.audit_logs (actor_id, action, entity_type, entity_id)
    values (v_user_id, 'SUBMIT_BUSINESS_APPLICATION', 'business', v_business.id);
    return v_business;
end;
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
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;
    select * into v_business from public.businesses
    where id = p_business_id for update;
    if not found then raise exception 'business application not found'; end if;
    if v_business.status <> 'PENDING' then raise exception 'business application is not pending'; end if;
    if v_business.submitted_by is null then raise exception 'application has no authenticated owner'; end if;

    insert into public.tenants (name, status, created_by)
    values (v_business.public_name, 'ACTIVE', v_business.submitted_by)
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

create or replace function public.reject_business_application(
    p_business_id uuid,
    p_reason text default ''
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
    update public.businesses
    set status = 'REJECTED',
        decision_reason = nullif(trim(p_reason), ''),
        approved_by = v_actor_id,
        approved_at = now(),
        updated_at = now()
    where id = p_business_id and status = 'PENDING'
    returning * into v_business;
    if not found then raise exception 'business application is missing or no longer pending'; end if;

    insert into public.audit_logs (actor_id, action, entity_type, entity_id, metadata)
    values (
        v_actor_id, 'REJECT_BUSINESS', 'business', p_business_id,
        jsonb_build_object('reason', nullif(trim(p_reason), ''))
    );
    return v_business;
end;
$$;

-- Grant permissions for RPCs
revoke all on function public.submit_business_application(text, text, text, text, text, text) from public, anon;
grant execute on function public.submit_business_application(text, text, text, text, text, text) to authenticated;
revoke all on function public.approve_business_application(uuid) from public, anon;
grant execute on function public.approve_business_application(uuid) to authenticated;
revoke all on function public.reject_business_application(uuid, text) from public, anon;
grant execute on function public.reject_business_application(uuid, text) to authenticated;
