-- Requires enable_consultation_status.sql to have been committed first.
begin;

alter table public.medical_records
    add column if not exists appointment_id uuid references public.appointments(id);
create unique index if not exists medical_records_appointment_unique
    on public.medical_records (appointment_id) where appointment_id is not null;

alter table public.medical_records enable row level security;
drop policy if exists medical_records_appointment_read on public.medical_records;
create policy medical_records_appointment_read on public.medical_records
for select to authenticated
using (
    exists (select 1 from public.profiles p where p.id = auth.uid()
        and p.role = 'VETERINARY_BUSINESS' and p.tenant_id = medical_records.tenant_id)
    or exists (select 1 from public.pets p where p.id = medical_records.pet_id and p.owner_id = auth.uid())
);
grant select on public.medical_records to authenticated;

-- Update the guard used by installations that already have this trigger.
create or replace function public.guard_appointment_updates()
returns trigger language plpgsql security definer set search_path = public
as $$
begin
    if auth.uid() = old.booked_by then raise exception 'customers cannot edit appointments'; end if;
    if not public.is_superadmin() and not public.is_tenant_member(old.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if new.tenant_id is distinct from old.tenant_id or new.pet_id is distinct from old.pet_id
       or new.service_id is distinct from old.service_id or new.booked_by is distinct from old.booked_by
       or new.starts_at is distinct from old.starts_at or new.ends_at is distinct from old.ends_at
       or new.idempotency_key is distinct from old.idempotency_key then
        raise exception 'appointment details cannot be changed after booking';
    end if;
    if not (
        (old.status::text = 'REQUESTED' and new.status::text in ('CONFIRMED', 'CANCELLED'))
        or (old.status::text = 'CONFIRMED' and new.status::text = 'IN_PROGRESS')
        or (old.status::text = 'IN_PROGRESS' and new.status::text = 'COMPLETED'
            and exists (select 1 from public.medical_records where appointment_id = old.id))
    ) then raise exception 'unsupported appointment status transition'; end if;
    return new;
end;
$$;

create or replace function public.transition_appointment_status(
    p_appointment_id uuid,
    p_expected_status public.appointment_status,
    p_next_status public.appointment_status
) returns public.appointments
language plpgsql security definer set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_appointment public.appointments;
begin
    if v_actor_id is null then raise exception 'authentication required'; end if;
    select * into v_appointment from public.appointments where id = p_appointment_id for update;
    if not found then raise exception 'appointment not found'; end if;
    if not public.is_tenant_member(v_appointment.tenant_id) then raise exception 'tenant membership required'; end if;
    if v_appointment.status <> p_expected_status then raise exception 'appointment status changed; reload before retrying'; end if;
    if not (
        (p_expected_status::text = 'REQUESTED' and p_next_status::text in ('CONFIRMED', 'CANCELLED'))
        or (p_expected_status::text = 'CONFIRMED' and p_next_status::text = 'IN_PROGRESS')
    ) then raise exception 'unsupported appointment status transition'; end if;
    update public.appointments set status = p_next_status
    where id = p_appointment_id returning * into v_appointment;
    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id, metadata)
    values (v_actor_id, v_appointment.tenant_id, 'CHANGE_APPOINTMENT_STATUS', 'appointment',
            p_appointment_id, jsonb_build_object('status', p_next_status));
    return v_appointment;
end;
$$;

create or replace function public.finish_appointment_with_record(
    p_appointment_id uuid, p_reason text, p_symptoms text, p_diagnosis text,
    p_treatment text, p_observations text, p_weight_grams integer,
    p_medication text, p_dosage text
) returns uuid
language plpgsql security definer set search_path = public
as $$
declare
    v_actor uuid := auth.uid();
    v_appointment public.appointments;
    v_record uuid;
    v_service text;
    v_zone text;
    v_local timestamp;
begin
    if v_actor is null then raise exception 'authentication required'; end if;
    select * into v_appointment from public.appointments where id = p_appointment_id for update;
    if not found then raise exception 'appointment not found'; end if;
    if not exists (
        select 1 from public.profiles
        where id = v_actor and role = 'VETERINARY_BUSINESS' and tenant_id = v_appointment.tenant_id
    ) then raise exception 'veterinary clinic membership required'; end if;
    select id into v_record from public.medical_records where appointment_id = p_appointment_id;
    if found then return v_record; end if;
    if v_appointment.status::text not in ('IN_PROGRESS', 'COMPLETED') then
        raise exception 'appointment must be in progress or already completed';
    end if;
    if length(trim(coalesce(p_reason, ''))) = 0 or length(trim(coalesce(p_symptoms, ''))) = 0
       or length(trim(coalesce(p_diagnosis, ''))) = 0 or length(trim(coalesce(p_treatment, ''))) = 0
       or length(trim(coalesce(p_observations, ''))) = 0 or p_weight_grams is null or p_weight_grams <= 0
       or p_medication is null or p_dosage is null then
        raise exception 'clinical details and positive weight are required';
    end if;
    select service.name, tenant.time_zone into v_service, v_zone
    from public.services service join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = v_appointment.service_id and tenant.id = v_appointment.tenant_id;
    if not found then raise exception 'appointment clinic or service not found'; end if;
    v_local := (case when v_appointment.status::text = 'COMPLETED'
                     then v_appointment.starts_at else now() end) at time zone v_zone;
    insert into public.medical_records (
        appointment_id, pet_id, tenant_id, veterinarian_id, authored_by,
        weight_grams, record_date, record_time, consultation_reason, symptoms,
        diagnosis, treatment, medication, dosage, observations, title, content
    ) values (
        p_appointment_id, v_appointment.pet_id, v_appointment.tenant_id, v_actor, v_actor,
        p_weight_grams, v_local::date, v_local::time, trim(p_reason), trim(p_symptoms),
        trim(p_diagnosis), trim(p_treatment), trim(p_medication), trim(p_dosage),
        trim(p_observations), v_service,
        'Motivo: ' || trim(p_reason) || E'\nSíntomas: ' || trim(p_symptoms)
            || E'\nDiagnóstico: ' || trim(p_diagnosis) || E'\nTratamiento: ' || trim(p_treatment)
            || E'\nObservaciones: ' || trim(p_observations)
    ) returning id into v_record;
    if v_appointment.status::text = 'IN_PROGRESS' then
        update public.appointments set status = 'COMPLETED' where id = p_appointment_id;
    end if;
    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id, metadata)
    values (v_actor, v_appointment.tenant_id, 'COMPLETE_APPOINTMENT', 'appointment',
            p_appointment_id, jsonb_build_object('medical_record_id', v_record));
    return v_record;
end;
$$;

create or replace function public.get_owned_record_clinics()
returns table(tenant_id uuid, name text)
language sql stable security definer set search_path = public
as $$
    select distinct tenant.id, tenant.name::text
    from public.medical_records record
    join public.pets pet on pet.id = record.pet_id
    join public.tenants tenant on tenant.id = record.tenant_id
    where pet.owner_id = auth.uid();
$$;

create or replace function public.get_completed_appointments_missing_records(p_tenant_id uuid)
returns setof uuid
language plpgsql stable security definer set search_path = public
as $$
begin
    if auth.uid() is null or not public.is_tenant_member(p_tenant_id) then
        raise exception 'tenant membership required';
    end if;
    return query
    select appointment.id from public.appointments appointment
    where appointment.tenant_id = p_tenant_id and appointment.status::text = 'COMPLETED'
      and not exists (
          select 1 from public.medical_records record where record.appointment_id = appointment.id
      );
end;
$$;

revoke all on function public.finish_appointment_with_record(uuid,text,text,text,text,text,integer,text,text) from public, anon;
grant execute on function public.finish_appointment_with_record(uuid,text,text,text,text,text,integer,text,text) to authenticated;
revoke all on function public.get_owned_record_clinics() from public, anon;
grant execute on function public.get_owned_record_clinics() to authenticated;
revoke all on function public.get_completed_appointments_missing_records(uuid) from public, anon;
grant execute on function public.get_completed_appointments_missing_records(uuid) to authenticated;

notify pgrst, 'reload schema';
commit;
