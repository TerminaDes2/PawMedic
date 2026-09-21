# System context

```mermaid
flowchart LR
    U[USER<br/>pet owner] --> A[Android app]
    V[VETERINARY_BUSINESS<br/>tenant operator] --> C[Veterinary desktop]
    S[SUPERADMIN<br/>platform operator] --> D[Admin desktop]
    A --> B[Supabase client boundary]
    C --> B
    D --> B
    B --> AUTH[Supabase Auth<br/>JWT]
    B --> EF[Authenticated<br/>Edge Functions]
    B --> DB[(PostgreSQL<br/>RLS)]
    EF --> DB
    DB --> R[Authorized response]
    R --> B
```

The three roles use separate app shells, but all data access carries the same
Auth/JWT identity and is filtered by PostgreSQL/RLS. The service-role key is
never present in a client.
