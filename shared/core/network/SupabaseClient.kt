package com.pawsmedic.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

// Se crea una función factory en lugar de una variable global
fun provideSupabaseClient(url: String, anonKey: String) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey
) {
    install(Auth)
    install(Postgrest)
}