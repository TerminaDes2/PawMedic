create type public.consultation_status as enum ('DRAFT', 'PUBLISHED', 'AMENDED');

create table public.clinical_consultations (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id),
    pet_id uuid not null references public.pets(id) on delete cascade,
    veterinarian_id uuid not null references public.profiles(id),
    appointment_id uuid references public.appointments(id),
    reason text not null,
    observations text not null,
    diagnosis text not null default '',
    plan text not null default '',
    status public.consultation_status not null default 'DRAFT',
    occurred_at timestamptz not null default now(),
    idempotency_key uuid not null unique,
    created_at timestamptz not null default now()
);

create index clinical_consultations_pet_idx on public.clinical_consultations(pet_id);
