-- =============================================================================
-- SCRIPT COMPLETO AUTO-CONTENIDO PARA SUPABASE SQL EDITOR
-- MIGRACIÓN: Módulo de Expediente Médico Veterinario
-- =============================================================================

-- 1. Helper Function: Verificar si el usuario es Superadmin
create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'SUPERADMIN'
    );
$$;

-- 2. Helper Function: Verificar si el usuario es Miembro de un Tenant/Clínica
create or replace function public.is_tenant_member(target_tenant uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select public.is_superadmin() or exists (
        select 1 from public.profiles
        where id = auth.uid() and tenant_id = target_tenant
          and role = 'VETERINARY_BUSINESS'
    );
$$;

-- 3. Helper Function: Verificar si el usuario es Veterinario o Superadmin
create or replace function public.is_veterinarian()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'VETERINARY_BUSINESS'
    ) or public.is_superadmin();
$$;

-- 4. Helper Function: Verificar acceso a la mascota (Propietario o Veterinario)
create or replace function public.can_access_pet(target_pet uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.pets where id = target_pet and owner_id = auth.uid()
    ) or public.is_veterinarian();
$$;

-- 5. Crear Tablas del Expediente Médico (si no existen)
create table if not exists public.pet_backgrounds (
    pet_id uuid primary key references public.pets(id) on delete cascade,
    last_deworming_date date,
    deworming_product text not null default '',
    diet text not null default '',
    allergies text not null default '',
    past_diseases text not null default '',
    past_surgeries text not null default '',
    family_history text not null default '',
    reproductive_status text not null default '',
    habitat text not null default '',
    updated_at timestamptz not null default now(),
    updated_by uuid references public.profiles(id)
);

alter table public.clinical_consultations
    add column if not exists anamnesis text not null default '',
    add column if not exists vital_signs jsonb not null default '{}'::jsonb,
    add column if not exists systems_exploration jsonb not null default '{}'::jsonb,
    add column if not exists problem_list jsonb not null default '[]'::jsonb,
    add column if not exists master_problem_list jsonb not null default '[]'::jsonb,
    add column if not exists differential_diagnoses text not null default '',
    add column if not exists definitive_diagnosis text not null default '',
    add column if not exists diagnostic_plan text not null default '',
    add column if not exists treatment_plan text not null default '',
    add column if not exists next_appointment_date date;

create table if not exists public.prescriptions (
    id uuid primary key default gen_random_uuid(),
    consultation_id uuid references public.clinical_consultations(id) on delete cascade,
    pet_id uuid not null references public.pets(id) on delete cascade,
    drug_name text not null,
    dosage text not null default '',
    unit text not null default '',
    route text not null default 'Oral',
    frequency text not null default 'Cada 12h',
    duration text not null default '7 días',
    start_date date,
    end_date date,
    is_administered_in_consultation boolean not null default false,
    prescribing_vet_id uuid references public.profiles(id),
    notes text not null default '',
    created_at timestamptz not null default now()
);

create table if not exists public.vaccinations (
    id uuid primary key default gen_random_uuid(),
    pet_id uuid not null references public.pets(id) on delete cascade,
    consultation_id uuid references public.clinical_consultations(id) on delete set null,
    vaccine_name text not null,
    batch_number text not null default '',
    applied_date date not null default current_date,
    next_due_date date,
    applied_by uuid references public.profiles(id),
    notes text not null default '',
    created_at timestamptz not null default now()
);

create table if not exists public.dewormings (
    id uuid primary key default gen_random_uuid(),
    pet_id uuid not null references public.pets(id) on delete cascade,
    consultation_id uuid references public.clinical_consultations(id) on delete set null,
    product_name text not null,
    dosage text not null default '',
    applied_date date not null default current_date,
    next_due_date date,
    applied_by uuid references public.profiles(id),
    notes text not null default '',
    created_at timestamptz not null default now()
);

create table if not exists public.clinical_studies (
    id uuid primary key default gen_random_uuid(),
    consultation_id uuid references public.clinical_consultations(id) on delete cascade,
    pet_id uuid not null references public.pets(id) on delete cascade,
    study_type text not null default 'LABORATORY',
    name text not null,
    status text not null default 'COMPLETED',
    requested_at timestamptz not null default now(),
    results_notes text not null default '',
    attachment_urls jsonb not null default '[]'::jsonb,
    created_at timestamptz not null default now()
);

-- 6. Habilitar RLS en todas las tablas
alter table public.pets enable row level security;
alter table public.pet_backgrounds enable row level security;
alter table public.clinical_consultations enable row level security;
alter table public.medical_records enable row level security;
alter table public.prescriptions enable row level security;
alter table public.vaccinations enable row level security;
alter table public.dewormings enable row level security;
alter table public.clinical_studies enable row level security;

-- 7. Crear Políticas RLS

-- PETS (Permite a cualquier Veterinario buscar y gestionar mascotas registradas)
drop policy if exists pets_owner_crud on public.pets;
drop policy if exists pets_select_access on public.pets;
drop policy if exists pets_vet_read_write on public.pets;

create policy pets_select_access on public.pets for select
using (owner_id = auth.uid() or public.is_veterinarian());

create policy pets_vet_read_write on public.pets for all
using (owner_id = auth.uid() or public.is_veterinarian())
with check (owner_id = auth.uid() or public.is_veterinarian());

-- PET BACKGROUNDS
drop policy if exists pet_backgrounds_select on public.pet_backgrounds;
drop policy if exists pet_backgrounds_vet_write on public.pet_backgrounds;

create policy pet_backgrounds_select on public.pet_backgrounds for select
using (public.can_access_pet(pet_id));

create policy pet_backgrounds_vet_write on public.pet_backgrounds for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- CLINICAL CONSULTATIONS
drop policy if exists consultations_clinical_access on public.clinical_consultations;
drop policy if exists consultations_veterinary_write on public.clinical_consultations;
drop policy if exists consultations_select_access on public.clinical_consultations;
drop policy if exists consultations_vet_write on public.clinical_consultations;

create policy consultations_select_access on public.clinical_consultations for select
using (public.can_access_pet(pet_id));

create policy consultations_vet_write on public.clinical_consultations for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- MEDICAL RECORDS
drop policy if exists medical_records_select_access on public.medical_records;
drop policy if exists medical_records_clinical_access on public.medical_records;
drop policy if exists medical_records_vet_write on public.medical_records;

create policy medical_records_select_access on public.medical_records for select
using (public.can_access_pet(pet_id));

create policy medical_records_vet_write on public.medical_records for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- PRESCRIPTIONS
drop policy if exists prescriptions_select on public.prescriptions;
drop policy if exists prescriptions_vet_write on public.prescriptions;

create policy prescriptions_select on public.prescriptions for select
using (public.can_access_pet(pet_id));

create policy prescriptions_vet_write on public.prescriptions for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- VACCINATIONS
drop policy if exists vaccinations_select on public.vaccinations;
drop policy if exists vaccinations_vet_write on public.vaccinations;

create policy vaccinations_select on public.vaccinations for select
using (public.can_access_pet(pet_id));

create policy vaccinations_vet_write on public.vaccinations for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- DEWORMINGS
drop policy if exists dewormings_select on public.dewormings;
drop policy if exists dewormings_vet_write on public.dewormings;

create policy dewormings_select on public.dewormings for select
using (public.can_access_pet(pet_id));

create policy dewormings_vet_write on public.dewormings for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- CLINICAL STUDIES
drop policy if exists clinical_studies_select on public.clinical_studies;
drop policy if exists clinical_studies_vet_write on public.clinical_studies;

create policy clinical_studies_select on public.clinical_studies for select
using (public.can_access_pet(pet_id));

create policy clinical_studies_vet_write on public.clinical_studies for all
using (public.is_veterinarian())
with check (public.is_veterinarian());

-- 8. Trigger para Bitácora de Auditoría Automática
create or replace function public.log_medical_audit_event()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.audit_logs(
        actor_id,
        tenant_id,
        action,
        entity_type,
        entity_id,
        metadata
    ) values (
        auth.uid(),
        case when TG_TABLE_NAME = 'clinical_consultations' then NEW.tenant_id else null end,
        TG_OP,
        TG_TABLE_NAME,
        coalesce(NEW.id, OLD.id),
        jsonb_build_object(
            'timestamp', now(),
            'actor_id', auth.uid(),
            'pet_id', coalesce(NEW.pet_id, OLD.pet_id),
            'previous', case when TG_OP = 'UPDATE' then row_to_json(OLD)::jsonb else null end,
            'current', case when TG_OP in ('INSERT', 'UPDATE') then row_to_json(NEW)::jsonb else null end
        )
    );
    return NEW;
end;
$$;

drop trigger if exists audit_consultations_trg on public.clinical_consultations;
create trigger audit_consultations_trg
after insert or update or delete on public.clinical_consultations
for each row execute function public.log_medical_audit_event();

drop trigger if exists audit_backgrounds_trg on public.pet_backgrounds;
create trigger audit_backgrounds_trg
after insert or update on public.pet_backgrounds
for each row execute function public.log_medical_audit_event();
