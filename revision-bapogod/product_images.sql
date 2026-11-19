insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('product-images', 'product-images', true, 5000000, array['image/jpeg', 'image/png'])
on conflict (id) do update set public = true, file_size_limit = 5000000,
    allowed_mime_types = array['image/jpeg', 'image/png'];

drop policy if exists product_images_tenant_insert on storage.objects;
create policy product_images_tenant_insert on storage.objects for insert to authenticated
with check (
    bucket_id = 'product-images'
    and exists (
        select 1 from public.tenants t
        where t.id::text = (storage.foldername(name))[1]
          and t.status = 'ACTIVE'
          and public.is_tenant_member(t.id)
    )
);

drop policy if exists product_images_tenant_delete on storage.objects;
create policy product_images_tenant_delete on storage.objects for delete to authenticated
using (
    bucket_id = 'product-images'
    and exists (
        select 1 from public.tenants t
        where t.id::text = (storage.foldername(name))[1]
          and public.is_tenant_member(t.id)
    )
);
