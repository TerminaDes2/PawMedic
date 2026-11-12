-- =============================================================================
-- MIGRACIÓN: Módulo de Expediente Médico Veterinario
-- Fecha: 2026-09-21
-- Descripción: Tablas, RLS, Auditoría y Funciones RPC para el Expediente Médico Digital
-- =============================================================================

-- 1. Helper Function: Verificar si el usuario es Veterinario o Superadmin
create or replace function public.is_veterinarian()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'VETERINARY_BUSINESS'
    ) or public.is_superadmin();
$$;

-- 2. Helper Function: Verificar acceso a la mascota (propietario o miembro del tenant)
create or replace function public.can_access_pet(target_pet uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.pets where id = target_pet and owner_id = auth.uid()
    ) or exists (
        select 1
        from public.medical_records mr
        where mr.pet_id = target_pet
          and public.is_tenant_member(mr.tenant_id)
    ) or exists (
        select 1
        from public.clinical_consultations cc
        where cc.pet_id = target_pet
          and public.is_tenant_member(cc.tenant_id)
    );
$$;

-- 3. Tabla: Antecedentes Médicos de Mascotas
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

-- 4. Ampliación / Estructura de Consultas Clínicas (clinical_consultations)
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

-- 5. Tabla: Medicamentos y Recetas
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

create index if not exists prescriptions_pet_idx on public.prescriptions(pet_id);

-- 6. Tabla: Vacunas Aplicadas
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

create index if not exists vaccinations_pet_idx on public.vaccinations(pet_id);

-- 7. Tabla: Desparasitaciones
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

create index if not exists dewormings_pet_idx on public.dewormings(pet_id);

-- 8. Tabla: Estudios Clínicos
create table if not exists public.clinical_studies (
    id uuid primary key default gen_random_uuid(),
    consultation_id uuid references public.clinical_consultations(id) on delete cascade,
    pet_id uuid not null references public.pets(id) on delete cascade,
    study_type text not null default 'LABORATORY', -- LABORATORY, XRAY, ULTRASOUND, CYTOLOGY, HEMOGRAM, BLOOD_CHEMISTRY, OTHER
    name text not null,
    status text not null default 'COMPLETED',
    requested_at timestamptz not null default now(),
    results_notes text not null default '',
    attachment_urls jsonb not null default '[]'::jsonb,
    created_at timestamptz not null default now()
);

create index if not exists clinical_studies_pet_idx on public.clinical_studies(pet_id);

-- 9. Habilitar RLS en las nuevas tablas
alter table public.pet_backgrounds enable row level security;
alter table public.prescriptions enable row level security;
alter table public.vaccinations enable row level security;
alter table public.dewormings enable row level security;
alter table public.clinical_studies enable row level security;

-- 10. Políticas de Seguridad (RLS Policies)

-- PET BACKGROUNDS
drop policy if exists pet_backgrounds_select on public.pet_backgrounds;
drop policy if exists pet_backgrounds_vet_write on public.pet_backgrounds;
create policy pet_backgrounds_select on public.pet_backgrounds for select
using (public.can_access_pet(pet_id));

create policy pet_backgrounds_vet_write on public.pet_backgrounds for all
using (public.is_veterinarian() and public.can_access_pet(pet_id))
with check (public.is_veterinarian() and public.can_access_pet(pet_id));

-- CLINICAL CONSULTATIONS
drop policy if exists consultations_clinical_access on public.clinical_consultations;
drop policy if exists consultations_veterinary_write on public.clinical_consultations;
drop policy if exists consultations_select_access on public.clinical_consultations;
drop policy if exists consultations_vet_write on public.clinical_consultations;

create policy consultations_select_access on public.clinical_consultations for select
using (public.can_access_pet(pet_id));

create policy consultations_vet_write on public.clinical_consultations for all
using (
    public.is_veterinarian()
    and public.is_tenant_member(tenant_id)
    and veterinarian_id = auth.uid()
)
with check (
    public.is_veterinarian()
    and public.is_tenant_member(tenant_id)
    and veterinarian_id = auth.uid()
);

-- PRESCRIPTIONS
drop policy if exists prescriptions_select on public.prescriptions;
drop policy if exists prescriptions_vet_write on public.prescriptions;
create policy prescriptions_select on public.prescriptions for select
using (public.can_access_pet(pet_id));

create policy prescriptions_vet_write on public.prescriptions for all
using (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = prescriptions.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
)
with check (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = prescriptions.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
);

-- VACCINATIONS
drop policy if exists vaccinations_select on public.vaccinations;
drop policy if exists vaccinations_vet_write on public.vaccinations;
create policy vaccinations_select on public.vaccinations for select
using (public.can_access_pet(pet_id));

create policy vaccinations_vet_write on public.vaccinations for all
using (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = vaccinations.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
)
with check (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = vaccinations.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
);

-- DEWORMINGS
drop policy if exists dewormings_select on public.dewormings;
drop policy if exists dewormings_vet_write on public.dewormings;
create policy dewormings_select on public.dewormings for select
using (public.can_access_pet(pet_id));

create policy dewormings_vet_write on public.dewormings for all
using (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = dewormings.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
)
with check (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = dewormings.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
);

-- CLINICAL STUDIES
drop policy if exists clinical_studies_select on public.clinical_studies;
drop policy if exists clinical_studies_vet_write on public.clinical_studies;
create policy clinical_studies_select on public.clinical_studies for select
using (public.can_access_pet(pet_id));

create policy clinical_studies_vet_write on public.clinical_studies for all
using (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = clinical_studies.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
)
with check (
    public.is_veterinarian()
    and exists (
        select 1 from public.clinical_consultations cc
        where cc.id = consultation_id
          and cc.pet_id = clinical_studies.pet_id
          and public.is_tenant_member(cc.tenant_id)
    )
);

-- 11. Trigger para Bitácora de Auditoría Automática
create or replace function public.log_medical_audit_event()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    actor_role_val text := 'VETERINARIAN';
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
