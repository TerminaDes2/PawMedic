package com.pawsmedic.desktop.veterinary

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

private const val SUPABASE_URL = "https://fmcsdyupvmdjvlbzjgml.supabase.co"
private const val SUPABASE_ANON_KEY = "sb_publishable_rO8I1yDID5nKSASNgaa8sA_kZHqICuG"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
}
