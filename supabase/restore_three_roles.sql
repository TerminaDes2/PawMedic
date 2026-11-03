-- =============================================================================
-- Restauración de los 3 Roles: USER, VETERINARY_BUSINESS, SUPERADMIN
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Recrear el tipo ENUM profile_role con 3 valores
do $$ begin
    create type public.profile_role as enum ('USER', 'VETERINARY_BUSINESS', 'SUPERADMIN');
exception
    when duplicate_object then null;
end $$;

-- 2. Asegurar que la columna role en public.profiles utilice el enum profile_role
alter table public.profiles alter column role type text;
drop type public.profile_role cascade;
create type public.profile_role as enum ('USER', 'VETERINARY_BUSINESS', 'SUPERADMIN');
alter table public.profiles alter column role type public.profile_role using role::public.profile_role;
alter table public.profiles alter column role set default 'USER'::public.profile_role;

-- 3. Restaurar la función is_superadmin() clásica
create or replace function public.is_superadmin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and role = 'SUPERADMIN'::public.profile_role
    );
$$;

-- 4. Asegurar que BapoAdm@gmail.com tenga exactamente el rol SUPERADMIN
update public.profiles
set role = 'SUPERADMIN'::public.profile_role,
    tenant_id = null
where lower(email) = 'bapoadm@gmail.com';

-- 5. Asegurar políticas RLS para businesses con is_superadmin()
drop policy if exists businesses_authorized_read on public.businesses;
create policy businesses_authorized_read on public.businesses for select to authenticated
using (
    submitted_by = auth.uid()
    or public.is_superadmin()
);
