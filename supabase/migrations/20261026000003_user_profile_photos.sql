alter table public.profiles
    add column if not exists foto_url text;

insert into storage.buckets (id, name, public)
values ('profiles', 'profiles', true)
on conflict (id) do update set public = true;

drop policy if exists "Authenticated users can upload profile photos" on storage.objects;
create policy "Authenticated users can upload profile photos"
    on storage.objects
    for insert
    to authenticated
    with check (bucket_id = 'profiles');

drop policy if exists "Anyone can view profile photos" on storage.objects;
create policy "Anyone can view profile photos"
    on storage.objects
    for select
    to public
    using (bucket_id = 'profiles');
