-- Run in the Supabase SQL Editor. This aligns legacy pets tables with the
-- columns written by the mobile app without inventing missing pet data.
begin;

create table if not exists public.pets (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references public.profiles(id) on delete cascade,
    name text not null check (length(trim(name)) > 0),
    species text not null,
    breed text,
    date_of_birth date,
    weight_grams integer check (weight_grams is null or weight_grams > 0),
    notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.pets
    add column if not exists owner_id uuid references public.profiles(id) on delete cascade,
    add column if not exists name text,
    add column if not exists species text,
    add column if not exists breed text,
    add column if not exists date_of_birth date,
    add column if not exists weight_grams integer,
    add column if not exists notes text,
    add column if not exists created_at timestamptz not null default now(),
    add column if not exists updated_at timestamptz not null default now();

alter table public.pets
    alter column created_at set default now(),
    alter column updated_at set default now();

create index if not exists pets_owner_id_idx on public.pets(owner_id);

do $$
declare
    mapping record;
    legacy_column text;
    id_type text;
begin
    select data_type into id_type
    from information_schema.columns
    where table_schema = 'public' and table_name = 'pets'
      and column_name = 'id';

    if id_type = 'uuid' then
        alter table public.pets alter column id set default gen_random_uuid();
    end if;

    for mapping in
        select * from (values
            ('nombre', 'name'),
            ('especie', 'species'),
            ('raza', 'breed'),
            ('alergias', 'notes'),
            ('allergies', 'notes')
        ) as aliases(legacy_name, canonical_name)
    loop
        if exists (
            select 1 from information_schema.columns
            where table_schema = 'public' and table_name = 'pets'
              and column_name = mapping.legacy_name
        ) and exists (
            select 1 from information_schema.columns
            where table_schema = 'public' and table_name = 'pets'
              and column_name = mapping.canonical_name
        ) then
            execute format(
                'update public.pets
                 set %1$I = nullif(btrim(%2$I::text), '''')
                 where (%1$I is null or btrim(%1$I::text) = '''')
                   and %2$I is not null',
                mapping.canonical_name,
                mapping.legacy_name
            );
        end if;
    end loop;

    for legacy_column in
        select column_name
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'pets'
          and is_nullable = 'NO'
          and column_name = any (array[
              'edad', 'age', 'nombre', 'especie', 'raza', 'alergias',
              'allergies', 'sexo', 'gender', 'peso', 'foto_url',
              'photo_url', 'medical_id', 'is_insured'
          ])
    loop
        execute format(
            'alter table public.pets alter column %I drop not null',
            legacy_column
        );
    end loop;
end $$;

commit;

notify pgrst, 'reload schema';

-- Inspect the veterinary application for this email and compare these values
-- with the clinic directory (public_name, address, and phone).
select
    u.id as auth_user_id,
    u.email as auth_email,
    p.role as profile_role,
    p.tenant_id as profile_tenant_id,
    to_jsonb(b)->>'id' as application_id,
    to_jsonb(b)->>'applicant_name' as applicant_name,
    to_jsonb(b)->>'public_name' as registered_public_name,
    to_jsonb(b)->>'legal_name' as legal_name,
    to_jsonb(b)->>'tax_id' as tax_id,
    to_jsonb(b)->>'email' as application_email,
    to_jsonb(b)->>'phone' as application_phone,
    to_jsonb(b)->>'address' as application_address,
    to_jsonb(b)->>'license_number' as license_number,
    to_jsonb(b)->>'status' as application_status,
    to_jsonb(d)->>'name' as directory_name,
    to_jsonb(d)->>'address' as directory_address,
    to_jsonb(d)->>'phone' as directory_phone,
    to_jsonb(t)->>'name' as tenant_name,
    to_jsonb(t)->>'status' as tenant_status
from auth.users u
left join public.profiles p on p.id = u.id
left join public.businesses b on b.submitted_by = u.id
left join public.tenants t on t.id = coalesce(
    nullif(to_jsonb(b)->>'tenant_id', '')::uuid,
    p.tenant_id
)
left join public.business_directory d on d.id = coalesce(
    nullif(to_jsonb(b)->>'tenant_id', '')::uuid,
    p.tenant_id
)
where lower(u.email) = lower('elkrfstv@gmail.com')
order by to_jsonb(b)->>'created_at' desc nulls last;

-- Confirm the mobile API's expected columns and any remaining required fields.
select
    column_name,
    data_type,
    is_nullable,
    column_default
from information_schema.columns
where table_schema = 'public' and table_name = 'pets'
order by ordinal_position;

select
    column_name,
    data_type,
    column_default
from information_schema.columns
where table_schema = 'public'
  and table_name = 'pets'
  and is_nullable = 'NO'
  and column_default is null
  and column_name not in (
      'owner_id', 'name', 'species', 'breed', 'date_of_birth',
      'weight_grams', 'notes'
  );

select
    conname as constraint_name,
    contype as constraint_type,
    pg_get_constraintdef(oid) as definition
from pg_constraint
where conrelid = 'public.pets'::regclass
order by conname;

select
    c.relrowsecurity as row_level_security_enabled,
    p.policyname,
    p.cmd as policy_command,
    p.qual as using_expression,
    p.with_check as check_expression
from pg_class c
join pg_namespace n on n.oid = c.relnamespace
left join pg_policies p on p.schemaname = n.nspname and p.tablename = c.relname
where n.nspname = 'public' and c.relname = 'pets';
