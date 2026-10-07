do $$
begin
    if to_regtype('public.application_status') is null then
        execute 'create type public.application_status as enum (''PENDIENTE'', ''APROBADA'', ''RECHAZADA'')';
    end if;
end;
$$;

alter table public.profiles
    add column if not exists email text not null default '',
    add column if not exists display_name text not null default '',
    add column if not exists nombre text not null default '',
    add column if not exists apellidos text not null default '',
    add column if not exists num_tel text;

create table if not exists public.business_applications (
    id uuid primary key default gen_random_uuid(),
    applicant_id uuid not null references public.profiles(id) on delete cascade,
    nombre_negocio text not null,
    domicilio text not null,
    municipio text not null,
    estado public.application_status not null default 'PENDIENTE',
    created_at timestamptz not null default now()
);

alter table public.business_applications
    add column if not exists ruc text not null default '',
    add column if not exists telefono_contacto text not null default '',
    add column if not exists responsable_veterinario text not null default '',
    add column if not exists cedula_profesional text not null default '',
    add column if not exists correo_contacto text not null default '';

alter table public.profiles enable row level security;
alter table public.business_applications enable row level security;
grant select on public.profiles to authenticated;
grant select, insert on public.business_applications to authenticated;

create or replace function public.is_superadmin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.profiles
        where id = auth.uid()
          and role = 'SUPERADMIN'::public.profile_role
    );
$$;

revoke all on function public.is_superadmin() from public;
grant execute on function public.is_superadmin() to authenticated;

drop policy if exists "Crear mis solicitudes" on public.business_applications;
drop policy if exists "Ver mis solicitudes" on public.business_applications;
drop policy if exists business_applications_insert_own on public.business_applications;
drop policy if exists business_applications_select_own on public.business_applications;
drop policy if exists business_applications_superadmin_read on public.business_applications;

create policy business_applications_insert_own
on public.business_applications
for insert
to authenticated
with check (auth.uid() = applicant_id);

create policy business_applications_select_own
on public.business_applications
for select
to authenticated
using (auth.uid() = applicant_id);

create policy business_applications_superadmin_read
on public.business_applications
for select
to authenticated
using (public.is_superadmin());

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    metadata jsonb := coalesce(new.raw_user_meta_data, '{}'::jsonb);
    full_name text := trim(coalesce(metadata->>'full_name', ''));
    first_name text := coalesce(
        nullif(trim(metadata->>'nombre'), ''),
        split_part(trim(coalesce(metadata->>'full_name', '')), ' ', 1),
        ''
    );
    last_name text := coalesce(
        nullif(trim(metadata->>'apellidos'), ''),
        case
            when position(' ' in trim(coalesce(metadata->>'full_name', ''))) > 0
            then trim(substring(
                trim(coalesce(metadata->>'full_name', ''))
                from position(' ' in trim(coalesce(metadata->>'full_name', ''))) + 1
            ))
            else ''
        end,
        ''
    );
    complete_name text;
    clinic_name text;
    clinic_address text;
    municipality text;
begin
    complete_name := coalesce(
        nullif(full_name, ''),
        trim(concat_ws(' ', nullif(first_name, ''), nullif(last_name, ''))),
        ''
    );

    insert into public.profiles (
        id, email, display_name, role, nombre, apellidos, num_tel
    )
    values (
        new.id,
        coalesce(new.email, ''),
        complete_name,
        'USER',
        first_name,
        last_name,
        coalesce(nullif(trim(metadata->>'num_tel'), ''), '')
    )
    on conflict (id) do update
    set
        email = excluded.email,
        display_name = case
            when excluded.display_name <> '' then excluded.display_name
            else public.profiles.display_name
        end,
        nombre = case
            when excluded.nombre <> '' then excluded.nombre
            else public.profiles.nombre
        end,
        apellidos = case
            when excluded.apellidos <> '' then excluded.apellidos
            else public.profiles.apellidos
        end,
        num_tel = case
            when excluded.num_tel <> '' then excluded.num_tel
            else public.profiles.num_tel
        end;

    if metadata->>'registration_type' = 'VETERINARY_BUSINESS' then
        clinic_name := trim(coalesce(metadata->>'clinic_name', ''));
        clinic_address := trim(coalesce(metadata->>'clinic_address', ''));
        municipality := trim(coalesce(metadata->>'municipality', ''));

        if clinic_name = '' or clinic_address = '' or municipality = '' then
            raise exception 'Falta el nombre, domicilio o municipio del negocio';
        end if;

        insert into public.business_applications (
            applicant_id,
            nombre_negocio,
            domicilio,
            municipio,
            ruc,
            telefono_contacto,
            responsable_veterinario,
            cedula_profesional,
            correo_contacto
        )
        values (
            new.id,
            clinic_name,
            clinic_address,
            municipality,
            coalesce(trim(metadata->>'tax_id'), ''),
            coalesce(trim(metadata->>'contact_phone'), ''),
            coalesce(trim(metadata->>'veterinarian_name'), ''),
            coalesce(trim(metadata->>'license_number'), ''),
            coalesce(new.email, '')
        );
    end if;

    return new;
end;
$$;

create or replace function public.aprobar_solicitud_veterinaria(
    p_solicitud_id uuid
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_applicant_id uuid;
begin
    if not public.is_superadmin() then
        raise exception 'Solo un SUPERADMIN puede aprobar solicitudes'
            using errcode = '42501';
    end if;

    select applicant_id
    into v_applicant_id
    from public.business_applications
    where id = p_solicitud_id
      and estado = 'PENDIENTE'::public.application_status
    for update;

    if not found then
        raise exception 'La solicitud no existe o ya no está pendiente';
    end if;

    update public.business_applications
    set estado = 'APROBADA'::public.application_status
    where id = p_solicitud_id;

    update public.profiles
    set role = 'VETERINARY_BUSINESS'::public.profile_role,
        updated_at = now()
    where id = v_applicant_id;
end;
$$;

create or replace function public.rechazar_solicitud_veterinaria(
    p_solicitud_id uuid
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
    if not public.is_superadmin() then
        raise exception 'Solo un SUPERADMIN puede rechazar solicitudes'
            using errcode = '42501';
    end if;

    update public.business_applications
    set estado = 'RECHAZADA'::public.application_status
    where id = p_solicitud_id
      and estado = 'PENDIENTE'::public.application_status;

    if not found then
        raise exception 'La solicitud no existe o ya no está pendiente';
    end if;
end;
$$;

revoke all on function public.aprobar_solicitud_veterinaria(uuid) from public;
revoke all on function public.rechazar_solicitud_veterinaria(uuid) from public;
grant execute on function public.aprobar_solicitud_veterinaria(uuid) to authenticated;
grant execute on function public.rechazar_solicitud_veterinaria(uuid) to authenticated;
