-- =============================================================================
-- Script Maestro Definitivo: Eliminar cualquier restricción NOT NULL problemática en TODAS las tablas principales
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

do $$
declare
    tbl record;
    col record;
begin
    for tbl in (
        select table_name
        from information_schema.tables
        where table_schema = 'public'
          and table_type = 'BASE TABLE'
    ) loop
        for col in (
            select column_name
            from information_schema.columns
            where table_schema = 'public'
              and table_name = tbl.table_name
              and is_nullable = 'NO'
              and column_name not in ('id', 'created_at', 'updated_at', 'owner_id', 'submitted_by', 'tenant_id')
              and data_type not in ('USER-DEFINED', 'ARRAY', 'uuid', 'boolean', 'integer', 'date', 'timestamptz')
        ) loop
            execute format('alter table public.%I alter column %I drop not null;', tbl.table_name, col.column_name);
            execute format('alter table public.%I alter column %I set default '''';', tbl.table_name, col.column_name);
        end loop;
    end loop;
end $$;

-- Asegurar explícitamente columnas clave en pets
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

do $$ begin
    alter table public.pets alter column nombre drop not null;
    alter table public.pets alter column nombre set default '';
exception when others then null; end $$;
