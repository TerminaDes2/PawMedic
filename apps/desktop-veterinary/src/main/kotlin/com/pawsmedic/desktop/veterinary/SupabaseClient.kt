package com.pawsmedic.desktop.veterinary

import io.github.jan_tennert.supabase.createSupabaseClient
import io.github.jan_tennert.supabase.gotrue.Auth
import io.github.jan_tennert.supabase.postgrest.Postgrest

// Reemplaza los textos entre comillas con las claves reales de tu panel de Supabase
private const val SUPABASE_URL = "https://fmcsdyupvmdjvlbzjgml.supabase.co"
private const val SUPABASE_ANON_KEY = "sb_publishable_rO8I1yDID5nKSASNgaa8sA_kZHqICuG"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
}