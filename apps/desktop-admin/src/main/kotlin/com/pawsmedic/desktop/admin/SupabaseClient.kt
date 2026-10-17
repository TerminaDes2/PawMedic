package com.pawsmedic.desktop.admin

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
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
    install(Auth)
    install(Postgrest)
}
