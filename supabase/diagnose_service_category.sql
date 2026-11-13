-- Read-only diagnosis: run in Supabase SQL Editor and share the results after
-- removing any sensitive information from function definitions.
select
    c.column_name,
    c.data_type,
    c.udt_schema,
    c.udt_name,
    c.is_nullable,
    c.column_default
from information_schema.columns c
where c.table_schema = 'public'
  and c.table_name = 'services'
  and c.column_name = 'category';

select pg_get_constraintdef(c.oid) as constraint_definition
from pg_constraint c
where c.conrelid = 'public.services'::regclass
  and c.contype in ('c', 'f');

select e.enumlabel as allowed_value
from pg_attribute a
join pg_type t on t.oid = a.atttypid
join pg_enum e on e.enumtypid = t.oid
where a.attrelid = 'public.services'::regclass
  and a.attname = 'category'
order by e.enumsortorder;

select
    n.nspname as table_schema,
    c.relname as table_name,
    t.tgname as trigger_name,
    pg_get_triggerdef(t.oid) as trigger_definition,
    pg_get_functiondef(t.tgfoid) as function_definition
from pg_trigger t
join pg_class c on c.oid = t.tgrelid
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public'
  and c.relname in ('tenants', 'businesses', 'services')
  and not t.tgisinternal
order by c.relname, t.tgname;
