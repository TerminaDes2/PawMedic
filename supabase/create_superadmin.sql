-- =============================================================================
-- Script para crear / restablecer la cuenta SUPERADMIN BapoAdm@gmail.com
-- Ejecutar este archivo completo en Supabase Dashboard -> SQL Editor
-- =============================================================================

create extension if not exists "pgcrypto";

-- 1. Asegurar que el trigger de registro no falle
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name, nombre, role)
    values (
        new.id,
        coalesce(new.email, ''),
        coalesce(new.raw_user_meta_data ->> 'full_name', 'Usuario'),
        coalesce(new.raw_user_meta_data ->> 'full_name', 'Usuario'),
        'USER'::public.profile_role
    )
    on conflict (id) do nothing;
    return new;
exception
    when others then
        return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

-- 2. Crear o actualizar el usuario en auth.users y public.profiles
do $$
declare
    v_user_id uuid;
    v_email text := 'bapoadm@gmail.com';
    v_password text := '123456';
    v_encrypted_pw text := crypt(v_password, gen_salt('bf'));
begin
    -- Desactivar temporalmente la guarda de perfiles mientras insertamos
    drop trigger if exists profiles_guard_privileges on public.profiles;

    -- Buscar usuario existente
    select id into v_user_id from auth.users where lower(email) = lower(v_email);

    if v_user_id is null then
        v_user_id := gen_random_uuid();

        insert into auth.users (
            id,
            instance_id,
            aud,
            role,
            email,
            encrypted_password,
            email_confirmed_at,
            raw_app_meta_data,
            raw_user_meta_data,
            created_at,
            updated_at,
            confirmation_token,
            email_change,
            email_change_token_new,
            recovery_token,
            is_super_admin
        ) values (
            v_user_id,
            '00000000-0000-0000-0000-000000000000',
            'authenticated',
            'authenticated',
            v_email,
            v_encrypted_pw,
            now(),
            '{"provider": "email", "providers": ["email"]}'::jsonb,
            '{"full_name": "Super Admin Bapo"}'::jsonb,
            now(),
            now(),
            '',
            '',
            '',
            '',
            false
        );
    else
        update auth.users
        set encrypted_password = v_encrypted_pw,
            email_confirmed_at = coalesce(email_confirmed_at, now()),
            updated_at = now()
        where id = v_user_id;
    end if;

    -- Crear o actualizar perfil con rol SUPERADMIN
    insert into public.profiles (
        id,
        email,
        display_name,
        nombre,
        role,
        created_at,
        updated_at
    ) values (
        v_user_id,
        v_email,
        'Super Admin Bapo',
        'Super Admin Bapo',
        'SUPERADMIN'::public.profile_role,
        now(),
        now()
    )
    on conflict (id) do update
    set role = 'SUPERADMIN'::public.profile_role,
        email = v_email,
        display_name = 'Super Admin Bapo',
        nombre = 'Super Admin Bapo',
        updated_at = now();

    -- Re-activar el trigger de la guarda de perfiles
    drop trigger if exists profiles_guard_privileges on public.profiles;
    create trigger profiles_guard_privileges
    before update on public.profiles
    for each row execute function public.guard_profile_privileges();

end $$;
