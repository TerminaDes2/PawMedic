# Module dependencies

```mermaid
flowchart TD
    APP[Platform app modules] --> UI[Feature presentation]
    UI --> VM[Feature ViewModels]
    VM --> DOM[Feature domain]
    DATA[Feature data adapters] --> DOM
    DATA --> NET[core/network]
    DATA --> AUTH[core/auth]
    DOM --> MODEL[core/model]
    DOM --> COMMON[core/common]
    UI --> DESIGN[core/design-system]
    APP --> CORE[core/storage and testing]
    DATA --> SUPA[Supabase API / Edge Function]
    SUPA --> JWT[Auth/JWT]
    JWT --> RLS[(PostgreSQL/RLS)]
```

Arrows point from a consumer to a dependency. Feature domain code does not
depend on Compose, Supabase SDK types, or another feature's data layer.
`settings.gradle.kts` is the authoritative list of modules.
