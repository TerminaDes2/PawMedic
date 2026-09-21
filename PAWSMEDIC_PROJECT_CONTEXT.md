# PawMedic project context

> Bootstrap context. The original project-context source document was absent
> when this repository was created. See ADR-0001 for assumptions.

## Purpose

PawMedic connects pet owners and veterinary clinics around a shared record of
pets and consultations. The scaffold prioritizes a secure session/profile
slice that can support later clinical features.

## Personas

* Pet owner: maintains pet profiles and reads consultation history.
* Veterinarian: works from the clinic client and records consultations.
* Admin: manages clinic access and operational configuration.

## Technical direction

* Kotlin Multiplatform shared domain and feature state.
* Compose UI for Android and desktop.
* Koin for dependency injection.
* Kotlin serialization for wire and persistence DTOs.
* Coroutines and `StateFlow` for asynchronous state.
* Supabase Postgres, RLS, and Edge Functions as the target backend.

## Non-goals for this scaffold

Billing, prescriptions, diagnostics, messaging, push notifications, and
multi-clinic tenancy are intentionally not implemented.

## Quality and security

Client applications never receive the Supabase service-role key. Every
backend mutation must be validated by authenticated RLS or an Edge Function.
Tests cover deterministic domain and state behavior without a network.
