# Database

Migrations in `supabase/migrations` are ordered by timestamp:
profiles, tenants, businesses, pets, services, products, business schedules,
availability blocks, appointments, medical records, clinical consultations,
audit logs, and RLS templates. Apply them with the Supabase CLI. The retained
`20260921000000_initial_schema.sql` is a history marker and performs no writes;
the numbered migrations are canonical.

Every client request carries the Supabase JWT. PostgreSQL policies repeat all
role and tenant checks, including user-pet ownership, authorized clinical
access, superadmin access, and storage path isolation.
