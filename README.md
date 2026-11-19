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

## Configuration, Secrets, and Supabase Connection

The shared development project's public client settings are in
`config/client.properties`, and the Android Firebase client configuration is in
`apps/mobile-android/google-services.json`. A normal checkout can use the shared
backend without creating a `.env` file. An optional root `.env` or environment
variables can override individual client settings; `.env` takes precedence.
These settings are distributed with client apps and do not provide authorization:
Supabase RLS and server-side authorization must protect the data.

Never put a service-role key, database password, JWT signing secret, or user
credentials in source control or a client app. `SUPABASE_SERVICE_ROLE_KEY`
belongs only in Supabase Edge Function secrets.

The Supabase client is configured in `shared/core/network`. Public client
configuration is not a substitute for protecting secrets.

## Run the project

On Windows use `gradlew.bat`; on macOS/Linux use `./gradlew`.

The desktop veterinary catalog supports product creation, detail, editing,
availability, publication and required JPG/PNG images (up to 5 MB). Products
must use a category selected from `product_types` in Supabase; the selected
ID is stored in `products.type_id`. Existing products without an image or
category must have those fields filled in when edited. Images are stored
in the public `product-images` bucket: do not upload private information, as
an image URL is accessible even when its product is unpublished.

The veterinary desktop's **Reportes operativos** screen reports total and
completed appointments and groups services from completed appointments for
an inclusive date range in the clinic's time zone. The database derives the
tenant from the authenticated veterinary profile; superadmin accounts cannot
use this tenant report.

After consulting a report, use **PDF** to save its current date range, totals,
completed appointments and services. The PDF contains clinic and patient
information; save it only in an authorized location.

The superadmin audit report requires
`supabase/migrations/20261118000000_superadmin_audit_read.sql` to let
superadmins read audit events. The report includes events recorded in the
selected period; the migration does not reconstruct missing history.

Products and services are entered individually; TXT import is no longer available.

Apply `supabase/migrations/20261116000000_operational_reports.sql` and
`supabase/migrations/20261116000001_product_images.sql`, followed by
`supabase/migrations/20261116000002_public_product_catalog.sql`, followed by
`supabase/migrations/20261117000000_public_product_id_compatibility.sql` and
`supabase/migrations/20261118000000_product_images_tenant_upload.sql`, followed by
`supabase/migrations/20261118000001_fix_product_image_storage_policies.sql`, in that order,
to the Supabase project after the existing migrations. The product image migrations
require permission to create a Storage bucket and policies. Verify policies and
reports with accounts from different clinics before deploying.
If the earlier migrations were already applied, apply only the new ID
compatibility and product-image storage-policy migrations; they update the
public-products function and storage policies without changing product rows.
The public product catalog migration exposes only active, published products
of active clinics to signed-in users; these appear in the clinic profile in the mobile app.
The deployment must allow authenticated users to read `product_types`, allow
the clinic account to write its products, and permit uploads to the
`product-images` bucket. Existing permissive policies can broaden access even
when stricter policies are also present.

To build a Windows portable ZIP with the integrated veterinary and superadmin
interfaces, the local translation worker and a Windows Java runtime, set
`JAVA_HOME` to that runtime and run
`gradlew.bat :apps:desktop-veterinary:portableWindowsZip`. The ZIP is created in
`apps\desktop-veterinary\build\distributions`. Extract it and run
`Iniciar PawMedic.exe`; it opens without a console window, and superadmin access
is available on the login screen. Building the launcher requires the Windows
.NET Framework C# compiler.
Only the Supabase URL and public publishable/anon key are included from `.env`
or environment variables. Never distribute a service-role or secret key.
The application still needs network access to Supabase, although Argos
translation runs locally. Review third-party redistribution licenses before
distributing the ZIP.

Both desktop dashboards provide a persistent dark-mode setting. English is
available for navigation, veterinary reports, the main catalog labels,
parts of clinical records and the superadmin interface. Errors and other
screens are still in Spanish.

An experimental Argos translation bridge is shared by the desktop applications.
In English mode the veterinary catalog automatically translates product and
service names and descriptions; use **Show original** to inspect the saved
text. Selected clinical record fields and application addresses still use
on-demand translation. It is not complete: many screens, errors and
user-generated fields in both desktop applications are not translated yet.

On a Windows development machine with Python 3.10, prepare the model and
standalone worker with `python apps\desktop-veterinary\translation\prepare_argos.py`.
This downloads dependencies and the Spanish-to-English model, verifies the
model's SHA-256, builds the worker and runs a socket-blocked smoke test.
Then run `gradlew.bat :apps:desktop-veterinary:installWithTranslation
:apps:desktop-admin:installWithTranslation`. These tasks create experimental
application folders under each module's `build\install-with-translation`;
they are not Windows installers and still require a compatible Java runtime.
The model's bundled README includes OPUS-MT CC BY 4.0 attribution.

The worker translates on the CPU and uses bounded segments rather than
download-capable sentence detectors. Segmentation and machine translation can
change meaning; consult the original and do not rely on translations for
clinical decisions. The packaged applications and redistribution of all
third-party licenses still need verification before customer use.

The veterinary desktop app can remember a signed-in session on Windows. The
session is protected with Windows DPAPI for the current OS user; the password
is not stored. Without “Recordarme”, closing the app signs out. Remembered
sessions are rechecked for veterinary approval when the app is reopened.
Secure session persistence is currently available only on Windows.

```text
gradlew.bat test
gradlew.bat :shared:features:auth:jvmTest
gradlew.bat :apps:desktop-veterinary:run
gradlew.bat :apps:desktop-admin:run
gradlew.bat :apps:mobile-android:assembleDebug
```

To use an isolated local backend instead of the shared development project,
start it in a second terminal:

```text
supabase start
supabase db reset
supabase functions serve
```

`supabase db reset` applies the timestamp-ordered migrations and seed data.
Use `supabase status` to obtain the local URL and anon key and place those
values in the optional untracked `.env`. See `docs/development/local-setup.md` for
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

Start with [project structure](docs/architecture/project-structure.md),
[architecture overview](docs/architecture/architecture-overview.md),
[local setup](docs/development/local-setup.md), and
[security/tenancy](docs/security/multi-tenancy.md). The complete documentation
index is the `docs/` tree. Existing product notes and assumptions remain in
`ADR-0001-documentation-assumptions.md`, `PAWSMEDIC_PROJECT_CONTEXT.md`,
`CATALOGO_FEATURES_FDD.md`, `CRONOGRAMA_FEATURES.md`,
`CRONOGRAMA_FEATURES_ATOMICOS.md`, and
`CU-CLI-01_REGISTRAR_CONSULTA_CLINICA.md`.
