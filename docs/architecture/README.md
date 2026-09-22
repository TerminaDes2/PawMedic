# PawMedic architecture

PawMedic is a Kotlin Multiplatform monorepo. `apps/mobile-android`,
`apps/desktop-veterinary`, and `apps/desktop-admin` compose shared feature
modules. Each feature keeps domain models, repository contracts, and use cases
independent from Supabase and UI frameworks. Data adapters implement those
contracts and presentation layers consume use cases through ViewModels.

Request flow: **UI -> ViewModel -> Use Case -> Repository -> Supabase API /
Edge Function -> Auth / JWT -> PostgreSQL / RLS -> authorized response**.

The canonical modules are listed in `settings.gradle.kts`; the original
`core`, `features`, and legacy app folders remain on disk as historical
scaffold material and are not included in the build.
