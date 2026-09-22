# ADR-002: Feature-first clean architecture

* Status: Accepted
* Date: 2026-09-21

## Context

Veterinary workflows cross pets, appointments, clinical records, and tenant
catalogs. A layer-first project would scatter one workflow across unrelated
packages and make ownership and RLS behavior hard to test.

## Decision

Organize shared code by feature. Each feature owns `domain` models, repository
ports, and use cases; `data` implements those ports; and presentation
ViewModels consume use cases. Core modules provide stable cross-feature ports
for Auth/JWT, networking, storage, models, and design primitives.

The production request path is **UI -> ViewModel -> Use Case -> Repository ->
Supabase API/Edge Function -> Auth/JWT -> PostgreSQL/RLS -> authorized
response**. Domain code never imports Compose or a Supabase SDK, and UI never
calls a table directly.

## Consequences

Use cases and ViewModels are testable without a network and repository
implementations can change without rewriting screens. Cross-feature contracts
need careful design; direct imports of another feature's data layer are
prohibited.
