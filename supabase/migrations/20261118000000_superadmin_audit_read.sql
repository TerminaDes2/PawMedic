-- Allow the superadmin to read audit events while retaining row-level security.
alter table public.audit_logs enable row level security;

grant select on public.audit_logs to authenticated;

drop policy if exists audit_logs_superadmin_report_read on public.audit_logs;
create policy audit_logs_superadmin_report_read
    on public.audit_logs
    for select
    to authenticated
    using (public.is_superadmin());
