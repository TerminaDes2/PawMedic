-- =============================================================================
-- Script Definitivo: Relajar restricciones NOT NULL en columnas de pets (incluyendo edad)
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

do $$
declare
    col record;
begin
    for col in (
        select c.column_name
        from information_schema.columns c
        where c.table_schema = 'public'
          and c.table_name = 'pets'
          and c.is_nullable = 'NO'
          and c.column_name not in ('id', 'created_at', 'updated_at', 'owner_id')
          and c.data_type not in ('USER-DEFINED', 'ARRAY')
    ) loop
        execute format('alter table public.pets alter column %I drop not null;', col.column_name);
        execute format('alter table public.pets alter column %I set default '''';', col.column_name);
    end loop;
end $$;

-- Asegurar explícitamente columnas comunes en pets
alter table public.pets
    add column if not exists name text not null default '',
    add column if not exists nombre text not null default '',
    add column if not exists edad text not null default '',
    add column if not exists species text not null default '',
    add column if not exists breed text,
    add column if not exists notes text;

do $$ begin
    alter table public.pets alter column edad drop not null;
    alter table public.pets alter column edad set default '';
exception when others then null; end $$;
