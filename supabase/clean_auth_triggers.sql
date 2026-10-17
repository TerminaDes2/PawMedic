-- =============================================================================
-- Script de Limpieza Completa de Triggers y Perfiles
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

-- 1. Eliminar TODOS los triggers personalizados existentes en la tabla auth.users
do $$
declare
    trig record;
begin
    for trig in (
        select trigger_name
        from information_schema.triggers
        where event_object_schema = 'auth'
          and event_object_table = 'users'
    ) loop
        execute format('drop trigger if exists %I on auth.users;', trig.trigger_name);
    end loop;
end $$;

-- 2. Asegurar que las columnas de public.profiles tengan valores por defecto seguros
alter table public.profiles alter column email set default '';
alter table public.profiles alter column display_name set default '';
alter table public.profiles alter column role set default 'USER'::public.profile_role;

do $$ begin
    alter table public.profiles alter column nombre drop not null;
    alter table public.profiles alter column nombre set default '';
exception when others then null; end $$;

do $$ begin
    alter table public.profiles alter column apellidos drop not null;
    alter table public.profiles alter column apellidos set default '';
exception when others then null; end $$;

do $$ begin
    alter table public.profiles alter column num_tel drop not null;
exception when others then null; end $$;

-- 3. Definición a prueba de fallos de handle_new_user
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_full_name text := coalesce(new.raw_user_meta_data ->> 'full_name', 'Usuario');
begin
    insert into public.profiles (
        id,
        email,
        display_name,
        nombre,
        role
    ) values (
        new.id,
        coalesce(new.email, ''),
        v_full_name,
        v_full_name,
        'USER'::public.profile_role
    )
    on conflict (id) do nothing;

    return new;
exception
    when others then
        -- Garantiza que el registro en auth.users NUNCA devuelva error 500
        return new;
end;
$$;

-- 4. Recrear ÚNICAMENTE el trigger oficial en auth.users
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();
