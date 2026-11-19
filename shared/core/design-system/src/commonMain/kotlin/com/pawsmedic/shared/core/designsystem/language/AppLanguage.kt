package com.pawsmedic.shared.core.designsystem.language

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage {
    SPANISH, ENGLISH;

    fun text(spanish: String, english: String): String =
        if (this == ENGLISH) english else spanish
}

class LanguagePreferenceControl(
    val selected: AppLanguage,
    val onChange: (AppLanguage) -> Unit
)

val LocalLanguagePreference = staticCompositionLocalOf<LanguagePreferenceControl?> { null }

fun languageFromPreference(value: String?): AppLanguage =
    AppLanguage.entries.firstOrNull { it.name == value } ?: AppLanguage.SPANISH

fun appointmentStatusLabel(status: String, language: AppLanguage): String = language.text(status, when (status) {
    "Pendiente de confirmación" -> "Pending confirmation"
    "Confirmada" -> "Confirmed"
    "Cancelada" -> "Cancelled"
    "Completada" -> "Completed"
    "No asistió" -> "No-show"
    "Rechazada" -> "Rejected"
    else -> status
})

fun petCatalogLabel(value: String, language: AppLanguage): String = language.text(value, when (value) {
    "Canino", "Perro" -> "Dog"
    "Felino", "Gato" -> "Cat"
    "Ave" -> "Bird"
    "Roedor" -> "Rodent"
    "Otro" -> "Other"
    "Mestizo", "Mestizo / Criollo", "Mestizo / General" -> "Mixed breed"
    "Mestizo Doméstico" -> "Domestic mixed breed"
    "Otro / Desconocido" -> "Other / Unknown"
    "Pastor Alemán" -> "German Shepherd"
    "Poodle / Caniche" -> "Poodle"
    "Husky Siberiano" -> "Siberian Husky"
    "Gran Danés" -> "Great Dane"
    "Siamés" -> "Siamese"
    "Persa" -> "Persian"
    "Bengalí" -> "Bengal"
    "Sphynx (Esfinge)" -> "Sphynx"
    "Perico / Periquito" -> "Parakeet / Budgie"
    "Canario" -> "Canary"
    "Ninfa" -> "Cockatiel"
    "Loro" -> "Parrot"
    "Cacatúa" -> "Cockatoo"
    "Hámster" -> "Hamster"
    "Cuy / Cobaya" -> "Guinea pig"
    "Conejo" -> "Rabbit"
    "Rata doméstica" -> "Domestic rat"
    "Macho" -> "Male"
    "Hembra" -> "Female"
    else -> value
})

fun serviceCategoryLabel(category: String, language: AppLanguage): String = when (category) {
    "consulta" -> language.text("Consultas", "Consultations")
    "vacunacion" -> language.text("Vacunación", "Vaccination")
    "desparasitacion" -> language.text("Desparasitación", "Deworming")
    "estetica" -> language.text("Estética", "Grooming")
    "cirugia" -> language.text("Cirugías", "Surgery")
    "diagnostico" -> language.text("Diagnóstico", "Diagnostics")
    "otros" -> language.text("Otros servicios", "Other services")
    else -> category
}

fun petAgeLabel(value: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return value
    if (value == "No especificada") return "Not specified"
    val pattern = Regex("(\\d+)\\s+(semanas?|mes(?:es)?|años?)", RegexOption.IGNORE_CASE)
    if (!Regex("^(\\d+\\s+(semanas?|mes(?:es)?|años?))(\\s+\\d+\\s+(semanas?|mes(?:es)?|años?))*$", RegexOption.IGNORE_CASE).matches(value)) return value
    return pattern.replace(value) { match ->
        val number = match.groupValues[1]
        val unit = when (match.groupValues[2].lowercase()) {
            "semana", "semanas" -> if (number == "1") "week" else "weeks"
            "mes", "meses" -> if (number == "1") "month" else "months"
            else -> if (number == "1") "year" else "years"
        }
        "$number $unit"
    }
}
