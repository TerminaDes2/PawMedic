insert into storage.buckets (id, name, public)
values ('pets', 'pets', true)
on conflict (id) do update set public = true;

drop policy if exists "Authenticated users can upload pet photos" on storage.objects;
create policy "Authenticated users can upload pet photos"
    on storage.objects
    for insert
    to authenticated
    with check (bucket_id = 'pets');

drop policy if exists "Anyone can view pet photos" on storage.objects;
create policy "Anyone can view pet photos"
    on storage.objects
    for select
    to public
    using (bucket_id = 'pets');
