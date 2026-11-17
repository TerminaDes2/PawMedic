# Supabase backend templates

These files are deployment templates only. Create a Supabase project and set
secrets using `supabase secrets set`; never put a service-role key in this
repository or in a client app.

## Local workflow

```text
supabase start
supabase db reset
supabase functions serve
```

The ordered migrations create profiles, tenants, businesses, pets, scheduling,
appointments, clinical records, and audit logs with RLS enabled. Review
policies with a security owner before production.

For the veterinary desktop registration flow, apply
`fix_submit_business_application_complete.sql` after the English-column
schema migration. It requires the `profiles`, `businesses`, `tenants`, and `audit_logs`
tables and creates `business_applications` if absent. Approval synchronizes
both application tables, the active tenant, the owner profile, and the
directory view in one transaction. RPC execution is granted to the API roles,
but submission requires a verified session and administrative operations
require a superadministrator or service role. Existing unrelated RLS policies
must also be reviewed because PostgreSQL combines permissive policies with OR.

If the deployed database has the optional `services.category` column and the
`trg_create_default_services` tenant trigger, apply
`fix_default_service_categories.sql` in the SQL editor before approving new
clinics. It updates that trigger's function to supply the deployed category
values and creates new services deselected. The script also deselects existing
services for every clinic **once**; existing appointments are not cancelled.
Clinic staff can select services individually or all at once in the desktop
catalog. Reapplying the script does not reset their selections. It stops
without changes if the column or trigger is absent.

To configure date-specific booking availability, apply
`date_specific_availability.sql` after the booking migrations. A date override
replaces weekly shifts for that date; an empty selection marks the day closed.
Consecutive hourly selections form a continuous interval. Existing appointments
are not cancelled by availability changes.
The desktop editor now uses weekly schedules instead. Apply
`use_weekly_availability.sql` to stop applying date overrides without deleting
their stored rows or existing appointments. It also limits appointment start
times to the hour while retaining each service's configured duration.

For consultation start and clinical records, run `enable_consultation_status.sql`
first and, in a separate SQL Editor execution, `consultation_workflow.sql`.
The second script requires the deployed `medical_records` fields shown by
`diagnose_consultation_workflow.sql`. Completed appointments from before this
change can be given a record manually; their clinical details cannot be inferred.

## Edge Functions

* `create-consultation`: validates a veterinary tenant command and inserts a
  consultation with an idempotency key.
* `profile-role`: returns the caller's profile role without exposing private
  account data.

The TypeScript files are intentionally dependency-light skeletons. Add the
project's generated Supabase types and tests before deployment.
