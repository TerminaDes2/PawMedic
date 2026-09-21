create table public.availability_blocks (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id) on delete cascade,
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    reason text not null default '',
    created_by uuid not null references public.profiles(id),
    check (starts_at < ends_at)
);

create index availability_blocks_tenant_time_idx
on public.availability_blocks(tenant_id, starts_at, ends_at);
