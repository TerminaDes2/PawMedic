create or replace view public.business_directory
with (security_barrier = true)
as
select
    b.tenant_id as id,
    b.public_name as name,
    b.address,
    b.phone,
    b.photo_url
from public.businesses b
join public.tenants t on t.id = b.tenant_id
where b.status = 'APPROVED'
  and t.status = 'ACTIVE';

revoke all on public.business_directory from public, anon;
grant select on public.business_directory to authenticated;
