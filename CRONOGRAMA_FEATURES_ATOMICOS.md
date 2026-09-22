# Cronograma de features atomicos

Each atomic item should be independently demonstrable and testable.

| ID | Atomic slice | Acceptance evidence | Status |
| --- | --- | --- | --- |
| A-001 | Create Gradle module graph | `checkScaffold` and dependency graph | Done |
| A-002 | Serialize account models | JSON round-trip unit test | Done |
| A-003 | Define auth repository port | In-memory and Supabase-ready adapters | Done |
| A-004 | Publish auth `StateFlow` | loading/success/error tests | Done |
| A-005 | Publish profile role | profile state test and enum | Done |
| A-006 | Render Android shell | debug APK starts at launcher | Done |
| A-007 | Render patient desktop shell | `:apps:patientApp:run` | Done |
| A-008 | Render clinic desktop shell | `:apps:clinicApp:run` | Done |
| A-009 | Create schema and RLS template | local Supabase migration review | Done |
| A-010 | Implement pet registration | RLS and repository tests | Planned |
| A-011 | Implement consultation command | Edge Function contract tests | Planned |
| A-012 | Add audit and observability | event design and dashboard | Planned |

The original atomic schedule was unavailable; owners and dates are intentionally
not invented.
