package com.pawsmedic.desktop.veterinary

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import com.pawsmedic.desktop.admin.readClientConfiguration

private val supabaseUrl = readClientConfiguration("SUPABASE_URL")
private val supabaseAnonKey = readClientConfiguration("SUPABASE_ANON_KEY")

val supabase = createSupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = supabaseAnonKey
) {
    install(Auth)
    install(Postgrest)
}
