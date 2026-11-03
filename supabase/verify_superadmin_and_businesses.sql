-- =============================================================================
-- Script de Verificación y Reparación de Superadmin y Permisos de Solicitudes
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Forzar que el usuario bapoadm@gmail.com tenga el rol SUPERADMIN en public.profiles
update public.profiles
set role = 'SUPERADMIN'::public.profile_role
where lower(email) = 'bapoadm@gmail.com';

-- 2. Verificar que la función is_superadmin funcione correctamente
create or replace function public.is_superadmin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'SUPERADMIN'
    );
$$;

-- 3. Asegurar la política RLS de lectura para businesses en el panel de superadmin
drop policy if exists businesses_authorized_read on public.businesses;
create policy businesses_authorized_read on public.businesses for select to authenticated
using (
    submitted_by = auth.uid()
    or public.is_superadmin()
);

-- 4. Mostrar todas las solicitudes pendientes actuales en la base de datos (para verificar que existen)
select id, public_name, applicant_name, email, status, created_at
from public.businesses
order by created_at desc;
