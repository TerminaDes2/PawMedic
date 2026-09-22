# ADR-003: Multi-tenancy and RLS

* Status: Accepted
* Date: 2026-09-21

## Context

Veterinary businesses share a platform but must not see each other's
operations or clinical records. Pet owners need access to their own pets and
authorized care history. UI route guards cannot protect a modified client or
an accidentally broad query.

## Decision

Use `tenants` as the veterinary isolation boundary and store the exact role
(`USER`, `VETERINARY_BUSINESS`, `SUPERADMIN`) and optional tenant ID in
`profiles`. Enable PostgreSQL RLS on all public domain tables. Policies use
`auth.uid()`, tenant membership, pet ownership, and explicit superadmin
checks. Authenticated Edge Functions call narrow, audited, transactional
RPCs for business approval, critical bookings, and other privileged
commands. Service-role credentials never leave the backend.

## Consequences

Authorization is enforced consistently for table APIs, Edge Functions, and
manual requests. Policy tests, indexes, and migration reviews are mandatory.
Some commands require an Edge Function rather than a direct insert, and local
setup must run Supabase to exercise the full security boundary.
