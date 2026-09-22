# Multi-tenant data flow

```mermaid
flowchart LR
    JWT[Auth/JWT<br/>auth.uid()] --> P[profiles<br/>role + tenant_id]
    P --> CHECK{RLS helper checks}
    CHECK --> OWN[Pet ownership<br/>owner_id = auth.uid()]
    CHECK --> MEMBER[Tenant membership<br/>tenant_id match]
    CHECK --> ADMIN[SUPERADMIN<br/>audited platform access]
    OWN --> PET[(pets)]
    MEMBER --> TENANT[(tenant catalog,<br/>schedules, appointments)]
    MEMBER --> CLINICAL[(medical records<br/>and consultations)]
    ADMIN --> OPS[(business approval,<br/>tenant status, audit logs)]
    PET --> RESPONSE[Authorized response]
    TENANT --> RESPONSE
    CLINICAL --> RESPONSE
    OPS --> RESPONSE
```

An approved business creates a tenant and links its veterinary profile in one
transaction. A `USER` reaches clinical data through the owned pet; a
`VETERINARY_BUSINESS` reaches it through authorized tenant membership; and a
`SUPERADMIN` reaches administrative data through audited operations. A
different user or tenant receives no rows even if it can guess an ID.
