# Cronograma de features

> Baseline estimado creado porque no habia cronograma original. Dates are
> placeholders until product and clinical stakeholders approve scope.

```mermaid
gantt
    title PawMedic baseline delivery
    dateFormat  YYYY-MM-DD
    axisFormat  %d %b
    section Foundation
    KMP scaffold and CI       :done, foundation, 2026-09-21, 7d
    Auth/session vertical     :active, auth, after foundation, 7d
    Profile and roles         :profile, after auth, 5d
    section Domain
    Pet records               :pet, after profile, 10d
    Clinical consultation     :consultation, after pet, 15d
    section Hardening
    RLS review and audit      :rls, after consultation, 7d
    Pilot readiness           :pilot, after rls, 7d
```

## Milestones

| Milestone | Exit criteria | Dependency |
| --- | --- | --- |
| Foundation | Modules build and tests run | JDK/Android SDK |
| Identity | Sign in, sign out, session restore, role loading | Supabase project |
| Records | Pet CRUD and owner authorization | Identity |
| Clinical | Consultation create/read with audit trail | Records, clinical review |
| Pilot | Observability, backup, threat model, support runbook | All above |

This is not a commitment to dates. Update this document after validating the
missing product source documents.
