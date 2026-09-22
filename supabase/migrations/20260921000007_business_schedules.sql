create table public.business_schedules (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id) on delete cascade,
    weekday smallint not null check (weekday between 0 and 6),
    opens_at time not null,
    closes_at time not null,
    unique (tenant_id, weekday),
    check (opens_at < closes_at)
);
