create table public.services (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id) on delete cascade,
    name text not null,
    description text not null default '',
    duration_minutes integer not null check (duration_minutes > 0),
    price_cents integer not null check (price_cents >= 0),
    active boolean not null default true,
    created_at timestamptz not null default now()
);

create index services_tenant_id_idx on public.services(tenant_id);
