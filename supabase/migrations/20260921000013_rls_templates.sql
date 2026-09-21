-- Reusable authorization helpers. RLS remains the final enforcement boundary;
-- client role checks are only for navigation and presentation.
create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'SUPERADMIN'
    );
$$;

create or replace function public.is_tenant_member(target_tenant uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select public.is_superadmin() or exists (
        select 1 from public.profiles
        where id = auth.uid() and tenant_id = target_tenant
          and role = 'VETERINARY_BUSINESS'
    );
$$;

create or replace function public.can_access_pet(target_pet uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists (select 1 from public.pets where id = target_pet and owner_id = auth.uid())
        or exists (
            select 1 from public.medical_records mr
            where mr.pet_id = target_pet and public.is_tenant_member(mr.tenant_id)
        );
$$;

alter table public.profiles enable row level security;
alter table public.tenants enable row level security;
alter table public.businesses enable row level security;
alter table public.pets enable row level security;
alter table public.services enable row level security;
alter table public.products enable row level security;
alter table public.business_schedules enable row level security;
alter table public.availability_blocks enable row level security;
alter table public.appointments enable row level security;
alter table public.medical_records enable row level security;
alter table public.clinical_consultations enable row level security;
alter table public.audit_logs enable row level security;

create policy profiles_self_or_superadmin on public.profiles for select
using (id = auth.uid() or public.is_superadmin());
create policy profiles_self_update on public.profiles for update
using (id = auth.uid()) with check (id = auth.uid());

-- User pets: owners have CRUD; authorized clinical users have read access.
create policy pets_owner_crud on public.pets for all
using (owner_id = auth.uid() or public.can_access_pet(id))
with check (owner_id = auth.uid());

-- Tenant isolation: business data is visible only to members of that tenant.
create policy businesses_tenant_isolation on public.businesses for select
using (public.is_tenant_member(tenant_id));
create policy services_tenant_isolation on public.services for all
using (public.is_tenant_member(tenant_id)) with check (public.is_tenant_member(tenant_id));
create policy products_tenant_isolation on public.products for all
using (public.is_tenant_member(tenant_id)) with check (public.is_tenant_member(tenant_id));
create policy schedules_tenant_isolation on public.business_schedules for all
using (public.is_tenant_member(tenant_id)) with check (public.is_tenant_member(tenant_id));
create policy blocks_tenant_isolation on public.availability_blocks for all
using (public.is_tenant_member(tenant_id)) with check (public.is_tenant_member(tenant_id));
create policy appointments_authorized_access on public.appointments for all
using (booked_by = auth.uid() or public.is_tenant_member(tenant_id))
with check (booked_by = auth.uid() or public.is_tenant_member(tenant_id));

-- Clinical access is restricted to the pet owner or an approved tenant member.
create policy medical_records_clinical_access on public.medical_records for select
using (public.can_access_pet(pet_id));
create policy consultations_clinical_access on public.clinical_consultations for select
using (public.can_access_pet(pet_id));
create policy consultations_veterinary_write on public.clinical_consultations for insert
with check (veterinarian_id = auth.uid() and public.is_tenant_member(tenant_id));

-- Superadmin has explicit access to administrative data, including audit logs.
create policy tenants_superadmin_or_member on public.tenants for select
using (public.is_superadmin() or public.is_tenant_member(id));
create policy audit_logs_superadmin_or_actor on public.audit_logs for select
using (public.is_superadmin() or actor_id = auth.uid());
create policy audit_logs_insert_actor on public.audit_logs for insert
with check (actor_id = auth.uid() or public.is_superadmin());

-- Storage: object paths must begin with the authenticated user's id or tenant id.
create policy storage_user_or_tenant_read on storage.objects for select
using (
    bucket_id = 'pet-files'
    and (
        (storage.foldername(name))[1] = auth.uid()::text
        or exists (
            select 1 from public.tenants t
            where t.id::text = (storage.foldername(name))[1]
              and public.is_tenant_member(t.id)
        )
    )
);
create policy storage_user_upload on storage.objects for insert
with check (bucket_id = 'pet-files' and (storage.foldername(name))[1] = auth.uid()::text);
