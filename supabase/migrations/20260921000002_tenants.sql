create type public.tenant_status as enum ('PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED');

create table public.tenants (
    id uuid primary key default gen_random_uuid(),
    name text not null check (length(trim(name)) > 0),
    status public.tenant_status not null default 'PENDING',
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.profiles
    add constraint profiles_tenant_fk foreign key (tenant_id) references public.tenants(id);

create index profiles_tenant_id_idx on public.profiles(tenant_id);
