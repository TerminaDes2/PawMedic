alter table public.businesses
    add column if not exists applicant_name text not null default '',
    add column if not exists tax_id text not null default '',
    add column if not exists email text not null default '',
    add column if not exists phone text not null default '',
    add column if not exists address text not null default '',
    add column if not exists license_number text not null default '',
    add column if not exists decision_reason text;

alter table public.products
    add column if not exists image_url text not null default '',
    add column if not exists published boolean not null default false;

alter table public.tenants
    add column if not exists time_zone text not null default 'America/Mexico_City';

create unique index if not exists businesses_active_applicant_unique
    on public.businesses (submitted_by)
    where submitted_by is not null and status in ('PENDING', 'APPROVED');

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name)
    values (
        new.id,
        coalesce(new.email, ''),
        coalesce(new.raw_user_meta_data ->> 'full_name', '')
    );
    return new;
end;
$$;

create or replace function public.guard_profile_privileges()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is not null
       and (new.role is distinct from old.role or new.tenant_id is distinct from old.tenant_id)
       and not public.is_superadmin() then
        raise exception 'profile role and tenant can only be changed by an administrator';
    end if;
    return new;
end;
$$;

drop trigger if exists profiles_guard_privileges on public.profiles;
create trigger profiles_guard_privileges
before update on public.profiles
for each row execute function public.guard_profile_privileges();

create or replace function public.submit_business_application(
    p_applicant_name text,
    p_business_name text,
    p_tax_id text,
    p_phone text,
    p_address text,
    p_license_number text
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile public.profiles;
    v_business public.businesses;
begin
    if v_user_id is null then raise exception 'authentication required'; end if;
    if not exists (
        select 1 from auth.users
        where id = v_user_id and email_confirmed_at is not null
    ) then
        raise exception 'verify the account email before submitting a business application';
    end if;
    select * into v_profile from public.profiles where id = v_user_id for update;
    if not found or v_profile.role <> 'USER' then
        raise exception 'this account cannot submit a veterinary application';
    end if;
    if length(trim(p_applicant_name)) < 2
       or length(trim(p_business_name)) < 2
       or length(trim(p_tax_id)) < 3
       or length(trim(p_phone)) < 8
       or length(trim(p_address)) < 5
       or length(trim(p_license_number)) < 2 then
        raise exception 'complete all required business application fields';
    end if;
    if exists (
        select 1 from public.businesses
        where submitted_by = v_user_id and status in ('PENDING', 'APPROVED')
    ) then
        raise exception 'an active business application already exists for this account';
    end if;

    insert into public.businesses (
        submitted_by, applicant_name, legal_name, public_name, tax_id,
        email, phone, address, license_number, status
    ) values (
        v_user_id, trim(p_applicant_name), trim(p_business_name), trim(p_business_name),
        trim(p_tax_id), coalesce(auth.jwt() ->> 'email', v_profile.email),
        trim(p_phone), trim(p_address), trim(p_license_number), 'PENDING'
    ) returning * into v_business;

    insert into public.audit_logs (actor_id, action, entity_type, entity_id)
    values (v_user_id, 'SUBMIT_BUSINESS_APPLICATION', 'business', v_business.id);
    return v_business;
end;
$$;

create or replace function public.approve_business_application(
    p_business_id uuid
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_business public.businesses;
    v_tenant public.tenants;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;
    select * into v_business from public.businesses
    where id = p_business_id for update;
    if not found then raise exception 'business application not found'; end if;
    if v_business.status <> 'PENDING' then raise exception 'business application is not pending'; end if;
    if v_business.submitted_by is null then raise exception 'application has no authenticated owner'; end if;

    insert into public.tenants (name, status, created_by)
    values (v_business.public_name, 'ACTIVE', v_business.submitted_by)
    returning * into v_tenant;

    update public.businesses
    set tenant_id = v_tenant.id,
        status = 'APPROVED',
        approved_by = v_actor_id,
        approved_at = now(),
        updated_at = now()
    where id = p_business_id returning * into v_business;

    update public.profiles
    set role = 'VETERINARY_BUSINESS', tenant_id = v_tenant.id, updated_at = now()
    where id = v_business.submitted_by;
    if not found then raise exception 'applicant profile not found'; end if;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_actor_id, v_tenant.id, 'APPROVE_BUSINESS', 'business', p_business_id);
    return v_business;
end;
$$;

create or replace function public.reject_business_application(
    p_business_id uuid,
    p_reason text default ''
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_business public.businesses;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;
    update public.businesses
    set status = 'REJECTED',
        decision_reason = nullif(trim(p_reason), ''),
        approved_by = v_actor_id,
        approved_at = now(),
        updated_at = now()
    where id = p_business_id and status = 'PENDING'
    returning * into v_business;
    if not found then raise exception 'business application is missing or no longer pending'; end if;

    insert into public.audit_logs (actor_id, action, entity_type, entity_id, metadata)
    values (
        v_actor_id, 'REJECT_BUSINESS', 'business', p_business_id,
        jsonb_build_object('reason', nullif(trim(p_reason), ''))
    );
    return v_business;
end;
$$;

create or replace function public.set_business_status(
    p_business_id uuid,
    p_status public.business_status
) returns public.businesses
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_business public.businesses;
begin
    if v_actor_id is null or not public.is_superadmin() then
        raise exception 'superadministrator access required';
    end if;
    if p_status not in ('APPROVED', 'SUSPENDED') then
        raise exception 'unsupported business status transition';
    end if;
    update public.businesses
    set status = p_status, updated_at = now()
    where id = p_business_id and status in ('APPROVED', 'SUSPENDED')
    returning * into v_business;
    if not found then raise exception 'business not found or status cannot be changed'; end if;

    update public.tenants
    set status = case when p_status = 'APPROVED' then 'ACTIVE'::public.tenant_status
                      else 'SUSPENDED'::public.tenant_status end,
        updated_at = now()
    where id = v_business.tenant_id;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id, metadata)
    values (
        v_actor_id, v_business.tenant_id, 'CHANGE_BUSINESS_STATUS', 'business', p_business_id,
        jsonb_build_object('status', p_status)
    );
    return v_business;
end;
$$;

drop policy if exists businesses_tenant_isolation on public.businesses;
create policy businesses_authorized_read on public.businesses for select to authenticated
using (
    submitted_by = auth.uid()
    or public.is_superadmin()
);

drop policy if exists tenants_superadmin_or_member on public.tenants;
create policy tenants_directory_or_member on public.tenants for select to authenticated
using (
    public.is_superadmin()
    or public.is_tenant_member(id)
    or status = 'ACTIVE'
);

create policy services_public_active_read on public.services for select to authenticated
using (
    active
    and exists (select 1 from public.tenants t where t.id = tenant_id and t.status = 'ACTIVE')
);

create policy products_public_active_read on public.products for select to authenticated
using (
    active and published
    and exists (select 1 from public.tenants t where t.id = tenant_id and t.status = 'ACTIVE')
);

create policy schedules_public_active_read on public.business_schedules for select to authenticated
using (
    exists (select 1 from public.tenants t where t.id = tenant_id and t.status = 'ACTIVE')
);

create or replace view public.business_directory
with (security_barrier = true)
as
select
    b.tenant_id as id,
    b.public_name as name,
    b.address,
    b.phone
from public.businesses b
join public.tenants t on t.id = b.tenant_id
where b.status = 'APPROVED'
  and t.status = 'ACTIVE';

revoke all on public.business_directory from public, anon;
grant select on public.business_directory to authenticated;

drop policy if exists appointments_authorized_access on public.appointments;
create policy appointments_authorized_read on public.appointments for select
using (booked_by = auth.uid() or public.is_tenant_member(tenant_id));

create or replace function public.get_available_slots(
    p_tenant_id uuid,
    p_service_id uuid,
    p_date date
) returns table(starts_at timestamptz, ends_at timestamptz)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_duration integer;
    v_open time;
    v_close time;
    v_time_zone text;
    v_opens_at timestamptz;
    v_closes_at timestamptz;
begin
    if auth.uid() is null or not exists (
        select 1 from public.profiles where id = auth.uid() and role = 'USER'
    ) then
        raise exception 'authenticated customer access required';
    end if;
    select s.duration_minutes, t.time_zone
    into v_duration, v_time_zone
    from public.services s
    join public.tenants t on t.id = s.tenant_id
    where s.id = p_service_id and s.tenant_id = p_tenant_id
      and s.active and t.status = 'ACTIVE';
    if not found then raise exception 'active service or clinic not found'; end if;
    if p_date < (now() at time zone v_time_zone)::date then
        return;
    end if;

    select bs.opens_at, bs.closes_at
    into v_open, v_close
    from public.business_schedules bs
    where bs.tenant_id = p_tenant_id
      and bs.weekday = extract(dow from p_date)::smallint;
    if not found then return; end if;

    v_opens_at := (p_date + v_open) at time zone v_time_zone;
    v_closes_at := (p_date + v_close) at time zone v_time_zone;

    return query
    select slot.starts_at, slot.starts_at + make_interval(mins => v_duration)
    from generate_series(
        v_opens_at,
        v_closes_at - make_interval(mins => v_duration),
        make_interval(mins => v_duration)
    ) as slot(starts_at)
    where slot.starts_at > now()
      and not exists (
          select 1 from public.availability_blocks b
          where b.tenant_id = p_tenant_id
            and b.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and b.ends_at > slot.starts_at
      )
      and not exists (
          select 1 from public.appointments a
          where a.tenant_id = p_tenant_id
            and a.status in ('REQUESTED', 'CONFIRMED')
            and a.starts_at < slot.starts_at + make_interval(mins => v_duration)
            and a.ends_at > slot.starts_at
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
    if v_user_id is null or p_booked_by <> v_user_id then
        raise exception 'booking user must match the authenticated account';
    end if;
    if not exists (
        select 1 from public.profiles where id = v_user_id and role = 'USER'
    ) then
        raise exception 'customer account required';
    end if;
    if not exists (select 1 from public.pets where id = p_pet_id and owner_id = v_user_id) then
        raise exception 'pet must belong to the authenticated account';
    end if;

    select s.duration_minutes, t.time_zone
    into v_duration, v_time_zone
    from public.services s
    join public.tenants t on t.id = s.tenant_id
    where s.id = p_service_id and s.tenant_id = p_tenant_id
      and s.active and t.status = 'ACTIVE';
    if not found then raise exception 'active service or clinic not found'; end if;
    if p_ends_at <> p_starts_at + make_interval(mins => v_duration) then
        raise exception 'appointment interval does not match service duration';
    end if;
    if p_starts_at <= now() then raise exception 'appointment must start in the future'; end if;

    perform pg_advisory_xact_lock(
        hashtextextended(
            p_tenant_id::text || ':' ||
            ((p_starts_at at time zone v_time_zone)::date)::text,
            0
        )
    );

    select * into v_appointment
    from public.appointments where idempotency_key = p_idempotency_key;
    if found then
        if v_appointment.booked_by <> v_user_id
           or v_appointment.pet_id <> p_pet_id
           or v_appointment.tenant_id <> p_tenant_id
           or v_appointment.service_id <> p_service_id
           or v_appointment.starts_at <> p_starts_at
           or v_appointment.ends_at <> p_ends_at then
            raise exception 'idempotency key was already used for a different booking';
        end if;
        return v_appointment;
    end if;

    if not exists (
        select 1 from public.get_available_slots(
            p_tenant_id,
            p_service_id,
            (p_starts_at at time zone v_time_zone)::date
        ) slot
        where slot.starts_at = p_starts_at and slot.ends_at = p_ends_at
    ) then
        raise exception 'requested time is no longer available';
    end if;

    insert into public.appointments (
        tenant_id, pet_id, service_id, booked_by, starts_at, ends_at, idempotency_key
    ) values (
        p_tenant_id, p_pet_id, p_service_id, v_user_id, p_starts_at, p_ends_at, p_idempotency_key
    ) returning * into v_appointment;

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (v_user_id, p_tenant_id, 'CREATE_BOOKING', 'appointment', v_appointment.id);
    return v_appointment;
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
    if v_actor_id is null then raise exception 'authentication required'; end if;
    select * into v_appointment from public.appointments
    where id = p_appointment_id for update;
    if not found then raise exception 'appointment not found'; end if;
    if not public.is_tenant_member(v_appointment.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if v_appointment.status <> p_expected_status then
        raise exception 'appointment status changed; reload before retrying';
    end if;

    v_allowed :=
        (p_expected_status = 'REQUESTED' and p_next_status in ('CONFIRMED', 'CANCELLED'))
        or (p_expected_status = 'CONFIRMED' and p_next_status in ('COMPLETED', 'NO_SHOW'));
    if not v_allowed then raise exception 'unsupported appointment status transition'; end if;

    update public.appointments
    set status = p_next_status, updated_at = now()
    where id = p_appointment_id returning * into v_appointment;
    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id, metadata)
    values (
        v_actor_id, v_appointment.tenant_id, 'CHANGE_APPOINTMENT_STATUS',
        'appointment', p_appointment_id, jsonb_build_object('status', p_next_status)
    );
    return v_appointment;
end;
$$;

create or replace function public.guard_appointment_updates()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() = old.booked_by then
        raise exception 'customers cannot edit or cancel appointments in this MVP';
    end if;
    if not public.is_superadmin() and not public.is_tenant_member(old.tenant_id) then
        raise exception 'tenant membership required';
    end if;
    if new.tenant_id is distinct from old.tenant_id
       or new.pet_id is distinct from old.pet_id
       or new.service_id is distinct from old.service_id
       or new.booked_by is distinct from old.booked_by
       or new.starts_at is distinct from old.starts_at
       or new.ends_at is distinct from old.ends_at
       or new.idempotency_key is distinct from old.idempotency_key then
        raise exception 'appointment details cannot be changed after booking';
    end if;
    if not (
        (old.status = 'REQUESTED' and new.status in ('CONFIRMED', 'CANCELLED'))
        or (old.status = 'CONFIRMED' and new.status in ('COMPLETED', 'NO_SHOW'))
    ) then
        raise exception 'unsupported appointment status transition';
    end if;
    return new;
end;
$$;

drop trigger if exists appointments_guard_updates on public.appointments;
create trigger appointments_guard_updates
before update on public.appointments
for each row execute function public.guard_appointment_updates();

revoke all on function public.create_critical_booking(uuid, uuid, uuid, uuid, timestamptz, timestamptz, uuid) from public, anon;
grant execute on function public.create_critical_booking(uuid, uuid, uuid, uuid, timestamptz, timestamptz, uuid) to authenticated;
revoke all on function public.get_available_slots(uuid, uuid, date) from public, anon;
grant execute on function public.get_available_slots(uuid, uuid, date) to authenticated;
revoke all on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) from public, anon;
grant execute on function public.transition_appointment_status(uuid, public.appointment_status, public.appointment_status) to authenticated;

revoke all on function public.approve_business_create_tenant(uuid, uuid) from public, anon, authenticated;
revoke all on function public.change_entity_status(text, uuid, text, uuid) from public, anon, authenticated;

revoke all on function public.submit_business_application(text, text, text, text, text, text) from public, anon;
grant execute on function public.submit_business_application(text, text, text, text, text, text) to authenticated;
revoke all on function public.approve_business_application(uuid) from public, anon;
grant execute on function public.approve_business_application(uuid) to authenticated;
revoke all on function public.reject_business_application(uuid, text) from public, anon;
grant execute on function public.reject_business_application(uuid, text) to authenticated;
revoke all on function public.set_business_status(uuid, public.business_status) from public, anon;
grant execute on function public.set_business_status(uuid, public.business_status) to authenticated;
