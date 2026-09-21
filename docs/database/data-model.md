# Data model

The schema is applied by timestamp-ordered migrations in
`supabase/migrations`. UUIDs identify domain rows; timestamps are stored as
`timestamptz`; money is integer cents; and status fields use PostgreSQL enums.

## Identity and tenancy

* `auth.users` is managed by Supabase Auth.
* `profiles` extends an auth user with email, display name, exact role, and
  optional `tenant_id`.
* `tenants` represents an approved veterinary business and has `PENDING`,
  `ACTIVE`, `SUSPENDED`, or `REJECTED` status.
* `businesses` stores an application and approval state. `tenant_id` is null
  until approval; `approved_by` and `approved_at` identify the superadmin
  action.

## Pet and clinical relationships

```text
profiles (USER) 1 ── * pets
pets 1 ── * appointments
pets 1 ── * medical_records
pets 1 ── * clinical_consultations
tenants 1 ── * services/products/schedules/availability_blocks
tenants 1 ── * appointments/medical_records/clinical_consultations
services 1 ── * appointments
appointments 0..1 ── * clinical_consultations
```

`pets.owner_id` is the ownership boundary. A medical record and consultation
also carry `tenant_id` and an author/veterinarian identity so RLS can enforce
both the pet relationship and clinical tenant access. Attachments are JSON
metadata; the file itself is isolated by storage-object path policies.

## Operational tables

* `services`: tenant catalog, duration, cents price, and active flag.
* `products`: tenant catalog with unique `(tenant_id, sku)`, stock, and price.
* `business_schedules`: one opening interval per tenant weekday.
* `availability_blocks`: tenant closures or blocked intervals and creator.
* `appointments`: tenant, pet, service, booker, interval, enum status, and
  unique idempotency key. Overlap checks are performed in the critical
  booking RPC.
* `audit_logs`: actor, optional tenant, action, entity, metadata, and time.

## Invariants and access

Foreign keys cascade pet-owned clinical rows when a pet is removed and cascade
tenant catalog rows when a tenant is removed where defined. Check constraints
reject empty names, non-positive weights, negative prices/stock, invalid
intervals, and invalid weekdays. Indexes support owner, tenant, status, and
time-window lookups.

RLS determines the rows returned after the request passes through Auth/JWT.
The expected path is **UI -> ViewModel -> Use Case -> Repository -> Supabase
API/Edge Function -> Auth/JWT -> PostgreSQL/RLS -> authorized response**.
The data model must not be treated as a substitute for policies: every new
relationship needs a corresponding policy and test.
