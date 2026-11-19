drop policy if exists product_images_tenant_insert on storage.objects;
create policy product_images_tenant_insert on storage.objects for insert to authenticated
with check (
    bucket_id = 'product-images'
    and exists (
        select 1 from public.tenants t
        where t.id::text = (storage.foldername(storage.objects.name))[1]
          and public.is_tenant_member(t.id)
    )
);
