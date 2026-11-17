package com.pawsmedic.desktop.admin.translation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

@Serializable
private data class TranslationRequest(val text: String, val source: String, val target: String)

@Serializable
private data class TranslationResponse(val translation: String)

@Serializable
private data class BatchTranslationRequest(val texts: List<String>, val source: String, val target: String)

@Serializable
private data class BatchTranslationResponse(val translations: List<String>)

fun bundledTranslationDirectory(): File? {
    val location = File(object {}.javaClass.protectionDomain.codeSource.location.toURI())
    return if (location.isFile && location.parentFile.name == "lib") {
        File(location.parentFile.parentFile, "argos")
    } else null
}

suspend fun translateLocally(text: String): String {
    require(text.isNotBlank() && text.length <= 4000) { "El texto debe tener entre 1 y 4000 caracteres." }
    val response = runTranslationWorker(Json.encodeToString(TranslationRequest(text, "es", "en")))
    return Json.decodeFromString<TranslationResponse>(response).translation.also {
        check(it.isNotBlank()) { "El traductor no devolvió texto." }
    }
}

suspend fun translateBatchLocally(texts: List<String>): List<String> {
    require(texts.size in 1..50 && texts.all { it.isNotBlank() && it.length <= 4000 }) {
        "El lote debe contener de 1 a 50 textos de hasta 4000 caracteres."
    }
    val response = runTranslationWorker(Json.encodeToString(BatchTranslationRequest(texts, "es", "en")))
    return Json.decodeFromString<BatchTranslationResponse>(response).translations.also {
        check(it.size == texts.size && it.all(String::isNotBlank)) { "La respuesta del traductor está incompleta." }
    }
}

private suspend fun runTranslationWorker(request: String): String = withContext(Dispatchers.IO) {
    val bundle = bundledTranslationDirectory()
    val packages = System.getProperty("pawmedic.argos.packages")?.let(::File)
        ?: bundle?.resolve("models")
        ?: throw IllegalStateException("El modelo de traducción todavía no está instalado con la aplicación.")
    val executable = System.getProperty("pawmedic.argos.executable")?.let(::File)
        ?: if (System.getProperty("pawmedic.argos.python") == null) bundle?.resolve("worker")?.resolve("pawmedic-argos-worker.exe") else null
    val python = System.getProperty("pawmedic.argos.python")?.let(::File)
    val engine = executable ?: python
        ?: throw IllegalStateException("El traductor local todavía no está instalado con la aplicación.")
    check(engine.isFile && packages.isDirectory) { "No se encontró el motor o el modelo del traductor local." }
    val command = if (executable != null) {
        listOf(executable.absolutePath)
    } else {
        val script = object {}.javaClass.getResource("/translation/argos_worker.py")?.readText(Charsets.UTF_8)
            ?: error("No se encontró el módulo de traducción.")
        val encodedScript = Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_8))
        listOf(engine.absolutePath, "-I", "-c", "exec(__import__('base64').b64decode('$encodedScript'))")
    }
    val process = ProcessBuilder(command)
        .redirectError(ProcessBuilder.Redirect.DISCARD)
        .apply { environment()["ARGOS_PACKAGES_DIR"] = packages.absolutePath }
        .start()
    val maxOutput = 2_000_000
    val output = CompletableFuture.supplyAsync { process.inputStream.use { it.readNBytes(maxOutput + 1) } }
    val input = CompletableFuture.runAsync {
        process.outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.write(request)
        }
    }
    try {
        val start = System.nanoTime()
        while (!process.waitFor(250, TimeUnit.MILLISECONDS)) {
            coroutineContext.ensureActive()
            if (input.isCompletedExceptionally) input.join()
            check(!output.isDone || output.join().size <= maxOutput) { "La respuesta del traductor excede el límite." }
            check(System.nanoTime() - start < TimeUnit.MINUTES.toNanos(3)) { "El traductor tardó demasiado." }
        }
        check(process.exitValue() == 0) { "No se pudo ejecutar el traductor local. Revisa la instalación del motor y el modelo." }
        input.get(5, TimeUnit.SECONDS)
        val bytes = output.get(5, TimeUnit.SECONDS)
        check(bytes.size <= maxOutput) { "La respuesta del traductor excede el límite." }
        bytes.toString(Charsets.UTF_8)
    } finally {
        if (process.isAlive) process.destroyForcibly()
    }
}
