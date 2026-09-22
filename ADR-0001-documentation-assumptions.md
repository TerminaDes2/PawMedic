# ADR-0001: Scaffold assumptions when product source documents are absent

- Status: accepted
- Date: 2026-09-21
- Decision type: project bootstrap

## Context

The repository initially contained only `README.md` with the title `PawMedic`.
The requested source-of-truth documents were absent:

* `PAWSMEDIC_PROJECT_CONTEXT.md`
* `CATALOGO_FEATURES_FDD.md`
* `CRONOGRAMA_FEATURES.md`
* `CRONOGRAMA_FEATURES_ATOMICOS.md`
* `CU-CLI-01_REGISTRAR_CONSULTA_CLINICA.md`

No existing API contract, visual system, domain model, release plan, or
Supabase project configuration could therefore be verified.

## Decision

Create a compilable, backend-safe vertical slice and add placeholder product
documents that clearly mark assumptions. The first slice contains:

1. A session/authentication port and a Supabase-ready adapter.
2. A profile model with the canonical roles `USER`, `VETERINARY_BUSINESS`,
   and `SUPERADMIN` (the historical bootstrap used different placeholders).
3. Flow-backed state holders for auth and profile screens.
4. Android, patient desktop, and clinic desktop entry points.
5. A relational Supabase schema with conservative RLS templates.

The adapter does not call a live service until a real URL and public anon key
are supplied by deployment configuration. No service-role secret is accepted
by client code.

## Assumptions

* A user has one profile and may own many pets.
* A consultation belongs to a pet and a clinic-side veterinarian.
* Authentication is email/password for this first slice; social login and
  password recovery remain future features.
* Role checks are enforced by database RLS in addition to UI visibility.
* UTC timestamps and UUID primary keys are used in Supabase.
* Product details in the companion documents are planning defaults, not
  validated requirements.

## Consequences

The module boundaries and ports can remain stable while the real Supabase
transport is added. Product owners must replace the marked placeholders with
approved requirements before building billing, prescriptions, notifications,
or clinical workflows. Any change to the assumptions should add a new ADR.
