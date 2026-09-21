create table public.products (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id) on delete cascade,
    sku text not null,
    name text not null,
    description text not null default '',
    price_cents integer not null check (price_cents >= 0),
    stock_quantity integer not null default 0 check (stock_quantity >= 0),
    active boolean not null default true,
    created_at timestamptz not null default now(),
    unique (tenant_id, sku)
);
