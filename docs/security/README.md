# Security

- Roles are exactly `USER`, `VETERINARY_BUSINESS`, and `SUPERADMIN`.
- The JWT identifies the caller; role and tenant membership are resolved from
  `profiles` and checked again by PostgreSQL RLS.
- Clinical data is readable by a pet owner or an authorized veterinary tenant.
- Administrative mutations use authenticated Edge Functions and audited RPCs.
- Supabase service-role credentials exist only as Edge Function secrets.
- Storage paths begin with a user or tenant UUID and are policy checked.
- Never log access tokens, passwords, service-role keys, or private medical
  content.
