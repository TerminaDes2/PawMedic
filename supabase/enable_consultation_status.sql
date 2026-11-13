-- Run separately before consultation_workflow.sql.
-- PostgreSQL must commit a new enum value before functions can use it.
alter type public.appointment_status add value if not exists 'IN_PROGRESS';
