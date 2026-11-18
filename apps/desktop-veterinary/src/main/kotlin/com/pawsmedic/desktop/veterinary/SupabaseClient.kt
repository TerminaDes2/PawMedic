package com.pawsmedic.desktop.veterinary

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import com.pawsmedic.desktop.admin.readClientConfiguration
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

private val supabaseUrl = readClientConfiguration("SUPABASE_URL")
private val supabaseAnonKey = readClientConfiguration("SUPABASE_ANON_KEY")

val supabase = createSupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = supabaseAnonKey
) {
    install(Auth) {
        autoLoadFromStorage = false
        sessionManager = SecureSessionStorage
    }
    install(Postgrest)
    install(Storage)
}

internal suspend fun verifyAccountPassword(email: String, password: String, userId: String): Boolean {
    val temporaryClient = createSupabaseClient(supabaseUrl, supabaseAnonKey) {
        install(Auth) { minimalSettings() }
    }
    try {
        temporaryClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return temporaryClient.auth.currentUserOrNull()?.id == userId
    } finally {
        withContext(NonCancellable) { temporaryClient.close() }
    }
}
