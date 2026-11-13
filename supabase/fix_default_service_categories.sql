-- Apply once in Supabase SQL Editor. Existing services are deselected once,
-- and future clinics receive the same catalog with all services deselected.
begin;

do $$
begin
    if not exists (
        select 1 from pg_attribute
        where attrelid = 'public.services'::regclass
          and attname = 'category' and attnum > 0 and not attisdropped
    ) then
        raise exception 'public.services.category does not exist in this database';
    end if;
    if not exists (
        select 1 from pg_trigger
        where tgrelid = 'public.tenants'::regclass
          and tgname = 'trg_create_default_services'
          and tgfoid = to_regprocedure('public.clone_default_services_for_tenant()')
          and not tgisinternal
    ) then
        raise exception 'the default services tenant trigger was not found';
    end if;
end;
$$;

alter table public.services alter column category set default 'otros';

create or replace function public.clone_default_services_for_tenant()
returns trigger
language plpgsql
as $$
begin
    insert into public.services (tenant_id, name, description, price_cents, duration_minutes, active, category)
    values
        (new.id, 'Consulta General', 'Revisión médica general, auscultación y valoración del estado de salud de la mascota.', 30000, 30, false, 'consulta'),
        (new.id, 'Vacunación (Dosis Individual)', 'Aplicación de vacuna polivalente, antirrábica o específica según el calendario de inmunización.', 35000, 15, false, 'vacunacion'),
        (new.id, 'Desparasitación Interna y Externa', 'Administración de tratamiento oral o tópica contra parásitos gastrointestinales y pulgas/garrapatas.', 20000, 15, false, 'desparasitacion'),
        (new.id, 'Esterilización / Ovariohisterectomía (Perro)', 'Cirugía de control natal para caninos bajo anestesia general y monitoreo.', 120000, 90, false, 'cirugia'),
        (new.id, 'Esterilización / Orquidectomía (Perro)', 'Cirugía de castración para caninos machos.', 95000, 60, false, 'cirugia'),
        (new.id, 'Esterilización (Gato / Gata)', 'Cirugía de control natal para felinos.', 80000, 60, false, 'cirugia'),
        (new.id, 'Limpieza Dental (Profilaxis)', 'Eliminación de sarro mediante ultrasonido dental y pulido bajo sedación.', 75000, 45, false, 'otros'),
        (new.id, 'Corte de Pelo y Baño (Estética)', 'Servicio de peluquería, baño medicado o cosmético, secado y corte de uñas.', 40000, 60, false, 'estetica'),
        (new.id, 'Baño Medicado / Dermatológico', 'Baño especial con fórmulas prescritas para tratar afecciones en la piel.', 30000, 40, false, 'estetica'),
        (new.id, 'Corte de Uñas', 'Servicio rápido de mantenimiento e higiene básica.', 15000, 15, false, 'estetica'),
        (new.id, 'Limpieza de Oídos', 'Servicio rápido de mantenimiento e higiene básica.', 15000, 15, false, 'estetica'),
        (new.id, 'Radiografía Digital', 'Estudio de imagenología para diagnóstico de fracturas, problemas pulmonares o torácicos.', 50000, 30, false, 'diagnostico'),
        (new.id, 'Ultrasonido Abdominal', 'Estudio ecográfico para revisión de órganos internos y gestación.', 60000, 30, false, 'diagnostico'),
        (new.id, 'Análisis de Sangre (Biometría Hemática)', 'Prueba de laboratorio para evaluar glóbulos rojos, blancos y plaquetas.', 45000, 20, false, 'diagnostico'),
        (new.id, 'Perfil Bioquímico Sanguíneo', 'Evaluación de la función renal, hepática y niveles de glucosa en sangre.', 70000, 20, false, 'diagnostico'),
        (new.id, 'Prueba Rápida (Parvovirus / Distemper)', 'Test de diagnóstico rápido para detección de enfermedades virales en caninos.', 35000, 15, false, 'diagnostico'),
        (new.id, 'Prueba Rápida (Leucemia / SIDA Felino)', 'Test de diagnóstico para retrovirus en gatos.', 40000, 15, false, 'diagnostico'),
        (new.id, 'Hospitalización (Por día)', 'Estancia clínica con monitoreo de signos vitales y aplicación de sueros o tratamientos.', 50000, 1440, false, 'otros'),
        (new.id, 'Curación de Heridas y Vendajes', 'Limpieza, desinfección y sutura menor o cambio de vendajes.', 25000, 30, false, 'otros'),
        (new.id, 'Certificado Médico de Viaje / Salud', 'Revisión y emisión de documento oficial de salud para traslado de mascotas.', 25000, 20, false, 'consulta'),
        (new.id, 'Consulta de Especialidad / Urgencia', 'Atención médica prioritaria fuera de horario o enfocada en áreas complejas.', 60000, 45, false, 'consulta');

    return new;
end;
$$;

create table if not exists public.pawmedic_setup_migrations (
    name text primary key,
    applied_at timestamptz not null default now()
);
alter table public.pawmedic_setup_migrations enable row level security;
revoke all on public.pawmedic_setup_migrations from public, anon, authenticated;

do $$
declare
    v_applied text;
begin
    insert into public.pawmedic_setup_migrations (name)
    values ('deselect_existing_services_20261111')
    on conflict do nothing
    returning name into v_applied;
    if v_applied is not null then
        update public.services set active = false where active;
    end if;
end;
$$;

alter table public.business_schedules enable row level security;
drop policy if exists schedules_clinic_manage on public.business_schedules;
create policy schedules_clinic_manage on public.business_schedules for all to authenticated
using (exists (
    select 1 from public.profiles p
    where p.id = auth.uid() and p.role = 'VETERINARY_BUSINESS'
      and p.tenant_id = business_schedules.tenant_id
))
with check (exists (
    select 1 from public.profiles p
    where p.id = auth.uid() and p.role = 'VETERINARY_BUSINESS'
      and p.tenant_id = business_schedules.tenant_id
));

alter table public.services enable row level security;
drop policy if exists services_clinic_manage on public.services;
create policy services_clinic_manage on public.services for all to authenticated
using (exists (
    select 1 from public.profiles p
    where p.id = auth.uid() and p.role = 'VETERINARY_BUSINESS'
      and p.tenant_id = services.tenant_id
))
with check (exists (
    select 1 from public.profiles p
    where p.id = auth.uid() and p.role = 'VETERINARY_BUSINESS'
      and p.tenant_id = services.tenant_id
));
grant select, insert, update, delete on public.business_schedules to authenticated;
grant select, insert, update, delete on public.services to authenticated;
notify pgrst, 'reload schema';

commit;
