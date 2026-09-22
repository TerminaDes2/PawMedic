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
