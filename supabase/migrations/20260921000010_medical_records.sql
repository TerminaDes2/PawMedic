create table public.medical_records (
    id uuid primary key default gen_random_uuid(),
    pet_id uuid not null references public.pets(id) on delete cascade,
    tenant_id uuid not null references public.tenants(id),
    authored_by uuid not null references public.profiles(id),
    title text not null,
    content text not null,
    attachments jsonb not null default '[]'::jsonb,
    created_at timestamptz not null default now()
);

create index medical_records_pet_id_idx on public.medical_records(pet_id);
