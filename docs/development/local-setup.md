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

For Google on Android, create a Web OAuth client and an Android OAuth client
for `com.pawsmedic.mobile` with the signing certificate SHA-1 used to install
the app. Enable Google as an Auth provider in Supabase, put the Web client ID
and secret in Supabase's provider settings, and put only that same Web client
ID in `.env` as `GOOGLE_WEB_CLIENT_ID`. The Google client secret must never be
added to `.env` or the Android app. The phone must have Google Play Services
and a Google account available.

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
