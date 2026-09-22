create extension if not exists "pgcrypto";

create type public.profile_role as enum ('USER', 'VETERINARY_BUSINESS', 'SUPERADMIN');

create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null default '',
    display_name text not null default '',
    role public.profile_role not null default 'USER',
    tenant_id uuid,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.profiles (id, email) values (new.id, coalesce(new.email, ''));
    return new;
end;
$$;

create trigger on_auth_user_created after insert on auth.users
for each row execute procedure public.handle_new_user();
