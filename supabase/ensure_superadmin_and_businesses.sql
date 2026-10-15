-- =============================================================================
-- Solución Definitiva para Superadmin y Lectura de Solicitudes (RLS & 2 Roles)
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Asegurar tipo ENUM profile_role con 'USER' y 'VETERINARY_BUSINESS'
do $$ begin
    create type public.profile_role as enum ('USER', 'VETERINARY_BUSINESS');
exception
    when duplicate_object then null;
end $$;

-- 2. Función is_superadmin infalible (reconoce a BapoAdm@gmail.com o cualquier VETERINARY_BUSINESS sin tenant_id)
create or replace function public.is_superadmin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid()
          and (
              lower(email) = 'bapoadm@gmail.com'
              or (role = 'VETERINARY_BUSINESS'::public.profile_role and tenant_id is null)
          )
    );
$$;

-- 3. Configurar definitivamente al usuario BapoAdm@gmail.com como Superadmin
update public.profiles
set role = 'VETERINARY_BUSINESS'::public.profile_role,
    tenant_id = null
where lower(email) = 'bapoadm@gmail.com';

-- 4. Asegurar que las políticas RLS en public.businesses permitan al Superadmin ver todas las solicitudes
alter table public.businesses enable row level security;

drop policy if exists businesses_authorized_read on public.businesses;
create policy businesses_authorized_read on public.businesses for select to authenticated
using (
    submitted_by = auth.uid()
    or public.is_superadmin()
);

drop policy if exists businesses_all_superadmin on public.businesses;
create policy businesses_all_superadmin on public.businesses for all to authenticated
using (public.is_superadmin())
with check (public.is_superadmin());

-- 5. Consulta de prueba para verificar que las solicitudes se devuelven correctamente
select id, public_name, applicant_name, email, status, created_at
from public.businesses
order by created_at desc;
