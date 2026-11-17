-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.profiles (
  id uuid NOT NULL,
  role USER-DEFINED NOT NULL DEFAULT 'USER'::profile_role,
  first_name text NOT NULL DEFAULT ''::text,
  last_name text NOT NULL DEFAULT ''::text,
  phone text,
  display_name text NOT NULL DEFAULT ''::text,
  email text NOT NULL DEFAULT ''::text,
  tenant_id uuid,
  avatar_url text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  username text NOT NULL DEFAULT ''::text,
  CONSTRAINT profiles_pkey PRIMARY KEY (id),
  CONSTRAINT profiles_tenant_fk FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id)
);
CREATE TABLE public.tenants (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  owner_id uuid,
  name character varying NOT NULL DEFAULT ''::character varying,
  address character varying NOT NULL DEFAULT ''::character varying,
  city character varying NOT NULL DEFAULT ''::character varying,
  phone character varying NOT NULL DEFAULT ''::character varying,
  operational_status character varying NOT NULL DEFAULT 'ACTIVE'::character varying,
  email text NOT NULL DEFAULT ''::text,
  status USER-DEFINED NOT NULL DEFAULT 'PENDING'::tenant_status,
  created_by uuid,
  time_zone text NOT NULL DEFAULT 'America/Mexico_City'::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT tenants_pkey PRIMARY KEY (id),
  CONSTRAINT tenants_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id)
);
CREATE TABLE public.business_applications (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  applicant_id uuid NOT NULL,
  business_name text NOT NULL,
  address text NOT NULL,
  city text NOT NULL,
  status USER-DEFINED NOT NULL DEFAULT 'PENDING'::application_status,
  tax_id text NOT NULL DEFAULT ''::text,
  contact_phone text NOT NULL DEFAULT ''::text,
  veterinarian_name text NOT NULL DEFAULT ''::text,
  professional_license text NOT NULL DEFAULT ''::text,
  contact_email text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  veterinarian_in_charge text,
  CONSTRAINT business_applications_pkey PRIMARY KEY (id),
  CONSTRAINT business_applications_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES public.profiles(id)
);
CREATE TABLE public.species (
  id integer NOT NULL DEFAULT nextval('species_id_seq'::regclass),
  name character varying NOT NULL,
  CONSTRAINT species_pkey PRIMARY KEY (id)
);
CREATE TABLE public.breeds (
  id integer NOT NULL DEFAULT nextval('breeds_id_seq'::regclass),
  name character varying NOT NULL,
  species_id integer NOT NULL,
  CONSTRAINT breeds_pkey PRIMARY KEY (id),
  CONSTRAINT breeds_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id)
);
CREATE TABLE public.product_types (
  id integer NOT NULL DEFAULT nextval('product_types_id_seq'::regclass),
  name character varying NOT NULL,
  CONSTRAINT product_types_pkey PRIMARY KEY (id)
);
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
  CONSTRAINT products_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT products_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.product_types(id)
);
CREATE TABLE public.pets (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL,
  name character varying NOT NULL DEFAULT ''::character varying,
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
  species_id integer,
  species text,
  breed text,
  CONSTRAINT pets_pkey PRIMARY KEY (id),
  CONSTRAINT pets_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id),
  CONSTRAINT pets_breed_id_fkey FOREIGN KEY (breed_id) REFERENCES public.breeds(id),
  CONSTRAINT pets_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id)
);
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
  CONSTRAINT services_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);
CREATE TABLE public.service_species (
  service_id uuid NOT NULL,
  species_id integer NOT NULL,
  CONSTRAINT service_species_pkey PRIMARY KEY (service_id, species_id),
  CONSTRAINT service_species_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id),
  CONSTRAINT service_species_species_id_fkey FOREIGN KEY (species_id) REFERENCES public.species(id)
);
CREATE TABLE public.business_schedules (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  day_of_week smallint NOT NULL,
  open_time time without time zone NOT NULL,
  close_time time without time zone NOT NULL,
  is_active boolean DEFAULT true,
  weekday smallint DEFAULT day_of_week,
  opens_at time without time zone DEFAULT open_time,
  closes_at time without time zone DEFAULT close_time,
  CONSTRAINT business_schedules_pkey PRIMARY KEY (id),
  CONSTRAINT business_schedules_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);
CREATE TABLE public.inventory (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  product_id integer NOT NULL,
  stock_min integer NOT NULL DEFAULT 0,
  updated_date date NOT NULL DEFAULT CURRENT_DATE,
  CONSTRAINT inventory_pkey PRIMARY KEY (id),
  CONSTRAINT inventory_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT inventory_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id)
);
CREATE TABLE public.appointments (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  pet_id uuid NOT NULL,
  owner_id uuid NOT NULL,
  tenant_id uuid NOT NULL,
  service_id uuid NOT NULL,
  status USER-DEFINED NOT NULL DEFAULT 'REQUESTED'::appointment_status,
  booked_by uuid,
  starts_at timestamp with time zone NOT NULL DEFAULT now(),
  ends_at timestamp with time zone NOT NULL DEFAULT now(),
  idempotency_key uuid,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT appointments_pkey PRIMARY KEY (id),
  CONSTRAINT appointments_booked_by_fkey FOREIGN KEY (booked_by) REFERENCES auth.users(id),
  CONSTRAINT appointments_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT appointments_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles(id),
  CONSTRAINT appointments_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT appointments_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id)
);
CREATE TABLE public.availability_blocks (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  tenant_id uuid NOT NULL,
  reason text NOT NULL DEFAULT ''::text,
  starts_at timestamp with time zone NOT NULL,
  ends_at timestamp with time zone NOT NULL,
  created_by uuid,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT availability_blocks_pkey PRIMARY KEY (id),
  CONSTRAINT availability_blocks_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT availability_blocks_created_by_fkey FOREIGN KEY (created_by) REFERENCES auth.users(id)
);
CREATE TABLE public.medical_records (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  pet_id uuid NOT NULL,
  weight_grams integer NOT NULL,
  record_date date NOT NULL,
  record_time time without time zone NOT NULL,
  consultation_reason character varying NOT NULL,
  symptoms character varying NOT NULL,
  diagnosis character varying NOT NULL,
  treatment character varying NOT NULL,
  medication character varying NOT NULL,
  dosage character varying NOT NULL,
  observations character varying NOT NULL,
  next_review_date date,
  veterinarian_id uuid,
  tenant_id uuid,
  authored_by uuid,
  title text NOT NULL DEFAULT ''::text,
  content text NOT NULL DEFAULT ''::text,
  attachments jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT medical_records_pkey PRIMARY KEY (id),
  CONSTRAINT medical_records_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT medical_records_veterinarian_id_fkey FOREIGN KEY (veterinarian_id) REFERENCES public.profiles(id),
  CONSTRAINT medical_records_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT medical_records_authored_by_fkey FOREIGN KEY (authored_by) REFERENCES auth.users(id)
);
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
  CONSTRAINT businesses_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT businesses_submitted_by_fkey FOREIGN KEY (submitted_by) REFERENCES public.profiles(id),
  CONSTRAINT businesses_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.profiles(id)
);
CREATE TABLE public.clinical_consultations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  tenant_id uuid,
  pet_id uuid NOT NULL,
  veterinarian_id uuid NOT NULL,
  appointment_id uuid,
  reason text NOT NULL DEFAULT ''::text,
  anamnesis text NOT NULL DEFAULT ''::text,
  vital_signs jsonb NOT NULL DEFAULT '{}'::jsonb,
  systems_exploration jsonb NOT NULL DEFAULT '{}'::jsonb,
  problem_list jsonb NOT NULL DEFAULT '[]'::jsonb,
  master_problem_list jsonb NOT NULL DEFAULT '[]'::jsonb,
  differential_diagnoses text NOT NULL DEFAULT ''::text,
  definitive_diagnosis text NOT NULL DEFAULT ''::text,
  diagnostic_plan text NOT NULL DEFAULT ''::text,
  treatment_plan text NOT NULL DEFAULT ''::text,
  observations text NOT NULL DEFAULT ''::text,
  next_appointment_date date,
  status USER-DEFINED NOT NULL DEFAULT 'COMPLETED'::consultation_status,
  occurred_at timestamp with time zone NOT NULL DEFAULT now(),
  idempotency_key uuid UNIQUE,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT clinical_consultations_pkey PRIMARY KEY (id),
  CONSTRAINT clinical_consultations_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
  CONSTRAINT clinical_consultations_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT clinical_consultations_veterinarian_id_fkey FOREIGN KEY (veterinarian_id) REFERENCES public.profiles(id),
  CONSTRAINT clinical_consultations_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id)
);
CREATE TABLE public.pet_backgrounds (
  pet_id uuid NOT NULL,
  last_deworming_date date,
  deworming_product text NOT NULL DEFAULT ''::text,
  diet text NOT NULL DEFAULT ''::text,
  allergies text NOT NULL DEFAULT ''::text,
  past_diseases text NOT NULL DEFAULT ''::text,
  past_surgeries text NOT NULL DEFAULT ''::text,
  family_history text NOT NULL DEFAULT ''::text,
  reproductive_status text NOT NULL DEFAULT ''::text,
  habitat text NOT NULL DEFAULT ''::text,
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_by uuid,
  CONSTRAINT pet_backgrounds_pkey PRIMARY KEY (pet_id),
  CONSTRAINT pet_backgrounds_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT pet_backgrounds_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.profiles(id)
);
CREATE TABLE public.prescriptions (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  consultation_id uuid,
  pet_id uuid NOT NULL,
  drug_name text NOT NULL,
  dosage text NOT NULL DEFAULT ''::text,
  unit text NOT NULL DEFAULT ''::text,
  route text NOT NULL DEFAULT 'Oral'::text,
  frequency text NOT NULL DEFAULT 'Cada 12h'::text,
  duration text NOT NULL DEFAULT '7 días'::text,
  start_date date,
  end_date date,
  is_administered_in_consultation boolean NOT NULL DEFAULT false,
  prescribing_vet_id uuid,
  notes text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT prescriptions_pkey PRIMARY KEY (id),
  CONSTRAINT prescriptions_consultation_id_fkey FOREIGN KEY (consultation_id) REFERENCES public.clinical_consultations(id),
  CONSTRAINT prescriptions_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT prescriptions_prescribing_vet_id_fkey FOREIGN KEY (prescribing_vet_id) REFERENCES public.profiles(id)
);
CREATE TABLE public.vaccinations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  pet_id uuid NOT NULL,
  consultation_id uuid,
  vaccine_name text NOT NULL,
  batch_number text NOT NULL DEFAULT ''::text,
  applied_date date NOT NULL DEFAULT CURRENT_DATE,
  next_due_date date,
  applied_by uuid,
  notes text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT vaccinations_pkey PRIMARY KEY (id),
  CONSTRAINT vaccinations_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT vaccinations_consultation_id_fkey FOREIGN KEY (consultation_id) REFERENCES public.clinical_consultations(id),
  CONSTRAINT vaccinations_applied_by_fkey FOREIGN KEY (applied_by) REFERENCES public.profiles(id)
);
CREATE TABLE public.dewormings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  pet_id uuid NOT NULL,
  consultation_id uuid,
  product_name text NOT NULL,
  dosage text NOT NULL DEFAULT ''::text,
  applied_date date NOT NULL DEFAULT CURRENT_DATE,
  next_due_date date,
  applied_by uuid,
  notes text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT dewormings_pkey PRIMARY KEY (id),
  CONSTRAINT dewormings_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id),
  CONSTRAINT dewormings_consultation_id_fkey FOREIGN KEY (consultation_id) REFERENCES public.clinical_consultations(id),
  CONSTRAINT dewormings_applied_by_fkey FOREIGN KEY (applied_by) REFERENCES public.profiles(id)
);
CREATE TABLE public.clinical_studies (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  consultation_id uuid,
  pet_id uuid NOT NULL,
  study_type text NOT NULL DEFAULT 'LABORATORY'::text,
  name text NOT NULL,
  status text NOT NULL DEFAULT 'COMPLETED'::text,
  requested_at timestamp with time zone NOT NULL DEFAULT now(),
  results_notes text NOT NULL DEFAULT ''::text,
  attachment_urls jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT clinical_studies_pkey PRIMARY KEY (id),
  CONSTRAINT clinical_studies_consultation_id_fkey FOREIGN KEY (consultation_id) REFERENCES public.clinical_consultations(id),
  CONSTRAINT clinical_studies_pet_id_fkey FOREIGN KEY (pet_id) REFERENCES public.pets(id)
);
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
  CONSTRAINT audit_logs_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.profiles(id),
  CONSTRAINT audit_logs_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);