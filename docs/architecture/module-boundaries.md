# Module boundaries

The canonical Gradle graph is declared in `settings.gradle.kts`. Modules that
are not listed there are retained for historical reference and are not part of
the current application graph.

## Core modules

| Module | Owns | Must not own |
| --- | --- | --- |
| `shared:core:common` | dates, result/error primitives, coroutine helpers | feature rules or UI |
| `shared:core:model` | serializable cross-feature identifiers and session models | Supabase client calls |
| `shared:core:auth` | Auth/JWT session port and token lifecycle | role-based screen navigation |
| `shared:core:network` | HTTP and Supabase transport abstractions | business authorization decisions |
| `shared:core:storage` | local cache and secure persistence ports | source-of-truth clinical data |
| `shared:core:design-system` | Compose theme, controls, accessibility primitives | feature-specific workflows |
| `shared:core:testing` | fakes, fixtures, and deterministic test utilities | production behavior |

## Feature modules

The feature-first modules are `auth`, `pets`, `businesses`, `appointments`,
`services`, `medical-records`, `products`, `reports`, and `platform-admin`.
Within a feature:

```text
src/commonMain/kotlin/com/pawsmedic/features/<name>/
  domain/       models, repository interfaces, use cases
  data/         DTOs, data sources, repository implementations
  presentation-common/  state and intent contracts
  presentation-android/ Android-only UI
  presentation-desktop/ Desktop-only UI
```

Domain depends only on core primitives and Kotlin. Data implements domain
ports and may depend on `core:network`, `core:auth`, and serialization.
Presentation depends on domain use cases and design-system primitives. App
modules compose features and provide platform wiring (Koin, navigation,
Supabase configuration); a feature never imports an app module.

## Dependency rules

1. Dependencies point inward: UI -> presentation -> domain; data implements
   domain ports.
2. A feature may use shared core modules but must not reach into another
   feature's `data` or `presentation` package.
3. Cross-feature collaboration uses a domain contract in `core:model` or a
   dedicated use-case interface, not a database table call.
4. No module embeds a service-role key. Repositories receive an authenticated
   client configured by the application.
5. RLS and Edge Functions remain authoritative. A ViewModel's role check is
   navigation only.
6. Platform-specific code stays in source sets; common code remains runnable
   in JVM tests.

This layout keeps pet ownership, tenant operations, and clinical access
testable without a live backend while preserving one consistent request path.
