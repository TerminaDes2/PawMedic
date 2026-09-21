# Architecture overview

PawMedic is a Kotlin Multiplatform monorepo with three user-facing
applications and a Supabase backend. The applications share domain contracts,
state models, networking ports, and design primitives, while each feature is
implemented as a vertical slice. The backend is the authorization boundary:
Supabase Auth issues JWTs, PostgreSQL enforces constraints and RLS, and Edge
Functions perform authenticated, audited, or transactional commands.

## Applications and roles

| Application | Primary role | Responsibility |
| --- | --- | --- |
| `apps/mobile-android` | `USER` | Maintain owned pets, request appointments, and read authorized clinical history |
| `apps/desktop-veterinary` | `VETERINARY_BUSINESS` | Operate an approved tenant, manage services/schedules, and record consultations |
| `apps/desktop-admin` | `SUPERADMIN` | Approve businesses, manage platform status, inspect audit data, and administer tenants |

The role is stored in `public.profiles.role` and associated with a
`tenant_id` for veterinary business users. A pending business application may
exist before it has a tenant. A business approval command creates the tenant,
links the profile, and records an audit event in one transaction.

## Layered request path

The non-negotiable application boundary is:

**UI -> ViewModel -> Use Case -> Repository -> Supabase API/Edge Function ->
Auth/JWT -> PostgreSQL/RLS -> authorized response**

* **UI** renders Compose state and emits user intent. It never calls Supabase.
* **ViewModel** owns screen state (`StateFlow`), loading, retry, and error
  presentation. It does not decide whether a record is authorized.
* **Use Case** expresses a business action such as `BookAppointment`,
  `CreateConsultation`, or `ApproveBusiness`.
* **Repository** is a domain port implementation. It maps DTOs and transport
  errors to domain results and chooses a table API, RPC, or Edge Function.
* **Supabase API/Edge Function** carries the access token. Edge Functions are
  required for privileged, audited, or race-sensitive commands.
* **Auth/JWT** identifies `auth.uid()`. Role and tenant membership are looked
  up from `profiles`; client claims are not trusted as authorization.
* **PostgreSQL/RLS** applies constraints and policies for every row operation.
* **Authorized response** contains only rows allowed by RLS and is mapped back
  to state; an unauthorized request is not converted into a client-side
  workaround.

## Module shape

Each feature under `shared/features/<feature>` owns its domain model,
repository contract, use cases, data adapters, and presentation state. Common
code belongs in `shared/core`; it must not import a concrete feature. See
[module boundaries](module-boundaries.md) for dependency rules.

The architecture deliberately keeps UI and Supabase SDK details at the edge.
This permits JVM unit tests for use cases and ViewModels and prevents a screen
from bypassing RLS by accidentally using a service-role client.
