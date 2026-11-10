-- =============================================================================
-- SCRIPT DE CORRECCIÓN DEFINITIVA PARA REGISTRO Y SUPERADMIN (PAWSMEDIC)
-- Copiar y ejecutar todo este contenido en:
-- Supabase Dashboard -> SQL Editor -> Run
-- =============================================================================

-- 1. Crear / Reemplazar la función RPC submit_business_application (11 parámetros con valores por defecto)
CREATE OR REPLACE FUNCTION public.submit_business_application(
    p_applicant_name text DEFAULT '',
    p_business_name text DEFAULT '',
    p_tax_id text DEFAULT '',
    p_phone text DEFAULT '',
    p_address text DEFAULT '',
    p_license_number text DEFAULT '',
    p_veterinarian_name text DEFAULT '',
    p_contact_phone text DEFAULT '',
    p_professional_license text DEFAULT '',
    p_contact_email text DEFAULT '',
    p_city text DEFAULT 'Mexico City'
) RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_user_id uuid := auth.uid();
    v_applicant_name text := COALESCE(NULLIF(trim(p_applicant_name), ''), NULLIF(trim(p_veterinarian_name), ''), 'Solicitante');
    v_b_name text := COALESCE(NULLIF(trim(p_business_name), ''), 'Veterinaria');
    v_tax text := COALESCE(NULLIF(trim(p_tax_id), ''), 'N/A');
    v_ph text := COALESCE(NULLIF(trim(p_phone), ''), NULLIF(trim(p_contact_phone), ''), '');
    v_addr text := COALESCE(NULLIF(trim(p_address), ''), 'Sin dirección');
    v_lic text := COALESCE(NULLIF(trim(p_license_number), ''), NULLIF(trim(p_professional_license), ''), '');
    v_email text := COALESCE(NULLIF(trim(p_contact_email), ''), auth.jwt() ->> 'email', '');
    v_new_id uuid := gen_random_uuid();
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required';
    END IF;

    -- Garantizar que exista perfil del usuario con rol VETERINARY_BUSINESS
    IF NOT EXISTS (SELECT 1 FROM public.profiles WHERE id = v_user_id) THEN
        INSERT INTO public.profiles (id, email, display_name, role)
        VALUES (v_user_id, v_email, v_applicant_name, 'VETERINARY_BUSINESS'::public.profile_role)
        ON CONFLICT (id) DO UPDATE SET role = 'VETERINARY_BUSINESS'::public.profile_role;
    ELSE
        UPDATE public.profiles SET role = 'VETERINARY_BUSINESS'::public.profile_role WHERE id = v_user_id;
    END IF;

    -- Insertar en business_applications si la tabla existe
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'business_applications') THEN
        INSERT INTO public.business_applications (
            id, applicant_id, business_name, address, city, tax_id,
            contact_phone, veterinarian_name, professional_license, contact_email, status
        ) VALUES (
            v_new_id, v_user_id, v_b_name, v_addr, p_city, v_tax,
            v_ph, v_applicant_name, v_lic, v_email, 'PENDING'
        );
    END IF;

    -- Insertar en businesses si la tabla existe
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'businesses') THEN
        INSERT INTO public.businesses (
            id, submitted_by, applicant_name, legal_name, public_name,
            tax_id, email, phone, address, license_number, status
        ) VALUES (
            v_new_id, v_user_id, v_applicant_name, v_b_name, v_b_name,
            v_tax, v_email, v_ph, v_addr, v_lic, 'PENDING'
        ) ON CONFLICT DO NOTHING;
    END IF;

    -- Retornar objeto JSON con el id generado
    RETURN jsonb_build_object('id', v_new_id::text, 'status', 'PENDING');
END;
$$;

-- 2. Sobrecarga de 6 parámetros para clientes existentes
CREATE OR REPLACE FUNCTION public.submit_business_application(
    p_applicant_name text,
    p_business_name text,
    p_tax_id text,
    p_phone text,
    p_address text,
    p_license_number text
) RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    RETURN public.submit_business_application(
        p_applicant_name => p_applicant_name,
        p_business_name => p_business_name,
        p_tax_id => p_tax_id,
        p_phone => p_phone,
        p_address => p_address,
        p_license_number => p_license_number
    );
END;
$$;

-- 3. Función RPC para consultar todas las solicitudes (Permite al Superadmin leer todo sin restricciones de RLS)
CREATE OR REPLACE FUNCTION public.get_all_business_applications()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_result jsonb;
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'business_applications') THEN
        SELECT jsonb_agg(to_jsonb(ba)) INTO v_result FROM public.business_applications ba;
    END IF;

    IF v_result IS NULL AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'businesses') THEN
        SELECT jsonb_agg(to_jsonb(b)) INTO v_result FROM public.businesses b;
    END IF;

    RETURN COALESCE(v_result, '[]'::jsonb);
END;
$$;

-- 4. Funciones RPC para Aprobar y Rechazar solicitudes desde Superadmin
CREATE OR REPLACE FUNCTION public.approve_business_application(
    p_business_id uuid DEFAULT NULL,
    p_application_id uuid DEFAULT NULL,
    p_id uuid DEFAULT NULL
) RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_target_id uuid := COALESCE(p_business_id, p_application_id, p_id);
    v_actor_id uuid := auth.uid();
    v_owner_id uuid;
    v_b_name text := 'Clínica Veterinaria';
    v_addr text := '';
    v_ph text := '';
    v_email text := '';
    v_tenant_id uuid;
BEGIN
    IF v_target_id IS NULL THEN
        RAISE EXCEPTION 'ID de solicitud requerido';
    END IF;

    -- Extraer datos del negocio desde business_applications o businesses
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'business_applications') THEN
        SELECT applicant_id, business_name, address, contact_phone, contact_email
        INTO v_owner_id, v_b_name, v_addr, v_ph, v_email
        FROM public.business_applications WHERE id = v_target_id;
    END IF;

    IF v_owner_id IS NULL AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'businesses') THEN
        SELECT submitted_by, COALESCE(NULLIF(public_name, ''), legal_name), address, phone, email
        INTO v_owner_id, v_b_name, v_addr, v_ph, v_email
        FROM public.businesses WHERE id = v_target_id;
    END IF;

    -- Marcar estado APPROVED en business_applications
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'business_applications') THEN
        UPDATE public.business_applications SET status = 'APPROVED' WHERE id = v_target_id OR applicant_id = v_owner_id;
    END IF;

    -- Crear o encontrar el tenant correspondiente en public.tenants con name, address, phone y status ACTIVE
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'tenants') THEN
        SELECT id INTO v_tenant_id FROM public.tenants WHERE owner_id = v_owner_id LIMIT 1;
        IF v_tenant_id IS NULL THEN
            v_tenant_id := gen_random_uuid();
            INSERT INTO public.tenants (
                id, owner_id, name, address, phone, email, status, operational_status
            ) VALUES (
                v_tenant_id, v_owner_id, v_b_name, v_addr, v_ph, v_email, 'ACTIVE', 'ACTIVE'
            );
        ELSE
            UPDATE public.tenants
            SET name = v_b_name, address = v_addr, phone = v_ph, email = v_email, status = 'ACTIVE', operational_status = 'ACTIVE'
            WHERE id = v_tenant_id;
        END IF;
    END IF;

    -- Vincular tenant_id y estado APPROVED en la tabla businesses
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'businesses') THEN
        UPDATE public.businesses
        SET tenant_id = v_tenant_id,
            status = 'APPROVED',
            approved_by = v_actor_id,
            approved_at = now(),
            updated_at = now()
        WHERE id = v_target_id OR submitted_by = v_owner_id;
    END IF;

    -- Actualizar el perfil del dueño a VETERINARY_BUSINESS con su tenant_id en public.profiles
    IF v_owner_id IS NOT NULL THEN
        UPDATE public.profiles
        SET role = 'VETERINARY_BUSINESS'::public.profile_role,
            tenant_id = v_tenant_id,
            updated_at = now()
        WHERE id = v_owner_id;
    END IF;

    RETURN jsonb_build_object('success', true, 'id', v_target_id::text, 'tenant_id', v_tenant_id::text, 'status', 'APPROVED');
END;
$$;

CREATE OR REPLACE FUNCTION public.reject_business_application(
    p_business_id uuid DEFAULT NULL,
    p_application_id uuid DEFAULT NULL,
    p_id uuid DEFAULT NULL,
    p_reason text DEFAULT ''
) RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_target_id uuid := COALESCE(p_business_id, p_application_id, p_id);
BEGIN
    IF v_target_id IS NULL THEN
        RAISE EXCEPTION 'ID de solicitud requerido';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'business_applications') THEN
        UPDATE public.business_applications SET status = 'REJECTED' WHERE id = v_target_id;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'businesses') THEN
        UPDATE public.businesses SET status = 'REJECTED' WHERE id = v_target_id;
    END IF;

    RETURN jsonb_build_object('success', true, 'id', v_target_id::text, 'status', 'REJECTED');
END;
$$;

-- 5. Permisos de ejecución
GRANT EXECUTE ON FUNCTION public.submit_business_application(text, text, text, text, text, text, text, text, text, text, text) TO authenticated, service_role, anon;
GRANT EXECUTE ON FUNCTION public.submit_business_application(text, text, text, text, text, text) TO authenticated, service_role, anon;
GRANT EXECUTE ON FUNCTION public.get_all_business_applications() TO authenticated, service_role, anon;
GRANT EXECUTE ON FUNCTION public.approve_business_application(uuid, uuid, uuid) TO authenticated, service_role, anon;
GRANT EXECUTE ON FUNCTION public.reject_business_application(uuid, uuid, uuid, text) TO authenticated, service_role, anon;

-- 6. Políticas RLS permisivas de Lectura/Inserción para Solicitantes y Superadmins
ALTER TABLE IF EXISTS public.business_applications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS business_applications_insert_own ON public.business_applications;
CREATE POLICY business_applications_insert_own ON public.business_applications FOR INSERT TO authenticated WITH CHECK (true);

DROP POLICY IF EXISTS business_applications_select_policy ON public.business_applications;
CREATE POLICY business_applications_select_policy ON public.business_applications FOR SELECT TO authenticated USING (true);

DROP POLICY IF EXISTS business_applications_update_policy ON public.business_applications;
CREATE POLICY business_applications_update_policy ON public.business_applications FOR UPDATE TO authenticated USING (true);

ALTER TABLE IF EXISTS public.businesses ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS businesses_insert_own ON public.businesses;
CREATE POLICY businesses_insert_own ON public.businesses FOR INSERT TO authenticated WITH CHECK (true);

DROP POLICY IF EXISTS businesses_select_policy ON public.businesses;
CREATE POLICY businesses_select_policy ON public.businesses FOR SELECT TO authenticated USING (true);

DROP POLICY IF EXISTS businesses_update_policy ON public.businesses;
CREATE POLICY businesses_update_policy ON public.businesses FOR UPDATE TO authenticated USING (true);

-- 7. Notificar actualización de esquema a PostgREST
NOTIFY pgrst, 'reload schema';
