# Multi-tenancy and authorization

PawMedic separates platform users, veterinary tenants, and pet owners. Roles
are exactly `USER`, `VETERINARY_BUSINESS`, and `SUPERADMIN`; there are no
client-defined role aliases.

## Tenant lifecycle

1. A veterinary business submits a `businesses` application with `PENDING`
   status. It has no tenant yet.
2. An authenticated `SUPERADMIN` invokes the approval Edge Function.
3. The transactional RPC creates an `ACTIVE` tenant, links the business,
   assigns the submitting veterinary profile to that tenant, and writes an
   `APPROVE_BUSINESS` audit log.
4. Tenant users operate only on rows whose `tenant_id` matches their profile.
5. `SUSPENDED` or `REJECTED` tenants cannot use tenant operations; the
   application routes to a status screen and RLS continues to deny access.

The tenant is the isolation boundary for businesses, services, products,
schedules, availability blocks, appointments, medical records,
consultations, and tenant-scoped audit entries. A row must carry a tenant ID
when its ownership is tenant-scoped; repositories must not infer a tenant
from an arbitrary client-supplied name.

## Access rules

* `USER` reads and mutates their own `pets` rows (`owner_id = auth.uid()`),
  books appointments for their pets, and reads clinical history for those
  pets when the record is authorized.
* `VETERINARY_BUSINESS` can operate only within the active tenant associated
  with the profile. Clinical reads and consultation writes require both
  tenant membership and the relevant pet/appointment relationship.
* `SUPERADMIN` performs platform administration, business approval, status
  changes, and audit inspection through authenticated, audited operations.

RLS helpers such as `is_superadmin()`, `is_tenant_member(tenant_id)`, and
`can_access_pet(pet_id)` centralize repeated checks. PostgreSQL evaluates
them for each request using `auth.uid()`. The UI may hide an unavailable
route, but it never grants access.

## Operational safeguards

Service-role credentials exist only in Edge Function secrets. Edge Functions
validate the caller's JWT before invoking a privileged RPC and return a
minimal authorized response. Critical booking and consultation commands use
idempotency keys and transaction boundaries. Audit logs record actor, tenant,
action, entity, and timestamp without storing access tokens or passwords.

Tenant IDs, user IDs, and pet IDs in storage paths are checked by storage RLS.
Logs and errors must redact JWTs, service-role keys, passwords, and private
medical content.
