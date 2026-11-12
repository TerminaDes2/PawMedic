-- =============================================================================
-- Corrección de la columna "nombre" en la tabla pets
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

alter table public.pets
    add column if not exists nombre text not null default '';

do $$ begin
    alter table public.pets alter column nombre drop not null;
    alter table public.pets alter column nombre set default '';
exception when others then null; end $$;

-- Sincronizar nombre con name si estuviera vacío
update public.pets set nombre = name where nombre = '' and name <> '';
update public.pets set name = nombre where name = '' and nombre <> '';
