create table if not exists public.legal_acceptances (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    terms_version text not null,
    privacy_version text not null,
    terms_accepted_at timestamptz not null default now(),
    privacy_accepted_at timestamptz not null default now(),
    unique (user_id, terms_version, privacy_version)
);

alter table public.legal_acceptances enable row level security;

revoke all on public.legal_acceptances from public, anon, authenticated;
grant select on public.legal_acceptances to authenticated;

drop policy if exists legal_acceptances_owner_select on public.legal_acceptances;
create policy legal_acceptances_owner_select on public.legal_acceptances
for select to authenticated
using (user_id = auth.uid());

create or replace function public.record_legal_acceptance(
    p_terms_version text,
    p_privacy_version text
) returns public.legal_acceptances
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_acceptance public.legal_acceptances;
begin
    if v_user_id is null then
        raise exception 'authentication required';
    end if;
    if nullif(btrim(p_terms_version), '') is null or nullif(btrim(p_privacy_version), '') is null then
        raise exception 'terms and privacy versions are required';
    end if;

    insert into public.legal_acceptances (
        user_id,
        terms_version,
        privacy_version,
        terms_accepted_at,
        privacy_accepted_at
    ) values (
        v_user_id,
        btrim(p_terms_version),
        btrim(p_privacy_version),
        now(),
        now()
    )
    on conflict (user_id, terms_version, privacy_version)
    do update set terms_accepted_at = public.legal_acceptances.terms_accepted_at,
                  privacy_accepted_at = public.legal_acceptances.privacy_accepted_at
    returning * into v_acceptance;

    return v_acceptance;
end;
$$;

revoke all on function public.record_legal_acceptance(text, text) from public, anon;
grant execute on function public.record_legal_acceptance(text, text) to authenticated;
