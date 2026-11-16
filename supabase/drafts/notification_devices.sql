-- Device-registration preparation only; this does not enable notification delivery.
begin;

do $$
begin
    if to_regclass('public.notification_devices') is not null
       or to_regprocedure('public.register_notification_device(text,text)') is not null
       or to_regprocedure('public.unregister_notification_device(text)') is not null then
        raise exception 'notification device objects already exist; inspect them before continuing';
    end if;
end;
$$;

create table public.notification_devices (
    token text primary key,
    user_id uuid not null references auth.users(id) on delete cascade,
    updated_at timestamptz not null default now()
);

alter table public.notification_devices enable row level security;
revoke all on public.notification_devices from public, anon, authenticated;

create function public.register_notification_device(p_token text, p_previous_token text default null)
returns void
language plpgsql
security definer
set search_path = public, pg_temp
as $$
begin
    if auth.uid() is null then
        raise exception 'authentication required';
    end if;
    if p_token is null or length(p_token) < 20 or length(p_token) > 4096 then
        raise exception 'invalid device token';
    end if;
    if p_previous_token is not null and p_previous_token <> p_token then
        delete from public.notification_devices
        where token = p_previous_token and user_id = auth.uid();
    end if;
    insert into public.notification_devices (token, user_id, updated_at)
    values (p_token, auth.uid(), now())
    on conflict (token) do update
        set user_id = excluded.user_id, updated_at = excluded.updated_at;
end;
$$;

create function public.unregister_notification_device(p_token text)
returns void
language plpgsql
security definer
set search_path = public, pg_temp
as $$
begin
    if auth.uid() is null then
        raise exception 'authentication required';
    end if;
    delete from public.notification_devices
    where token = p_token and user_id = auth.uid();
end;
$$;

revoke all on function public.register_notification_device(text, text) from public, anon;
revoke all on function public.unregister_notification_device(text) from public, anon;
grant execute on function public.register_notification_device(text, text) to authenticated;
grant execute on function public.unregister_notification_device(text) to authenticated;

commit;
