package com.pawsmedic.desktop.veterinary.ui.auth

import java.text.Normalizer

private val rfcRegex = Regex("^[A-Z0-9]{12,13}$")
private val licenseRegex = Regex("^[0-9]{7,8}$")
private val addressRegex = Regex("^[\\p{L}0-9, #]+$")

private val prohibitedWords = setOf(
    "puta", "puto", "putas", "putos", "putita", "pendejo", "pendeja", "pendejos", "pendejas",
    "cabron", "cabrona", "chingar", "chingada", "chingado", "chingadera", "chinga", "verga", "vergas",
    "mierda", "culero", "culera", "culo", "joder", "jodido", "marica", "maricon", "mamada", "mamadas",
    "zorra", "cojones", "pinche", "ojete", "idiota", "estupido", "estupida", "imbecil",
    "pene", "vagina", "vulva", "sexo", "sexual", "porno", "pornografia", "orgasmo", "tetas", "nalgas",
    "semen", "coito", "masturbacion", "erotico", "erotica", "desnudo", "desnuda", "follar",
    "fuck", "fucking", "shit", "bitch", "asshole", "dick", "pussy", "cunt", "porn", "slut", "whore", "nude"
)

private fun normalizeWord(text: String): String =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .map {
            when (it) {
                '0' -> 'o'
                '1' -> 'i'
                '3' -> 'e'
                '4' -> 'a'
                '5' -> 's'
                '7' -> 't'
                '@' -> 'a'
                '$' -> 's'
                else -> it
            }
        }.joinToString("")

internal fun containsProhibitedWord(text: String): Boolean =
    normalizeWord(text).split(Regex("[^a-zñ]+")).any { it in prohibitedWords }

internal fun validateBusinessOrPersonName(value: String): AuthMessage? {
    val text = value.trim()
    return when {
        text.length < 5 -> AuthMessage("Debe tener al menos 5 caracteres.", "It must have at least 5 characters.")
        text.none { it.isUpperCase() } -> AuthMessage("Debe incluir al menos una letra mayúscula.", "It must include at least one uppercase letter.")
        containsProhibitedWord(text) -> AuthMessage("El texto contiene una palabra no permitida. Elimínala para continuar.", "The text contains a word that is not allowed. Remove it to continue.")
        else -> null
    }
}

internal fun validateRfc(value: String): AuthMessage? =
    if (rfcRegex.matches(value)) null
    else AuthMessage("El RFC debe tener 12 o 13 caracteres: solo letras mayúsculas y números, sin espacios ni símbolos.", "The RFC must have 12 or 13 characters: uppercase letters and digits only, no spaces or symbols.")

internal fun validateClinicAddress(value: String): AuthMessage? {
    val text = value.trim()
    return when {
        text.count { !it.isWhitespace() } < 4 -> AuthMessage("La dirección debe tener al menos 4 caracteres.", "The address must have at least 4 characters.")
        !addressRegex.matches(text) -> AuthMessage("Solo se permiten letras, números, comas, espacios y #.", "Only letters, digits, commas, spaces and # are allowed.")
        else -> null
    }
}

internal fun validateProfessionalLicense(value: String): AuthMessage? =
    if (licenseRegex.matches(value)) null
    else AuthMessage("La cédula profesional debe tener de 7 a 8 dígitos, sin espacios ni letras.", "The professional license must have 7 to 8 digits, with no spaces or letters.")
