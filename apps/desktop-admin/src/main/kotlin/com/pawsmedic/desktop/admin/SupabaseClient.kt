package com.pawsmedic.desktop.admin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import java.io.File
import java.io.FileInputStream
import java.util.Properties

private fun loadLocalProperties(): Properties {
    val properties = Properties()
    var directory: File? = File(System.getProperty("user.dir")).absoluteFile

    while (directory != null) {
        val envFile = File(directory, ".env")
        if (envFile.isFile) {
            FileInputStream(envFile).use(properties::load)
            return properties
        }
        directory = directory.parentFile
    }

    return properties
}

private fun setting(name: String, properties: Properties): String? {
    val value = System.getenv(name)
        ?.takeIf { it.isNotBlank() }
        ?: properties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: return null

    return value.trim().removeSurrounding("\"").removeSurrounding("'")
}

fun createDesktopSupabaseClient(): SupabaseClient {
    val properties = loadLocalProperties()
    val url = setting("SUPABASE_URL", properties)
        ?: error("Falta SUPABASE_URL. Configúrala en el archivo .env de la raíz.")
    val anonKey = setting("SUPABASE_ANON_KEY", properties)
        ?: error("Falta SUPABASE_ANON_KEY. Configúrala en el archivo .env de la raíz.")

    return createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = anonKey
    ) {
        install(Auth)
        install(Postgrest)
    }
}
