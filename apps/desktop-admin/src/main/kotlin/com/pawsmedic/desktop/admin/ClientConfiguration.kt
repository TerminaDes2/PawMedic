package com.pawsmedic.desktop.admin

import java.io.File
import java.util.Properties

fun readClientConfiguration(name: String): String {
    val envFile = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
        .map { File(it, ".env") }
        .firstOrNull(File::isFile)
    val localValue = envFile?.let { file ->
        Properties().also { file.inputStream().use(it::load) }.getProperty(name)
    }
    val environmentValue = System.getenv(name)
    val sharedValue = object {}.javaClass.getResourceAsStream("/client.properties")?.use { stream ->
        Properties().also { it.load(stream) }.getProperty(name)
    }
    return resolveClientConfiguration(localValue, environmentValue, sharedValue)
        ?: error("Configura $name en config/client.properties, .env o como variable de entorno.")
}

internal fun resolveClientConfiguration(local: String?, environment: String?, shared: String?): String? =
    sequenceOf(local, environment, shared)
        .mapNotNull { it?.trim()?.trim('"')?.takeIf(String::isNotBlank) }
        .firstOrNull()
