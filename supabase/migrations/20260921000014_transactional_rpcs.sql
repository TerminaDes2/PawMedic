-- These functions are called by Edge Functions with a service-role client.
-- Each function is a single PostgreSQL transaction and records its mutation.
create or replace function public.approve_business_create_tenant(
    p_business_id uuid,
    p_actor_id uuid
) returns public.businesses
language plpgsql security definer set search_path = public as $$
declare
    v_business public.businesses;
    v_tenant public.tenants;
begin
    select * into v_business from public.businesses
    where id = p_business_id for update;
    if not found then raise exception 'business not found'; end if;
    if v_business.status <> 'PENDING' then raise exception 'business is not pending'; end if;

    insert into public.tenants (name, status, created_by)
    values (v_business.public_name, 'ACTIVE', p_actor_id)
    returning * into v_tenant;

    update public.businesses
    set tenant_id = v_tenant.id, status = 'APPROVED',
        approved_by = p_actor_id, approved_at = now(), updated_at = now()
    where id = p_business_id
    returning * into v_business;

    update public.profiles set tenant_id = v_tenant.id, updated_at = now()
    where id = v_business.submitted_by and role = 'VETERINARY_BUSINESS';

    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (p_actor_id, v_tenant.id, 'APPROVE_BUSINESS', 'business', p_business_id);
    return v_business;
end;
$$;

create or replace function public.change_entity_status(
    p_entity_type text,
    p_entity_id uuid,
    p_status text,
    p_actor_id uuid
) returns jsonb
language plpgsql security definer set search_path = public as $$
declare
    v_result jsonb;
begin
    if p_entity_type = 'business' then
        update public.businesses set status = p_status::public.business_status, updated_at = now()
        where id = p_entity_id returning to_jsonb(businesses.*) into v_result;
    elsif p_entity_type = 'appointment' then
        update public.appointments set status = p_status::public.appointment_status, updated_at = now()
        where id = p_entity_id returning to_jsonb(appointments.*) into v_result;
    else
        raise exception 'unsupported entity type';
    end if;
    if v_result is null then raise exception 'entity not found'; end if;
    insert into public.audit_logs (actor_id, action, entity_type, entity_id, metadata)
    values (p_actor_id, 'STATUS_CHANGE', p_entity_type, p_entity_id,
            jsonb_build_object('status', p_status));
    return v_result;
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
language plpgsql security definer set search_path = public as $$
declare
    v_appointment public.appointments;
begin
    if exists (select 1 from public.appointments where idempotency_key = p_idempotency_key) then
        select * into v_appointment from public.appointments where idempotency_key = p_idempotency_key;
        return v_appointment;
    end if;
    if exists (
        select 1 from public.appointments
        where tenant_id = p_tenant_id and status in ('REQUESTED', 'CONFIRMED')
          and starts_at < p_ends_at and ends_at > p_starts_at
        for update
    ) then raise exception 'requested time is no longer available'; end if;
    insert into public.appointments (
        tenant_id, pet_id, service_id, booked_by, starts_at, ends_at, idempotency_key
    ) values (
        p_tenant_id, p_pet_id, p_service_id, p_booked_by, p_starts_at, p_ends_at, p_idempotency_key
    ) returning * into v_appointment;
    insert into public.audit_logs (actor_id, tenant_id, action, entity_type, entity_id)
    values (p_booked_by, p_tenant_id, 'CREATE_BOOKING', 'appointment', v_appointment.id);
    return v_appointment;
end;
$$;
