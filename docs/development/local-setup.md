# Local setup

## Prerequisites

Install JDK 17+, Android SDK API 35 (for the Android target), Git, and the
Supabase CLI. Docker Desktop must be running for `supabase start`. The first
Gradle build needs network access to download plugins and dependencies.

## Configure the repository

```text
git clone <repository-url>
cd PawMedic
copy .env.example .env        # Windows
# cp .env.example .env        # macOS/Linux
```

Do not commit `.env`. Run `supabase start`, then use `supabase status` to
replace the local URL, anon key, and service-role placeholders. The anon key
may be passed to a client adapter. The service-role key is for local Edge
Function configuration only and must never enter Android, desktop, logs, or
Git.

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
the app. Enable Google as an Auth provider in Supabase, put the Web client ID
and secret in Supabase's provider settings, and put only that same Web client
ID in `.env` as `GOOGLE_WEB_CLIENT_ID`. The Google client secret must never be
added to `.env` or the Android app. The phone must have Google Play Services
and a Google account available.

The Android `google-services.json` file is required for every Android build,
local and ignored by Git. Obtain it from the Firebase project associated with
the target environment and place it in `apps/mobile-android`; never add the
service-account private key to the app. Appointment notifications are available
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

The mobile app requires `SUPABASE_URL` and `SUPABASE_ANON_KEY` at build time.
Google sign-in also requires `GOOGLE_WEB_CLIENT_ID`; email/password
authentication does not. Desktop apps read the same Supabase URL and anon key
from the root `.env` or environment variables.

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
