-- Migration: Add logo_url to tenants table and tenant update RLS policy for veterinary logo management
-- Instruction to apply: Run this SQL script in your Supabase project's SQL Editor.

alter table public.tenants
    add column if not exists logo_url text;

-- RLS Policy for updating tenants (so tenant members can update their tenant logo/info)
drop policy if exists tenants_member_update on public.tenants;
create policy tenants_member_update on public.tenants for update to authenticated
using (
    public.is_superadmin()
    or public.is_tenant_member(id)
)
with check (
    public.is_superadmin()
    or public.is_tenant_member(id)
);

-- Update business_directory view to include logo_url so Mobile can fetch it
create or replace view public.business_directory
with (security_barrier = true)
as
select
    b.tenant_id as id,
    b.public_name as name,
    b.address,
    b.phone,
    t.logo_url
from public.businesses b
join public.tenants t on t.id = b.tenant_id
where b.status = 'APPROVED'
  and t.status = 'ACTIVE';

revoke all on public.business_directory from public, anon;
grant select on public.business_directory to authenticated;
