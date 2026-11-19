-- Products use integer IDs in the deployed schema; return text so the mobile
-- client can decode IDs consistently, including on installations using UUIDs.
begin;

drop function if exists public.get_public_products(uuid);
create function public.get_public_products(p_tenant_id uuid)
returns table(id text, name text, description text, price_cents integer, image_url text)
language sql
stable
security definer
set search_path = public
as $$
    select p.id::text, p.name, p.description, p.price_cents, p.image_url
    from public.products p
    join public.tenants t on t.id = p.tenant_id
    where auth.uid() is not null
      and p.tenant_id = p_tenant_id
      and t.status = 'ACTIVE'
      and p.active and p.published
    order by p.name, p.id;
$$;

revoke all on function public.get_public_products(uuid) from public, anon;
grant execute on function public.get_public_products(uuid) to authenticated;

commit;
