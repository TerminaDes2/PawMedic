-- Apply to databases with the existing profiles columns used by mobile registration.
-- Only new auth users are affected; existing profiles are not backfilled.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_is_google boolean := coalesce(new.raw_app_meta_data ->> 'provider' = 'google', false);
    v_first_name text := coalesce(new.raw_user_meta_data ->> 'first_name', '');
    v_last_name text := coalesce(new.raw_user_meta_data ->> 'last_name', '');
    v_username text := coalesce(new.raw_user_meta_data ->> 'username', '');
    v_phone text := coalesce(new.raw_user_meta_data ->> 'phone', '');
    v_display_name text;
    v_avatar_url text;
begin
    if v_is_google then
        v_first_name := coalesce(nullif(v_first_name, ''), new.raw_user_meta_data ->> 'given_name', '');
        v_last_name := coalesce(nullif(v_last_name, ''), new.raw_user_meta_data ->> 'family_name', '');
        v_avatar_url := coalesce(
            nullif(new.raw_user_meta_data ->> 'avatar_url', ''),
            nullif(new.raw_user_meta_data ->> 'picture', '')
        );
    end if;

    v_display_name := trim(concat(v_first_name, ' ', v_last_name));
    if v_is_google and v_display_name = '' then
        v_display_name := coalesce(
            nullif(new.raw_user_meta_data ->> 'full_name', ''),
            nullif(new.raw_user_meta_data ->> 'name', ''),
            ''
        );
    end if;

    insert into public.profiles (
        id, role, first_name, last_name, username, phone,
        display_name, email, avatar_url
    ) values (
        new.id, 'USER'::public.profile_role, v_first_name, v_last_name,
        v_username, v_phone, v_display_name, coalesce(new.email, ''), v_avatar_url
    )
    on conflict (id) do nothing;

    return new;
end;
$$;
