# Role navigation

```mermaid
flowchart TD
    START[Launch] --> SESSION{Valid Auth/JWT session?}
    SESSION -- no --> SIGNIN[Sign in / register]
    SIGNIN --> SESSION
    SESSION -- yes --> PROFILE[Load authenticated profile]
    PROFILE --> ROLE{Exact role}
    ROLE -- USER --> PATIENT[Android patient shell<br/>owned pets and bookings]
    ROLE -- VETERINARY_BUSINESS --> TENANT{Active tenant?}
    TENANT -- no --> STATUS[Business application/status]
    TENANT -- yes --> VET[Veterinary shell<br/>tenant operations and consultations]
    ROLE -- SUPERADMIN --> ADMIN[Admin shell<br/>approval, status, audit]
    ROLE -- unknown --> SAFE[Safe error / sign out]
```

Routing selects a UI shell only. Each destination still follows **UI ->
ViewModel -> Use Case -> Repository -> Supabase API/Edge Function -> Auth/JWT
-> PostgreSQL/RLS -> authorized response**.
