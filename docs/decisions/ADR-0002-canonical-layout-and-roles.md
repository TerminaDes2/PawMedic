# ADR-0002: Canonical layout, roles, and missing source documents

- Status: accepted
- Date: 2026-09-21

The five requested source documents were absent in the initial repository.
ADR-0001 preserves that fact and the assumptions made at bootstrap. This ADR
does not rewrite that historical record; it establishes the corrected layout
and contract for implementation.

The canonical Gradle modules live under `shared/` and `apps/`. The only
application roles are `USER`, `VETERINARY_BUSINESS`, and `SUPERADMIN`.
`tenant_id` identifies the business tenant for veterinary users and tenant
owned records. RLS, not UI visibility, enforces tenant isolation and
superadmin access.
