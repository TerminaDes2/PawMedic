package com.pawsmedic.desktop.veterinary

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties

private fun readSupabaseConfig(name: String): String {
    val envValue = System.getenv(name)?.takeIf(String::isNotBlank)
    val envFile = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
        .map { File(it, ".env") }
        .firstOrNull(File::isFile)
    val fileValue = envFile?.let { file ->
        Properties().also { properties ->
            file.inputStream().use(properties::load)
        }.getProperty(name)
    }?.trim()?.trim('"')?.takeIf(String::isNotBlank)

    return fileValue ?: envValue
        ?: error("Configura $name en el archivo .env de la raíz o como variable de entorno.")
}

private val supabaseUrl = readSupabaseConfig("SUPABASE_URL")
private val supabaseAnonKey = readSupabaseConfig("SUPABASE_ANON_KEY")

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
