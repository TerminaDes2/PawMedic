-- Ejecutar en el SQL Editor de Supabase para habilitar el reporte de auditoria.
begin;

alter table public.audit_logs enable row level security;

grant select on public.audit_logs to authenticated;

drop policy if exists audit_logs_superadmin_report_read on public.audit_logs;
create policy audit_logs_superadmin_report_read
    on public.audit_logs
    for select
    to authenticated
    using (public.is_superadmin());

commit;
