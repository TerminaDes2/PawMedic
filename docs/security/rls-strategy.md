# RLS strategy

Row Level Security is the final authorization boundary. Every public data
table is created with RLS enabled in the ordered migrations, and policies
repeat the role, tenant, and ownership checks even when an application route
already filtered the request.

## Policy layers

1. **Identity**: Supabase Auth supplies `auth.uid()` from the bearer JWT.
2. **Profile**: `profiles` maps the identity to one of the three roles and,
   for veterinary users, a `tenant_id`.
3. **Ownership**: pet policies require `owner_id = auth.uid()` for owner
   writes and use `can_access_pet` for authorized clinical reads.
4. **Tenant membership**: tenant tables require
   `is_tenant_member(row.tenant_id)`, which allows an active veterinary
   membership or explicit superadmin access.
5. **Command authorization**: privileged or race-sensitive mutations run in an
   authenticated Edge Function and an audited PostgreSQL RPC.
6. **Storage isolation**: `pet-files` paths begin with the authenticated user
   or an authorized tenant UUID.

## Current policy intent

* Profiles are readable by the subject or a superadmin; ordinary updates are
  self-scoped.
* Pets are CRUD-accessible by their owner, with clinical read access only
  through an authorized care relationship.
* Businesses, services, products, schedules, and availability blocks are
  tenant-isolated.
* Appointments are accessible to the booker or the authorized tenant.
* Medical records and consultations are readable by the pet owner or an
  authorized veterinary tenant; consultation inserts require the veterinarian
  identity and tenant membership.
* Tenants are visible to members and superadmins; audit logs are visible to
  superadmins or their actor.

Policies are `USING` predicates for visible/existing rows and `WITH CHECK`
predicates for inserted or updated rows. Every new table must add both where
applicable, an index supporting the policy's tenant/owner lookup, and a
negative test proving that a different user or tenant receives no rows.

## Edge Functions and RPCs

The client sends its bearer token to an Edge Function. The function validates
the request and uses the service role only server-side to call a narrow RPC,
such as business approval or critical booking. The RPC validates state,
prevents races or duplicate commands, writes the audit record, and returns
only the resulting row. No client can call a service-role client directly.

RLS must remain correct if an API route is called manually, a client is
modified, or a role is changed while a session is open. After schema or policy
changes, run local Supabase tests for user-pet ownership, cross-tenant
isolation, clinical access, superadmin operations, storage paths, and
idempotency.
