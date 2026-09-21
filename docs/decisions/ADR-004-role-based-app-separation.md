# ADR-004: Role-based application separation

* Status: Accepted
* Date: 2026-09-21

## Context

Pet owners, veterinary operators, and platform administrators have different
tasks, risk profiles, and navigation. A single broad shell would encourage
accidental cross-role routes and expose administrative concepts to patients.

## Decision

Keep one Android patient app for `USER`, one veterinary desktop app for
`VETERINARY_BUSINESS`, and one admin desktop app for `SUPERADMIN`. Restore the
Auth session, load the profile role, and route to the matching shell. Deep
links and token refreshes repeat the same decision. Route guards are
presentation only; repositories still send the JWT and PostgreSQL/RLS makes
the authorization decision.

## Consequences

Each app has a focused information architecture and can release independently
within the monorepo. Shared features must expose role-neutral domain
contracts, and role-specific operations need explicit backend policies and
Edge Function checks. A suspended or pending tenant gets a status route
rather than tenant navigation.
