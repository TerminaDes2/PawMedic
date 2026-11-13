-- Read-only inspection of the deployed booking and medical-record schema.
select 'column' as kind, c.table_name || '.' || c.column_name as name,
       format('%s; nullable=%s; default=%s', c.udt_name, c.is_nullable, coalesce(c.column_default, '(none)')) as definition
from information_schema.columns c
where c.table_schema = 'public'
  and c.table_name in ('appointments', 'medical_records', 'clinical_consultations')
union all
select 'enum', t.typname, string_agg(e.enumlabel, ', ' order by e.enumsortorder)
from pg_type t join pg_namespace n on n.oid = t.typnamespace
join pg_enum e on e.enumtypid = t.oid
where n.nspname = 'public' and t.typname in ('appointment_status', 'consultation_status')
group by t.typname
union all
select 'trigger', c.relname || '.' || t.tgname, pg_get_triggerdef(t.oid)
from pg_trigger t join pg_class c on c.oid = t.tgrelid
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public' and c.relname in ('appointments', 'medical_records', 'clinical_consultations')
  and not t.tgisinternal
union all
select 'function', p.proname, pg_get_functiondef(p.oid)
from pg_proc p join pg_namespace n on n.oid = p.pronamespace
where n.nspname = 'public' and p.proname in
    ('transition_appointment_status', 'guard_appointment_updates', 'get_owned_appointments')
order by kind, name;
