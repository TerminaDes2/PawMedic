package com.pawsmedic.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

fun provideSupabaseClient(
    url: String,
    anonKey: String,
    enableAuthLifecycleCallbacks: Boolean = true,
) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey
) {
    install(Auth) {
        enableLifecycleCallbacks = enableAuthLifecycleCallbacks
    }
    install(Postgrest)
    install(Storage)
}
