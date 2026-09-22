create table public.audit_logs (
    id uuid primary key default gen_random_uuid(),
    actor_id uuid references public.profiles(id),
    tenant_id uuid references public.tenants(id),
    action text not null,
    entity_type text not null,
    entity_id uuid,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create index audit_logs_tenant_time_idx on public.audit_logs(tenant_id, created_at desc);
