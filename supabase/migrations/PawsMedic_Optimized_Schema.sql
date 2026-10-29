-- PawsMedic - Optimized Database Schema
-- WARNING: This schema is ready for execution. Tables are ordered to prevent dependency issues.

-- 1. PROFILES (Users)
CREATE TABLE public.profiles (
  id uuid NOT NULL,
  role USER-DEFINED NOT NULL DEFAULT 'USER'::profile_role,
  first_name text NOT NULL DEFAULT ''::text,
  last_name text NOT NULL DEFAULT ''::text,
  username text NOT NULL DEFAULT ''::text,
  phone text,
  display_name text NOT NULL DEFAULT ''::text,
  email text NOT NULL DEFAULT ''::text,
  tenant_id uuid,
  avatar_url text, -- EN: Standardized from photo_url
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT profiles_pkey PRIMARY KEY (id),
  CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE, -- FIX: Account deletion cascade
  CONSTRAINT profiles_tenant_fk FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE SET NULL
);

-- 2. TENANTS (Clinics)
CREATE TABLE public.tenants (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  owner_id uuid,
  name character varying NOT NULL DEFAULT ''::character varying,
  address character varying NOT NULL DEFAULT ''::character varying,
  city character varying NOT NULL DEFAULT ''::character varying, -- EN: Standardized from municipality
  phone character varying NOT NULL DEFAULT ''::character varying,
  operational_status character varying NOT NULL DEFAULT 'ACTIVE'::character varying,
  email text NOT NULL DEFAULT ''::text,
  status USER-DEFINED NOT NULL DEFAULT 'PENDING'::tenant_status,
  created_by uuid,
  time_zone text NOT NULL DEFAULT 'UTC'::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT tenants_pkey PRIMARY KEY (id),
  CONSTRAINT tenants_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id) ON DELETE SET NULL
);

-- 3. BUSINESS APPLICATIONS
CREATE TABLE public.business_applications (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  applicant_id uuid NOT NULL,
  business_name text NOT NULL,
  address text NOT NULL,
  city text NOT NULL, -- EN: Standardized from municipality
  status USER-DEFINED NOT NULL DEFAULT 'PENDING'::application_status,
  tax_id text NOT NULL DEFAULT ''::text,
  contact_phone text NOT NULL DEFAULT ''::text,
  veterinarian_name text NOT NULL DEFAULT ''::text,
  professional_license text NOT NULL DEFAULT ''::text,
  contact_email text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  veterinarian_in_charge text,
  CONSTRAINT business_applications_pkey PRIMARY KEY (id),
  CONSTRAINT business_applications_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES public.profiles(id) ON DELETE CASCADE
);

-- 4. SPECIES
CREATE TABLE public.species (
  id integer NOT NULL DEFAULT nextval('species_id_seq'::regclass),
  name character varying NOT NULL,
  CONSTRAINT species_pkey PRIMARY KEY (id)
);

-- 5. BREEDS
CREATE TABLE public.breeds (
  id integer NOT NULL DEFAULT nextval('breeds_id_seq'::regclass),
  name character varying NOT NULL,
  species_id integer NOT NULL,
  CONSTRAINT breeds_pkey PRIMARY KEY (id),
  CONSTRAINT breeds_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id) ON DELETE CASCADE
);

-- 6. PRODUCT TYPES
CREATE TABLE public.product_types (
  id integer NOT NULL DEFAULT nextval('product_types_id_seq'::regclass),
  name character varying NOT NULL,
  CONSTRAINT product_types_pkey PRIMARY KEY (id)
);

-- 7. PRODUCTS
CREATE TABLE public.products (
  id integer NOT NULL DEFAULT nextval('products_id_seq'::regclass),
  tenant_id uuid,
  name character varying NOT NULL,
  type_id integer,
  description text NOT NULL DEFAULT ''::text,
  price_cents integer NOT NULL DEFAULT 0 CHECK (price_cents >= 0),
  sku character varying,
  active boolean NOT NULL DEFAULT true,
  stock_quantity integer NOT NULL DEFAULT 0,
  image_url text NOT NULL DEFAULT ''::text,
  published boolean NOT NULL DEFAULT false,
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT products_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT products_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.product_types(id) ON DELETE SET NULL
);

-- 8. PETS
CREATE TABLE public.pets (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL,
  name character varying NOT NULL DEFAULT ''::character varying,
  species_id integer NOT NULL, -- FIX: Removed duplicate text fields, strict relational binding
  breed_id integer,
  date_of_birth date,
  allergies text DEFAULT 'None'::text,
  sex character varying,
  weight_grams integer,
  photo_url text,
  is_insured boolean DEFAULT false,
  medical_id text,
  notes text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT pets_pkey PRIMARY KEY (id),
  CONSTRAINT pets_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id) ON DELETE CASCADE,
  CONSTRAINT pets_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id) ON DELETE RESTRICT,
  CONSTRAINT pets_breed_id_fkey FOREIGN KEY (breed_id) REFERENCES public.breeds(id) ON DELETE SET NULL
);

-- 9. SERVICES
CREATE TABLE public.services (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  name character varying NOT NULL,
  description text NOT NULL DEFAULT ''::text,
  price_cents integer NOT NULL DEFAULT 0 CHECK (price_cents >= 0),
  duration_minutes integer NOT NULL DEFAULT 30,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT services_pkey PRIMARY KEY (id),
  CONSTRAINT services_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE
);

-- 10. SERVICE SPECIES
CREATE TABLE public.service_species (
  service_id uuid NOT NULL,
  species_id integer NOT NULL,
  CONSTRAINT service_species_pkey PRIMARY KEY (service_id, species_id),
  CONSTRAINT service_species_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id) ON DELETE CASCADE,
  CONSTRAINT service_species_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id) ON DELETE CASCADE
);

-- 11. BUSINESS SCHEDULES
CREATE TABLE public.business_schedules (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  weekday smallint NOT NULL CHECK (weekday >= 1 AND weekday <= 7),
  opens_at time without time zone NOT NULL,
  closes_at time without time zone NOT NULL,
  CONSTRAINT business_schedules_pkey PRIMARY KEY (id),
  CONSTRAINT business_schedules_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE
);

-- 12. INVENTORY
CREATE TABLE public.inventory (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  product_id integer NOT NULL,
  stock_min integer NOT NULL DEFAULT 0,
  updated_date date NOT NULL DEFAULT CURRENT_DATE,
  CONSTRAINT inventory_pkey PRIMARY KEY (id),
  CONSTRAINT inventory_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT inventory_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE
);

-- 13. APPOINTMENTS
CREATE TABLE public.appointments (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  pet_id uuid NOT NULL,
  owner_id uuid NOT NULL,
  tenant_id uuid NOT NULL,
  service_id uuid NOT NULL,
  status USER-DEFINED NOT NULL DEFAULT 'REQUESTED'::appointment_status,
  booked_by uuid,
  starts_at timestamp with time zone NOT NULL, -- FIX: Unified timing system, dropped separate date/time
  ends_at timestamp with time zone NOT NULL,
  idempotency_key uuid,
  notes text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT appointments_pkey PRIMARY KEY (id),
  CONSTRAINT appointments_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id) ON DELETE CASCADE,
  CONSTRAINT appointments_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id) ON DELETE CASCADE,
  CONSTRAINT appointments_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT appointments_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id) ON DELETE CASCADE,
  CONSTRAINT appointments_booked_by_fkey FOREIGN KEY (booked_by) REFERENCES auth.users(id) ON DELETE SET NULL
);

-- 14. AVAILABILITY BLOCKS
CREATE TABLE public.availability_blocks (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  reason text NOT NULL DEFAULT ''::text,
  starts_at timestamp with time zone NOT NULL,
  ends_at timestamp with time zone NOT NULL,
  created_by uuid,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT availability_blocks_pkey PRIMARY KEY (id),
  CONSTRAINT availability_blocks_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT availability_blocks_created_by_fkey FOREIGN KEY (created_by) REFERENCES auth.users(id) ON DELETE SET NULL
);

-- 15. MEDICAL RECORDS
CREATE TABLE public.medical_records (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  pet_id uuid NOT NULL,
  weight_grams integer NOT NULL,
  record_date date NOT NULL,
  record_time time without time zone NOT NULL,
  consultation_reason text NOT NULL,
  symptoms text NOT NULL,
  diagnosis text NOT NULL,
  treatment text NOT NULL,
  medication text NOT NULL,
  dosage text NOT NULL,
  observations text NOT NULL,
  next_review_date date,
  veterinarian_id uuid,
  tenant_id uuid,
  authored_by uuid,
  title text NOT NULL DEFAULT ''::text,
  content text NOT NULL DEFAULT ''::text,
  attachments jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT medical_records_pkey PRIMARY KEY (id),
  CONSTRAINT medical_records_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id) ON DELETE CASCADE,
  CONSTRAINT medical_records_veterinarian_id_fkey FOREIGN KEY (veterinarian_id) REFERENCES public.profiles(id) ON DELETE SET NULL,
  CONSTRAINT medical_records_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT medical_records_authored_by_fkey FOREIGN KEY (authored_by) REFERENCES auth.users(id) ON DELETE SET NULL
);

-- 16. BUSINESSES
CREATE TABLE public.businesses (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  tenant_id uuid UNIQUE,
  submitted_by uuid,
  legal_name text NOT NULL DEFAULT ''::text,
  public_name text NOT NULL DEFAULT ''::text,
  status USER-DEFINED NOT NULL DEFAULT 'PENDING'::business_status,
  approved_by uuid,
  approved_at timestamp with time zone,
  applicant_name text NOT NULL DEFAULT ''::text,
  tax_id text NOT NULL DEFAULT ''::text,
  email text NOT NULL DEFAULT ''::text,
  phone text NOT NULL DEFAULT ''::text,
  address text NOT NULL DEFAULT ''::text,
  license_number text NOT NULL DEFAULT ''::text,
  decision_reason text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT businesses_pkey PRIMARY KEY (id),
  CONSTRAINT businesses_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT businesses_submitted_by_fkey FOREIGN KEY (submitted_by) REFERENCES public.profiles(id) ON DELETE SET NULL,
  CONSTRAINT businesses_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.profiles(id) ON DELETE SET NULL
);

-- 17. CLINICAL CONSULTATIONS
CREATE TABLE public.clinical_consultations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  tenant_id uuid,
  pet_id uuid,
  veterinarian_id uuid,
  appointment_id uuid,
  reason text NOT NULL DEFAULT ''::text,
  observations text NOT NULL DEFAULT ''::text,
  diagnosis text NOT NULL DEFAULT ''::text,
  plan text NOT NULL DEFAULT ''::text,
  status USER-DEFINED NOT NULL DEFAULT 'DRAFT'::consultation_status,
  occurred_at timestamp with time zone NOT NULL DEFAULT now(),
  idempotency_key uuid UNIQUE,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT clinical_consultations_pkey PRIMARY KEY (id),
  CONSTRAINT clinical_consultations_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE,
  CONSTRAINT clinical_consultations_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id) ON DELETE CASCADE,
  CONSTRAINT clinical_consultations_veterinarian_id_fkey FOREIGN KEY (veterinarian_id) REFERENCES public.profiles(id) ON DELETE SET NULL,
  CONSTRAINT clinical_consultations_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id) ON DELETE SET NULL
);

-- 18. AUDIT LOGS
CREATE TABLE public.audit_logs (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  actor_id uuid,
  tenant_id uuid,
  action text NOT NULL DEFAULT ''::text,
  entity_type text NOT NULL DEFAULT ''::text,
  entity_id uuid,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT audit_logs_pkey PRIMARY KEY (id),
  CONSTRAINT audit_logs_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.profiles(id) ON DELETE SET NULL,
  CONSTRAINT audit_logs_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE
);

-- 19. AUTH PROFILE SYNCHRONIZATION
-- Copies registration metadata from auth.users into the public profile.
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_first_name text := COALESCE(NEW.raw_user_meta_data ->> 'first_name', '');
  v_last_name text := COALESCE(NEW.raw_user_meta_data ->> 'last_name', '');
  v_username text := COALESCE(NEW.raw_user_meta_data ->> 'username', '');
  v_phone text := COALESCE(NEW.raw_user_meta_data ->> 'phone', '');
  v_display_name text := TRIM(CONCAT(v_first_name, ' ', v_last_name));
BEGIN
  INSERT INTO public.profiles (
    id,
    role,
    first_name,
    last_name,
    username,
    phone,
    display_name,
    email
  )
  VALUES (
    NEW.id,
    'USER'::public.profile_role,
    v_first_name,
    v_last_name,
    v_username,
    v_phone,
    v_display_name,
    COALESCE(NEW.email, '')
  )
  ON CONFLICT (id) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    username = EXCLUDED.username,
    phone = EXCLUDED.phone,
    display_name = EXCLUDED.display_name,
    email = EXCLUDED.email,
    updated_at = now();

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW
EXECUTE FUNCTION public.handle_new_user();

-- Repairs profiles created before this synchronization was installed.
UPDATE public.profiles AS p
SET
  first_name = COALESCE(u.raw_user_meta_data ->> 'first_name', p.first_name),
  last_name = COALESCE(u.raw_user_meta_data ->> 'last_name', p.last_name),
  username = COALESCE(u.raw_user_meta_data ->> 'username', p.username),
  phone = COALESCE(u.raw_user_meta_data ->> 'phone', p.phone),
  display_name = COALESCE(
    NULLIF(TRIM(CONCAT(
      u.raw_user_meta_data ->> 'first_name',
      ' ',
      u.raw_user_meta_data ->> 'last_name'
    )), ''),
    p.display_name
  ),
  email = COALESCE(u.email, p.email),
  updated_at = now()
FROM auth.users AS u
WHERE u.id = p.id;
