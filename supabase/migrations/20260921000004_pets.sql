create table public.pets (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references public.profiles(id) on delete cascade,
    name text not null check (length(trim(name)) > 0),
    species text not null,
    breed text,
    date_of_birth date,
    weight_grams integer check (weight_grams is null or weight_grams > 0),
    notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index pets_owner_id_idx on public.pets(owner_id);
