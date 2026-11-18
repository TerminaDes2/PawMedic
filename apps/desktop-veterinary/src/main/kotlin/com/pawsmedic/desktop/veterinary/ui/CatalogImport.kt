package com.pawsmedic.desktop.veterinary.ui

import java.math.RoundingMode
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal enum class ImportKind { PRODUCT, SERVICE }

internal data class ImportItem(
    val name: String,
    val description: String,
    val priceCents: Int,
    val sku: String = "",
    val stock: Int = 0,
    val duration: Int = 0
)

internal fun chooseCatalogFile(): File? {
    val dialog = FileDialog(null as Frame?, "Seleccionar archivo TXT", FileDialog.LOAD)
    dialog.file = "*.txt"
    return try {
        dialog.isVisible = true
        val name = dialog.file
        val directory = dialog.directory
        if (name == null || directory == null) null else File(directory, name)
    } finally {
        dialog.dispose()
    }
}

internal fun readCatalogFile(file: File): String {
    require(file.extension.equals("txt", ignoreCase = true)) { "Selecciona un archivo .txt." }
    require(file.isFile && file.length() in 1..1_000_000) { "El archivo debe tener entre 1 byte y 1 MB." }
    val bytes = file.readBytes()
    require(bytes.size in 1..1_000_000) { "El archivo debe tener entre 1 byte y 1 MB." }
    return try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
    } catch (e: java.nio.charset.CharacterCodingException) {
        throw IllegalArgumentException("Guarda el archivo con codificación UTF-8.")
    }
}

internal fun parseCatalogImport(input: String, kind: ImportKind): List<ImportItem> {
    val records = mutableListOf<Map<String, String>>()
    var fields = linkedMapOf<String, String>()
    input.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, line ->
        val trimmed = line.trim()
        if (trimmed == "---") {
            require(fields.isNotEmpty()) { "Línea ${index + 1}: no hay datos antes del separador." }
            records.add(fields)
            fields = linkedMapOf()
        } else if (trimmed.isNotEmpty()) {
            val separator = trimmed.indexOf(':')
            require(separator > 0) { "Línea ${index + 1}: usa campo: valor." }
            val key = trimmed.substring(0, separator).trim().lowercase()
            val value = trimmed.substring(separator + 1).trim()
            val allowed = if (kind == ImportKind.PRODUCT) {
                setOf("nombre", "descripcion", "precio", "sku", "existencias")
            } else {
                setOf("nombre", "descripcion", "precio", "duracion")
            }
            require(key in allowed) { "Línea ${index + 1}: campo desconocido '$key'." }
            require(fields.put(key, value) == null) { "Línea ${index + 1}: campo '$key' duplicado." }
        }
    }
    if (fields.isNotEmpty()) records.add(fields)
    require(records.isNotEmpty()) { "El archivo no contiene elementos." }
    require(records.size <= 500) { "El archivo no puede contener más de 500 elementos." }
    val result = records.mapIndexed { index, record ->
        val number = index + 1
        fun required(key: String) = record[key]?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Elemento $number: falta $key.")
        val amount = required("precio").replace(",", ".").toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Elemento $number: precio inválido.")
        require(amount.signum() >= 0) { "Elemento $number: el precio no puede ser negativo." }
        val cents = try {
            amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact()
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException("Elemento $number: el precio debe tener hasta dos decimales y estar dentro del límite.")
        }
        val name = required("nombre")
        val description = record["descripcion"].orEmpty()
        if (kind == ImportKind.PRODUCT) {
            val sku = required("sku")
            val stock = required("existencias").toIntOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Elemento $number: existencias debe ser un entero no negativo.")
            ImportItem(name, description, cents, sku = sku, stock = stock)
        } else {
            val duration = required("duracion").toIntOrNull()?.takeIf { it > 0 }
                ?: throw IllegalArgumentException("Elemento $number: duración debe ser un entero positivo.")
            ImportItem(name, description, cents, duration = duration)
        }
    }
    if (kind == ImportKind.PRODUCT) {
        require(result.map { it.sku }.distinct().size == result.size) { "El archivo contiene SKU duplicados." }
    }
    return result
}
