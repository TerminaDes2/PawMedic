# Navigation and role routing

PawMedic has one authentication entry point and role-specific application
surfaces. The role comes from the authenticated profile after the Auth/JWT
session is restored. It is a routing input, not a security credential.

## Role destinations

| Role | Application surface | Allowed navigation |
| --- | --- | --- |
| `USER` | Android patient app | Home, owned pets, pet detail, appointment booking, appointment history, authorized medical records, profile |
| `VETERINARY_BUSINESS` | Veterinary desktop app | Tenant dashboard, appointments, services, products, schedules, availability blocks, patient lookup for authorized care, consultations, tenant reports |
| `SUPERADMIN` | Admin desktop app | Business applications, tenant status, platform reports, audit log, administrative status actions |

`USER` can create and edit only their own pet profiles. A veterinary user sees
tenant operations and clinical records only when the database relationship
authorizes that access. `SUPERADMIN` has platform-wide administrative access
through audited operations; this does not turn a client-side route into an
implicit service-role connection.

## Routing algorithm

1. Restore the Supabase Auth session.
2. If no valid session exists, show sign-in and registration.
3. Fetch the caller's profile through the authenticated repository (or the
   `profile-role` Edge Function).
4. Validate the exact enum value: `USER`, `VETERINARY_BUSINESS`, or
   `SUPERADMIN`.
5. Require an active `tenant_id` for veterinary tenant operations. A pending
   business application is routed to application status, not the tenant
   dashboard.
6. Select the matching app shell and reset any route state from a previous
   role.
7. On token refresh, profile change, sign-out, or a 401, repeat the decision.

Unknown roles, missing profiles, suspended tenants, and expired sessions fail
closed to a safe status/sign-in screen. Deep links are checked against the
same role route table before rendering. Navigation guards improve user
experience only; every repository request still carries the JWT and is
enforced by PostgreSQL RLS or an authenticated Edge Function.

## Screen data flow

**UI -> ViewModel -> Use Case -> Repository -> Supabase API/Edge Function ->
Auth/JWT -> PostgreSQL/RLS -> authorized response**

The ViewModel exposes loading and unauthorized states rather than attempting a
second query with elevated credentials. A denied response is displayed as a
permission or unavailable state, and is logged without tokens or clinical
content.
