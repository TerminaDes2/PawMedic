# PawMedic
# PawMedic

PawMedic is a Kotlin Multiplatform veterinary-care platform. Pet owners use
the Android client, veterinary businesses use the veterinary desktop client,
and platform operators use the admin desktop client. The backend is Supabase
PostgreSQL with Auth, Row Level Security (RLS), and authenticated Edge
Functions.

## Project structure

```
apps/
  mobile-android       Android Compose app for USER
  desktop-veterinary   Compose Desktop app for VETERINARY_BUSINESS
  desktop-admin        Compose Desktop app for SUPERADMIN
shared/
  core/common          cross-platform utilities
  core/model           shared value and wire models
  core/auth            session and Auth/JWT ports
  core/network         HTTP/Supabase transport ports
  core/storage         local persistence ports
  core/design-system   shared Compose design primitives
  core/testing         test fixtures and helpers
  features/*           feature-first vertical slices
supabase/
  migrations/          ordered schema, constraints, and RLS
  functions/           authenticated Edge Functions
  seed.sql             local development data
docs/
  architecture/        system and module boundaries
  security/            tenancy and RLS rules
  database/            data model and persistence conventions
  development/         setup and testing
  decisions/            architecture decision records
  diagrams/             Mermaid system diagrams
```

The modules listed in `settings.gradle.kts` are canonical. The top-level
`core/`, `features/`, and legacy app directories are retained as historical
scaffold material and are not included in the build.

## Requirements

* JDK 17 or newer
* Android SDK with API 35 platform/build tools for the Android target
* Supabase CLI for local database and Edge Function work
* Network access on the first Gradle invocation

## Configuration and secrets

Copy `.env.example` to `.env` for local Supabase tooling. The public
`SUPABASE_URL` and `SUPABASE_ANON_KEY` may be used by client adapters; they are
not secrets, but should still be supplied by deployment configuration. Never
put a service-role key, database password, JWT signing secret, or user
credentials in source control or a client app. `SUPABASE_SERVICE_ROLE_KEY`
belongs only in Supabase Edge Function secrets.

The current auth adapter is deliberately credential-free and safe to compile.
When the real adapter is wired, map the environment values into the platform
configuration rather than committing them.

## Run the project

On Windows use `gradlew.bat`; on macOS/Linux use `./gradlew`.

```text
gradlew.bat test
gradlew.bat :shared:features:auth:jvmTest
gradlew.bat :apps:desktop-veterinary:run
gradlew.bat :apps:desktop-admin:run
gradlew.bat :apps:mobile-android:assembleDebug
```

Start the local backend in a second terminal:

```text
supabase start
supabase db reset
supabase functions serve
```

`supabase db reset` applies the timestamp-ordered migrations and seed data.
Use `supabase status` to obtain the local URL and anon key and place those
values in the untracked `.env`. See `docs/development/local-setup.md` for
Android, desktop, and Supabase troubleshooting.

## Request and authorization model

Every feature follows **UI -> ViewModel -> Use Case -> Repository ->
Supabase API/Edge Function -> Auth/JWT -> PostgreSQL/RLS -> authorized
response**. The UI may hide routes based on the profile role, but only the
JWT, authenticated Edge Functions, PostgreSQL constraints, and RLS policies
authorize data. `USER` owns and books for their pets; `VETERINARY_BUSINESS`
operates inside its approved tenant; `SUPERADMIN` manages platform operations
and audited business approval.

## Documentation

Start with [architecture overview](docs/architecture/architecture-overview.md),
[local setup](docs/development/local-setup.md), and
[security/tenancy](docs/security/multi-tenancy.md). The complete documentation
index is the `docs/` tree. Existing product notes and assumptions remain in
`ADR-0001-documentation-assumptions.md`, `PAWSMEDIC_PROJECT_CONTEXT.md`,
`CATALOGO_FEATURES_FDD.md`, `CRONOGRAMA_FEATURES.md`,
`CRONOGRAMA_FEATURES_ATOMICOS.md`, and
`CU-CLI-01_REGISTRAR_CONSULTA_CLINICA.md`.
