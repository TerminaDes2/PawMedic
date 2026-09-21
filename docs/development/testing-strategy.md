# Testing strategy

Tests verify behavior at the same boundaries used in production. The
canonical request path is **UI -> ViewModel -> Use Case -> Repository ->
Supabase API/Edge Function -> Auth/JWT -> PostgreSQL/RLS -> authorized
response**; unit tests replace the outer transport with fakes while
integration tests exercise the backend boundary.

## Test levels

| Level | Scope | Examples |
| --- | --- | --- |
| Domain/use case | JVM, no network | pet ownership validation, appointment interval rules, status transitions |
| Repository/data | fake transport and DTOs | mapping, error translation, idempotency key propagation |
| ViewModel | `StateFlow` and test dispatcher | loading/success/error/unauthorized states, role routing |
| UI | Compose/platform | role-specific destinations, accessible controls, no cross-role route |
| Edge Function | local Supabase | JWT required, input validation, narrow authorized response, audit event |
| PostgreSQL/RLS | local database and pgTAP/integration | cross-user pets, cross-tenant rows, clinical access, storage paths |
| End-to-end | local clients plus Supabase | sign-in through authorized response for each role |

## Required authorization matrix

The backend suite must include positive and negative cases for:

* `USER` can CRUD only their own pets and book for an owned pet.
* A different `USER` cannot read or change that pet, appointment, or clinical
  record.
* `VETERINARY_BUSINESS` can manage services, products, schedules, and
  availability only for its active tenant.
* A veterinary user cannot see another tenant's catalog, appointment, or
  clinical data.
* An authorized veterinary user can read a patient's clinical history and
  create a consultation; an unrelated tenant cannot.
* `SUPERADMIN` can approve a pending business and the transaction creates the
  tenant, links the profile, and writes an audit row.
* Suspended tenants, missing JWTs, invalid roles, and storage paths outside
  the user/tenant prefix are denied.
* Repeating a critical booking or consultation command with one idempotency
  key does not duplicate data; overlapping bookings are rejected.

## Running tests

```text
gradlew.bat test
gradlew.bat :shared:features:auth:jvmTest
supabase db reset
supabase test db
```

The last command depends on the local Supabase CLI version and any pgTAP or
integration tests added under `supabase/tests`. Tests use seeded, disposable
data and never require production credentials. Do not assert authorization
only through hidden UI controls; assert the denied database response.
