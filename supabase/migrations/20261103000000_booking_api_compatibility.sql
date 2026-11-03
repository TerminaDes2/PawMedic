-- Booking API repair for databases that already use timestamptz appointments.
-- This migration is additive: it does not drop tables, columns, or rows.

alter table public.tenants
    add column if not exists time_zone text not null default 'America/Mexico_City';

update public.tenants
set time_zone = 'America/Mexico_City'
where time_zone is null or length(trim(time_zone)) = 0;

alter table public.tenants
    alter column time_zone set default 'America/Mexico_City',
    alter column time_zone set not null;

alter table public.appointments
    add column if not exists owner_id uuid references public.profiles(id);

create or replace function public.get_working_shifts(
    p_tenant_id uuid,
    p_day_of_week integer
) returns table(schedule_id uuid, open_time time, close_time time)
language sql
stable
set search_path = public
as $$
    select schedule.id, schedule.open_time, schedule.close_time
    from public.business_schedules schedule
    where schedule.tenant_id = p_tenant_id
      and schedule.day_of_week = p_day_of_week
      and schedule.is_active
    order by schedule.open_time;
$$;

create or replace function public.get_available_slots(
    p_tenant_id uuid,
    p_service_id uuid,
    p_date date
) returns table(starts_at timestamptz, ends_at timestamptz, local_time text)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_duration integer;
    v_time_zone text;
begin
    if auth.uid() is null or not exists (
        select 1
        from public.profiles
        where id = auth.uid() and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id
      and service.tenant_id = p_tenant_id
      and service.active
      and tenant.status = 'ACTIVE';
    if not found then
        raise exception 'active service or clinic not found';
    end if;
    if v_duration is null or v_duration <= 0 then
        raise exception 'service duration must be positive';
    end if;
    if p_date < (now() at time zone v_time_zone)::date then
        return;
    end if;

    return query
    select distinct slot.starts_at,
           slot.starts_at + make_interval(mins => v_duration),
           to_char(slot.starts_at at time zone v_time_zone, 'HH24:MI')
    from public.business_schedules schedule
    cross join lateral generate_series(
        (p_date + schedule.open_time) at time zone v_time_zone,
        ((p_date + schedule.close_time) at time zone v_time_zone)
            - make_interval(mins => v_duration),
        make_interval(mins => v_duration)
    ) as slot(starts_at)
    where schedule.tenant_id = p_tenant_id
      and schedule.day_of_week = extract(dow from p_date)::smallint
      and schedule.is_active
      and slot.starts_at > now()
      and not exists (
          select 1
          from public.availability_blocks block
          where block.tenant_id = p_tenant_id
            and block.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and block.ends_at > slot.starts_at
      )
      and not exists (
          select 1
          from public.appointments appointment
          where appointment.tenant_id = p_tenant_id
            and appointment.status in ('REQUESTED', 'CONFIRMED')
            and appointment.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and appointment.ends_at > slot.starts_at
      )
    order by slot.starts_at;
end;
$$;

create or replace function public.create_critical_booking(
    p_tenant_id uuid,
    p_pet_id uuid,
    p_service_id uuid,
    p_booked_by uuid,
    p_starts_at timestamptz,
    p_ends_at timestamptz,
    p_idempotency_key uuid
) returns public.appointments
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_duration integer;
    v_time_zone text;
    v_appointment public.appointments;
begin
    if v_user_id is null or p_booked_by is distinct from v_user_id then
        raise exception 'booking user must match the authenticated account';
    end if;
    if p_tenant_id is null or p_pet_id is null or p_service_id is null
       or p_starts_at is null or p_ends_at is null or p_idempotency_key is null then
        raise exception 'clinic, pet, service, time, and idempotency key are required';
    end if;
    if not exists (
        select 1
        from public.profiles
        where id = v_user_id and role = 'USER'
    ) then
        raise exception 'customer account required';
    end if;
    if not exists (
        select 1
        from public.pets
        where id = p_pet_id and owner_id = v_user_id
    ) then
        raise exception 'pet must belong to the authenticated account';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id
      and service.tenant_id = p_tenant_id
      and service.active
      and tenant.status = 'ACTIVE';
    if not found then
        raise exception 'active service or clinic not found';
    end if;
    if v_duration is null or v_duration <= 0
       or p_starts_at >= p_ends_at
       or p_ends_at <> p_starts_at + make_interval(mins => v_duration) then
        raise exception 'appointment interval does not match service duration';
    end if;
    if p_starts_at <= now() then
        raise exception 'appointment must start in the future';
    end if;

    perform pg_advisory_xact_lock(
        hashtextextended('booking-key:' || p_idempotency_key::text, 0)
    );
    perform pg_advisory_xact_lock(
        hashtextextended(
            p_tenant_id::text || ':' ||
            (p_starts_at at time zone v_time_zone)::date::text,
            0
        )
    );

    select *
    into v_appointment
    from public.appointments
    where idempotency_key = p_idempotency_key
    for update;
    if found then
        if coalesce(v_appointment.booked_by, v_appointment.owner_id) is distinct from v_user_id
           or v_appointment.owner_id is distinct from v_user_id
           or v_appointment.pet_id is distinct from p_pet_id
           or v_appointment.tenant_id is distinct from p_tenant_id
           or v_appointment.service_id is distinct from p_service_id
           or v_appointment.starts_at is distinct from p_starts_at
           or v_appointment.ends_at is distinct from p_ends_at then
            raise exception 'idempotency key was already used for a different booking';
        end if;
        return v_appointment;
    end if;

    if not exists (
        select 1
        from public.get_available_slots(
            p_tenant_id,
            p_service_id,
            (p_starts_at at time zone v_time_zone)::date
        ) slot
        where slot.starts_at = p_starts_at
          and slot.ends_at = p_ends_at
    ) then
        raise exception 'requested time is no longer available';
    end if;

    insert into public.appointments (
        owner_id, tenant_id, pet_id, service_id, booked_by,
        starts_at, ends_at, idempotency_key, status
    ) values (
        v_user_id, p_tenant_id, p_pet_id, p_service_id, v_user_id,
        p_starts_at, p_ends_at, p_idempotency_key, 'REQUESTED'
    )
    returning * into v_appointment;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_user_id, p_tenant_id, 'CREATE_BOOKING', 'appointment', v_appointment.id);
    return v_appointment;
end;
$$;

-- Keep the legacy signature usable by previously released clients.
create or replace function public.book_appointment(
    p_owner_id uuid,
    p_pet_id uuid,
    p_tenant_id uuid,
    p_service_id uuid,
    p_fecha date,
    p_hora time
) returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_duration integer;
    v_time_zone text;
    v_starts_at timestamptz;
    v_appointment public.appointments;
begin
    if v_user_id is null or p_owner_id is distinct from v_user_id then
        raise exception 'booking owner must match the authenticated account';
    end if;

    select service.duration_minutes, tenant.time_zone
    into v_duration, v_time_zone
    from public.services service
    join public.tenants tenant on tenant.id = service.tenant_id
    where service.id = p_service_id
      and service.tenant_id = p_tenant_id
      and service.active
      and tenant.status = 'ACTIVE';
    if not found then
        raise exception 'active service or clinic not found';
    end if;

    v_starts_at := (p_fecha + p_hora) at time zone v_time_zone;
    v_appointment := public.create_critical_booking(
        p_tenant_id,
        p_pet_id,
        p_service_id,
        v_user_id,
        v_starts_at,
        v_starts_at + make_interval(mins => v_duration),
        gen_random_uuid()
    );
    return v_appointment.id;
end;
$$;

create or replace function public.get_owned_booking_pets()
returns table(id uuid, name text, species text, breed text)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null or not exists (
        select 1 from public.profiles
        where id = v_user_id and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public'
          and table_name = 'pets'
          and column_name = 'species_id'
    ) and exists (
        select 1 from information_schema.columns
        where table_schema = 'public'
          and table_name = 'pets'
          and column_name = 'breed_id'
    ) and to_regclass('public.species') is not null
      and to_regclass('public.breeds') is not null then
        return query execute
            'select pet.id, pet.name::text, coalesce(species.name::text, ''''), breed.name::text
             from public.pets pet
             left join public.species species on species.id = pet.species_id
             left join public.breeds breed on breed.id = pet.breed_id
             where pet.owner_id = $1
             order by pet.name'
            using v_user_id;
    else
        return query execute
            'select pet.id, pet.name::text, pet.species::text, pet.breed::text
             from public.pets pet
             where pet.owner_id = $1
             order by pet.name'
            using v_user_id;
    end if;
end;
$$;

create or replace function public.get_owned_appointments()
returns table(
    id uuid,
    pet_id uuid,
    pet_name text,
    tenant_id uuid,
    business_name text,
    service_id uuid,
    service_name text,
    starts_at timestamptz,
    ends_at timestamptz,
    status public.appointment_status,
    local_date text,
    local_time text
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null or not exists (
        select 1 from public.profiles
        where id = v_user_id and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;

    return query
    select appointment.id,
           appointment.pet_id,
           pet.name::text,
           appointment.tenant_id,
           coalesce(business.public_name, tenant.name)::text,
           appointment.service_id,
           service.name::text,
           appointment.starts_at,
           appointment.ends_at,
           appointment.status,
           to_char(appointment.starts_at at time zone tenant.time_zone, 'YYYY-MM-DD'),
           to_char(appointment.starts_at at time zone tenant.time_zone, 'HH24:MI')
    from public.appointments appointment
    join public.pets pet on pet.id = appointment.pet_id
    join public.tenants tenant on tenant.id = appointment.tenant_id
    join public.services service on service.id = appointment.service_id
    left join public.businesses business on business.tenant_id = tenant.id
    where appointment.owner_id = v_user_id
       or appointment.booked_by = v_user_id
    order by appointment.starts_at;
end;
$$;

create or replace function public.get_tenant_appointments(p_tenant_id uuid)
returns table(
    id uuid,
    pet_id uuid,
    pet_name text,
    service_id uuid,
    service_name text,
    starts_at timestamptz,
    ends_at timestamptz,
    status public.appointment_status,
    display_time text
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_time_zone text;
begin
    if auth.uid() is null or not public.is_tenant_member(p_tenant_id) then
        raise exception 'tenant membership required';
    end if;

    select tenant.time_zone
    into v_time_zone
    from public.tenants tenant
    where tenant.id = p_tenant_id;
    if not found then
        raise exception 'clinic not found';
    end if;

    return query
    select appointment.id,
           appointment.pet_id,
           pet.name::text,
           appointment.service_id,
           service.name::text,
           appointment.starts_at,
           appointment.ends_at,
           appointment.status,
           to_char(appointment.starts_at at time zone v_time_zone, 'DD/MM/YYYY HH24:MI')
               || ' - ' ||
           to_char(appointment.ends_at at time zone v_time_zone, 'HH24:MI')
    from public.appointments appointment
    join public.pets pet on pet.id = appointment.pet_id
    join public.services service on service.id = appointment.service_id
    where appointment.tenant_id = p_tenant_id
    order by appointment.starts_at;
end;
$$;

create or replace function public.transition_appointment_status(
    p_appointment_id uuid,
    p_expected_status public.appointment_status,
    p_next_status public.appointment_status
) returns public.appointments
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_appointment public.appointments;
    v_allowed boolean;
begin
    if v_actor_id is null then
        raise exception 'authentication required';
    end if;

    select *
    into v_appointment
    from public.appointments
    where id = p_appointment_id
    for update;
    if not found then
        raise exception 'appointment not found';
    end if;
    if not public.is_tenant_member(v_appointment.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if v_appointment.status <> p_expected_status then
        raise exception 'appointment status changed; reload before retrying';
    end if;

    v_allowed :=
        (p_expected_status = 'REQUESTED' and p_next_status in ('CONFIRMED', 'CANCELLED'))
        or (p_expected_status = 'CONFIRMED' and p_next_status = 'COMPLETED');
    if not v_allowed then
        raise exception 'unsupported appointment status transition';
    end if;

    update public.appointments
    set status = p_next_status
    where id = p_appointment_id
    returning * into v_appointment;

    insert into public.audit_logs (
        actor_id, tenant_id, action, entity_type, entity_id, metadata
    ) values (
        v_actor_id,
        v_appointment.tenant_id,
        'CHANGE_APPOINTMENT_STATUS',
        'appointment',
        p_appointment_id,
        jsonb_build_object('status', p_next_status)
    );
    return v_appointment;
end;
$$;

grant execute on function public.get_working_shifts(uuid, integer) to authenticated, service_role;

revoke all on function public.get_available_slots(uuid, uuid, date) from public, anon;
grant execute on function public.get_available_slots(uuid, uuid, date) to authenticated;

revoke all on function public.create_critical_booking(uuid, uuid, uuid, uuid, timestamptz, timestamptz, uuid) from public, anon;
grant execute on function public.create_critical_booking(uuid, uuid, uuid, uuid, timestamptz, timestamptz, uuid) to authenticated;

grant execute on function public.book_appointment(uuid, uuid, uuid, uuid, date, time) to authenticated, service_role;

revoke all on function public.get_owned_booking_pets() from public, anon;
grant execute on function public.get_owned_booking_pets() to authenticated;

revoke all on function public.get_owned_appointments() from public, anon;
grant execute on function public.get_owned_appointments() to authenticated;

revoke all on function public.get_tenant_appointments(uuid) from public, anon;
grant execute on function public.get_tenant_appointments(uuid) to authenticated;

revoke all on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) from public, anon;
grant execute on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) to authenticated;
