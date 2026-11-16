-- Development draft: run only after testing and creating both Vault secrets.
-- Required secret names: pawmedic_notification_endpoint and pawmedic_notification_dispatch_secret.
-- The endpoint must use HTTPS and point to the deployed appointment-notifications function.
begin;

do $$
declare
    endpoint text;
    dispatch_secret text;
begin
    if exists (select 1 from cron.job where jobname = 'pawmedic-appointment-notifications') then
        raise exception 'notification scheduler already exists; inspect it before continuing';
    end if;
    select decrypted_secret into strict endpoint from vault.decrypted_secrets
        where name = 'pawmedic_notification_endpoint';
    select decrypted_secret into strict dispatch_secret from vault.decrypted_secrets
        where name = 'pawmedic_notification_dispatch_secret';
    if endpoint !~ '^https://[a-z0-9]+\.supabase\.co/functions/v1/appointment-notifications$'
       or length(dispatch_secret) < 32 then
        raise exception 'notification endpoint or dispatch secret is invalid';
    end if;
end;
$$;

select cron.schedule(
    'pawmedic-appointment-notifications',
    '* * * * *',
    $job$
    select net.http_post(
        url := (select decrypted_secret from vault.decrypted_secrets where name = 'pawmedic_notification_endpoint'),
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'x-dispatch-secret', (select decrypted_secret from vault.decrypted_secrets where name = 'pawmedic_notification_dispatch_secret')
        ),
        body := '{}'::jsonb,
        timeout_milliseconds := 20000
    );
    $job$
);

commit;
