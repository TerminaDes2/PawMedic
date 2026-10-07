-- Semilla de desarrollo para PawMedic en Supabase
-- Crea los usuarios iniciales de prueba en auth.users y sus perfiles correspondientes en public.profiles

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Usuario Veterinaria (VETERINARY_BUSINESS)
INSERT INTO auth.users (
    id,
    instance_id,
    aud,
    role,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_app_meta_data,
    raw_user_meta_data,
    is_super_admin,
    created_at,
    updated_at
) VALUES (
    'a1111111-1111-1111-1111-111111111111',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'veterinaria@pawsmedic.com',
    crypt('VetPaws2026!', gen_salt('bf')),
    now(),
    '{"provider": "email", "providers": ["email"]}',
    '{"display_name": "MVZ Dr. Carlos Ramos"}',
    false,
    now(),
    now()
) ON CONFLICT (id) DO NOTHING;

-- 2. Usuario Superadmin (SUPERADMIN)
INSERT INTO auth.users (
    id,
    instance_id,
    aud,
    role,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_app_meta_data,
    raw_user_meta_data,
    is_super_admin,
    created_at,
    updated_at
) VALUES (
    'b2222222-2222-2222-2222-222222222222',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'admin@pawsmedic.com',
    crypt('AdminPaws2026!', gen_salt('bf')),
    now(),
    '{"provider": "email", "providers": ["email"]}',
    '{"display_name": "Administrador Central PawsMedic"}',
    false,
    now(),
    now()
) ON CONFLICT (id) DO NOTHING;

-- 3. Inserción / Actualización en la tabla pública profiles
INSERT INTO public.profiles (id, email, display_name, role)
VALUES
    ('a1111111-1111-1111-1111-111111111111', 'veterinaria@pawsmedic.com', 'MVZ Dr. Carlos Ramos', 'VETERINARY_BUSINESS'),
    ('b2222222-2222-2222-2222-222222222222', 'admin@pawsmedic.com', 'Administrador Central PawsMedic', 'SUPERADMIN')
ON CONFLICT (id) DO UPDATE SET
    role = EXCLUDED.role,
    display_name = EXCLUDED.display_name,
    email = EXCLUDED.email;
