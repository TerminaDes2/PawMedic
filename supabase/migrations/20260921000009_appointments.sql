create type public.appointment_status as enum ('REQUESTED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW');

create table public.appointments (
    id uuid primary key default gen_random_uuid(),
    tenant_id uuid not null references public.tenants(id),
    pet_id uuid not null references public.pets(id),
    service_id uuid not null references public.services(id),
    booked_by uuid not null references public.profiles(id),
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    status public.appointment_status not null default 'REQUESTED',
    idempotency_key uuid not null unique,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    check (starts_at < ends_at)
);

create index appointments_tenant_time_idx on public.appointments(tenant_id, starts_at);
