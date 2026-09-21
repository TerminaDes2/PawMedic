# ADR-001: Kotlin Multiplatform monorepo

* Status: Accepted
* Date: 2026-09-21

## Context

PawMedic needs an Android experience for `USER`, desktop workflows for
`VETERINARY_BUSINESS`, and a separate desktop surface for `SUPERADMIN`.
Duplicating domain rules across repositories would make pet ownership,
tenant operations, and authorization state drift.

## Decision

Keep applications, shared core modules, feature vertical slices, and the
Supabase project in one Kotlin Multiplatform monorepo. Use Android and JVM
desktop targets with common domain and presentation contracts. The canonical
Gradle modules are the ones listed in `settings.gradle.kts`.

## Consequences

Shared models, use cases, repositories, and test fixtures can be reviewed
together, while platform UI remains isolated in source sets. Gradle builds are
larger and module boundaries must be enforced deliberately. Historical
scaffold directories remain on disk but are not part of the canonical build.
