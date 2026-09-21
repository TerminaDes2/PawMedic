# Supabase backend templates

These files are deployment templates only. Create a Supabase project and set
secrets using `supabase secrets set`; never put a service-role key in this
repository or in a client app.

## Local workflow

```text
supabase start
supabase db reset
supabase functions serve
```

The ordered migrations create profiles, tenants, businesses, pets, scheduling,
appointments, clinical records, and audit logs with RLS enabled. Review
policies with a security owner before production.

## Edge Functions

* `create-consultation`: validates a veterinary tenant command and inserts a
  consultation with an idempotency key.
* `profile-role`: returns the caller's profile role without exposing private
  account data.

The TypeScript files are intentionally dependency-light skeletons. Add the
project's generated Supabase types and tests before deployment.
