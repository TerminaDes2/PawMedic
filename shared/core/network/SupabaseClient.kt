package com.pawsmedic.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth // Importación recomendada en v2.x / v3.x
import io.github.jan.supabase.postgrest.Postgrest

fun provideSupabaseClient(url: String, anonKey: String) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey
) {
    install(Auth)
    install(Postgrest)
}