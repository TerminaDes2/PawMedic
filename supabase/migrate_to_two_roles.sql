-- =============================================================================
-- Migración a 2 Roles: USER y VETERINARY_BUSINESS (Superadmin = Vet Business sin Tenant)
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Actualizar perfiles SUPERADMIN a VETERINARY_BUSINESS con tenant_id NULL (Superadmin de plataforma)
update public.profiles
set role = 'VETERINARY_BUSINESS'::text::public.profile_role,
    tenant_id = null
where role::text = 'SUPERADMIN';

-- 2. Modificar el tipo ENUM profile_role para que solo tenga 'USER' y 'VETERINARY_BUSINESS'
-- PostgreSQL no permite eliminar un valor de un enum directamente, por lo que recreamos el tipo de forma segura:
create type public.profile_role_new as enum ('USER', 'VETERINARY_BUSINESS');

-- Cambiar temporalmente la columna role a text
alter table public.profiles alter column role type text;

-- Eliminar el tipo antiguo y renombrar el nuevo
drop type public.profile_role cascade;
alter type public.profile_role_new rename to profile_role;

-- Reconvertir la columna role al nuevo enum profile_role
alter table public.profiles
    alter column role type public.profile_role
    using role::public.profile_role;

alter table public.profiles
    alter column role set default 'USER'::public.profile_role;

-- 3. Actualizar la función is_superadmin() para identificar al Superadmin como VETERINARY_BUSINESS con tenant_id IS NULL
create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid()
          and role = 'VETERINARY_BUSINESS'
          and tenant_id is null
    );
$$;

-- 4. Asegurar que bapoadm@gmail.com sea Superadmin de plataforma (VETERINARY_BUSINESS sin tenant_id)
update public.profiles
set role = 'VETERINARY_BUSINESS'::public.profile_role,
    tenant_id = null
where lower(email) = 'bapoadm@gmail.com';

-- 5. Comprobación final
select id, email, role, tenant_id
from public.profiles
where lower(email) = 'bapoadm@gmail.com';
