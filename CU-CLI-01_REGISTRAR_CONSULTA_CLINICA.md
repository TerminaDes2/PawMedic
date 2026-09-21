# CU-CLI-01: Registrar consulta clinica

> This use-case source document was absent at bootstrap. The following is a
> reviewable contract, not a claim about approved clinical requirements.

## Actors

Primary: authenticated veterinarian. Supporting: pet owner (read-only after
publication), Supabase Edge Function, Postgres RLS.

## Preconditions

1. The veterinarian has an active session.
2. The veterinary operator has the `VETERINARY_BUSINESS` role and tenant
   membership.
3. The pet exists and is visible to that clinic.

## Main flow

1. The veterinarian selects a pet and starts a consultation.
2. The app captures reason, observations, diagnosis, plan, and optional weight.
3. The app validates required text and displays a review step.
4. The client sends a command to `create-consultation`.
5. The function verifies the JWT, role, clinic membership, and pet access.
6. The function inserts the consultation and an audit event in one transaction.
7. The app shows the saved consultation as `PUBLISHED` and refreshes the pet
   timeline.

## Alternate and error flows

* Missing reason or observations: keep the form open and show field errors.
* Expired session: stop submission and route to sign in.
* Insufficient role/access: return a generic forbidden response; do not expose
  whether a private pet exists.
* Network retry: use an idempotency key and never duplicate a consultation.
* Backend failure: retain a local draft without pretending it was published.

## Data contract (draft)

```json
{
  "pet_id": "uuid",
  "reason": "string",
  "observations": "string",
  "diagnosis": "string",
  "plan": "string",
  "weight_grams": 4200,
  "occurred_at": "2026-09-21T16:00:00Z",
  "idempotency_key": "uuid"
}
```

## Acceptance criteria

* A valid veterinarian can create exactly one consultation per idempotency key.
* Owners can read published consultations for their own pets only.
* A non-veterinarian cannot create a consultation, even if the UI is bypassed.
* The audit record contains actor, action, entity, and timestamp.
* Tests cover validation, authorization, retry, and failure states.

Clinical, legal, retention, and consent policies must be approved before this
use case is promoted beyond a prototype.
