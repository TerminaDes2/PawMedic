create type public.business_status as enum ('PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED');

create table public.businesses (
    id uuid primary key default gen_random_uuid(),
    -- A pending application has no tenant yet; approval creates it atomically.
    tenant_id uuid unique references public.tenants(id) on delete cascade,
    submitted_by uuid references public.profiles(id),
    legal_name text not null,
    public_name text not null,
    status public.business_status not null default 'PENDING',
    approved_by uuid references public.profiles(id),
    approved_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index businesses_status_idx on public.businesses(status);
