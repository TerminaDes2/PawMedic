-- =============================================================================
-- Crear / Restablecer Usuario SUPERADMIN BapoAdm@gmail.com
-- Ejecutar en Supabase Dashboard -> SQL Editor
-- =============================================================================

create extension if not exists "pgcrypto";

do $$
declare
    v_user_id uuid := 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'::uuid;
    v_email text := 'bapoadm@gmail.com';
    v_password text := '123456';
    v_encrypted_pw text := extensions.crypt(v_password, extensions.gen_salt('bf', 10));
begin
    -- Desactivar guardas temporales
    drop trigger if exists profiles_guard_privileges on public.profiles;

    -- Limpiar cuenta anterior si existía
    delete from public.profiles where lower(email) = lower(v_email);
    delete from auth.users where lower(email) = lower(v_email);

    -- Insertar en auth.users con hash bcrypt oficial de Supabase
    insert into auth.users (
        instance_id,
        id,
        aud,
        role,
        email,
        encrypted_password,
        email_confirmed_at,
        last_sign_in_at,
        raw_app_meta_data,
        raw_user_meta_data,
        is_super_admin,
        created_at,
        updated_at,
        confirmation_token,
        email_change,
        email_change_token_new,
        recovery_token
    ) values (
        '00000000-0000-0000-0000-000000000000',
        v_user_id,
        'authenticated',
        'authenticated',
        v_email,
        v_encrypted_pw,
        now(),
        now(),
        '{"provider": "email", "providers": ["email"]}'::jsonb,
        '{"full_name": "Super Admin Bapo"}'::jsonb,
        false,
        now(),
        now(),
        '',
        '',
        '',
        ''
    );

    -- Insertar perfil en public.profiles con rol SUPERADMIN
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

    -- Re-activar guarda de perfiles
    drop trigger if exists profiles_guard_privileges on public.profiles;
    create trigger profiles_guard_privileges
    before update on public.profiles
    for each row execute function public.guard_profile_privileges();

end $$;
