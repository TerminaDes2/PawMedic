# Local setup

## Prerequisites

Install JDK 17+, Android SDK API 35 (for the Android target), Git, and the
Supabase CLI only if you need a local backend. Docker Desktop must be running
for `supabase start`. The first Gradle build needs network access to download
plugins and dependencies.

## Configure the repository

```text
git clone <repository-url>
cd PawMedic
```

The checked-in `config/client.properties` and Android `google-services.json`
configure the clients for the shared development project. No `.env` is needed
for that project. These client values are public, so RLS and server-side
authorization must protect all data. To use a different backend, optionally
create a root `.env` with `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and
`GOOGLE_WEB_CLIENT_ID`. Nonempty `.env` values override environment variables,
which override the shared defaults.

Do not commit `.env`. For an isolated local backend, run `supabase start` and
use `supabase status` to obtain its URL and anon key. The service-role key is
for server-side Edge Function configuration only and must never enter Android,
desktop, logs, or Git.

For email registration in Android and Desktop, enable email confirmation in
Supabase Auth and set the confirmation email template to display `{{ .Token }}`
so users receive the six-digit code entered by the app. Configure SMTP in
Supabase for reliable delivery to real email addresses; the built-in test SMTP
service is rate-limited and is not intended for production delivery.

For password recovery, configure the Supabase Auth **Reset Password** email
template to display `{{ .Token }}`. The mobile app verifies this code and
lets the user set a new password. Verify delivery and expiry with the project's
actual email provider before using the flow in production. Review the template
with the team before changing it: it is shared by all clients, and replacing
the existing recovery link with a code could break other clients. The mobile
recovery flow uses a separate in-memory session, rather than persisting the
temporary recovery session as the user's normal session.

For Google on Android, create a Web OAuth client and an Android OAuth client
for `com.pawsmedic.mobile` with the signing certificate SHA-1 used to install
the app. Google sign-in requires the SHA-1 of each developer's debug signing
key to be registered with Firebase/Google; the checked-in client configuration
does not authorize an unknown signing key automatically. Enable Google as an
Auth provider in Supabase and keep the Google client secret in Supabase's
provider settings, never in the Android app or Git. The phone must have Google
Play Services and a Google account available.

The checked-in Android `google-services.json` is the public client configuration
for the shared development Firebase project, not a service-account key.
When targeting a different Firebase project, replace it locally with that
project's Android client configuration; never add a service-account private key
to the app or Git. Appointment notifications are available
in Android settings without a build flag, but each user must opt in and grant
the Android notification permission. Firebase configuration alone does not
enable delivery: each target Supabase environment also requires the database
objects, dispatcher, scheduler and server-side secrets. Applying the outbox draft installs a
trigger on appointment inserts and changes to status, time or pet, so use an
isolated project when possible; on a shared development project, coordinate
downtime and recovery first. The dispatcher needs
a protected server-side Firebase service account and a separate scheduler
secret; never put either credential in the Android app or Git. Delivery can
be delayed or missed due to Android, network or Firebase restrictions, and
retries may produce duplicates.

Android settings store the selected Spanish or English language on the device.
The selection applies to the mobile interface, its date picker and appointment
notifications, including messages received while the app is closed. Names and
other content entered by users or provided by clinics, server errors, and emails
from the shared Supabase templates are not automatically translated. Known pet
catalog values are translated for display without changing stored values.

The scheduler draft requires `pg_cron`, `pg_net` and Vault. Its invocation
uses a dedicated dispatch secret instead of a project service key; the Edge
gateway must permit that request while the function validates the secret.
Supabase warns that database login roles can read request headers while
`pg_net` queues them, so Vault does not by itself protect that dispatch
secret from database users. Review database access and rotate the secret if
exposed. A per-minute scheduler can also incur Edge Function and database
usage even when there are no notifications.

The notification SQL can be exercised against an isolated embedded PostgreSQL
instance with `npm install --prefix supabase/tests` followed by
`npm test --prefix supabase/tests`. The dispatcher mock tests use Deno:
`deno test --allow-env=NOTIFICATION_DISPATCH_SECRET,SUPABASE_URL,SUPABASE_SERVICE_ROLE_KEY,FIREBASE_SERVICE_ACCOUNT supabase/tests/notification-dispatch.test.ts`.

The Android appearance setting supports light, dark and system themes and is
stored locally on the device.

The mobile app reads its client settings at build time. Desktop apps include
the shared public configuration as a resource and can also read overrides from
the root `.env` or environment variables at runtime.

After applying migrations, create the initial Superadmin account through
Supabase Auth, confirm its email, then promote its profile from the trusted
Supabase SQL editor:

```sql
update public.profiles
set role = 'SUPERADMIN'
where email = 'your-admin-email@example.com';
```

Do not expose Superadmin registration in either client. Veterinary
applications are created only after email verification; the Superadmin
approval RPC creates the tenant and assigns the approved owner's role.

## Start Supabase

```text
supabase start
supabase db reset
supabase functions serve
```

`db reset` applies every migration in timestamp order and loads `seed.sql`.
The migrations create profiles, tenants, businesses, pets, scheduling,
appointments, medical records, consultations, audit logs, policies, and
transactional RPCs. Use `supabase stop` when finished. Inspect the local
dashboard with the URL printed by `supabase status`.

## Build and run clients

```text
gradlew.bat test
gradlew.bat :shared:features:auth:jvmTest
gradlew.bat :apps:desktop-veterinary:run
gradlew.bat :apps:desktop-admin:run
gradlew.bat :apps:mobile-android:assembleDebug
```

Use `./gradlew` on macOS/Linux. Android Studio can open the root project and
run `apps/mobile-android`; set the SDK location in an untracked
`local.properties` if Android Studio does not discover it. Desktop apps read
the same public endpoint configuration when the real Supabase adapter is
wired.

## Troubleshooting

* If Gradle cannot resolve dependencies, verify JDK selection and network
  access, then retry without deleting source files.
* If Supabase containers fail, check Docker and run `supabase stop` followed by
  `supabase start`.
* If a request returns 401, restore the Auth session and verify the bearer
  token is forwarded; do not use the service role as a workaround.
* If a request returns no rows, check profile role, active tenant membership,
  pet ownership, and the relevant RLS policy.
