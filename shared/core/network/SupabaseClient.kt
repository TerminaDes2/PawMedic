package com.pawsmedic.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.time.Duration.Companion.seconds

fun provideSupabaseClient(url: String, anonKey: String) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey
) {
    requestTimeout = 30.seconds
    install(Auth)
    install(Postgrest)
}
