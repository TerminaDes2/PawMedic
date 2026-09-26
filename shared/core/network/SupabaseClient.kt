package com.pawsmedic.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://fmcsdyupvmdjvlbzjgml.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZtY3NkeXVwdm1kanZsYnpqZ21sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAyOTE0MDMsImV4cCI6MjEwNTg2NzQwM30.y4dIbAKuGdFujQNos3HXGH-TYbtCMMEFw083SbGwkVc"
) {
    install(Auth)
    install(Postgrest)
}