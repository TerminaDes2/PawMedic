# Authenticated request flow

```mermaid
flowchart LR
    UI[Compose UI] --> VM[StateFlow ViewModel]
    VM --> UC[RestoreSessionUseCase]
    UC --> REPO[AuthRepository]
    REPO --> API[Supabase API / Edge Function]
    API --> JWT[Auth / JWT]
    JWT --> DB[PostgreSQL / RLS]
    DB --> AUTHZ[Authorized response]
    AUTHZ --> VM
    VM --> UI
```

The same sequence is used by Android and desktop. UI routing maps
`USER` to the user app, `VETERINARY_BUSINESS` to the veterinary tenant
dashboard, and `SUPERADMIN` to the platform administration dashboard.
