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
    else -> value
})

fun petAgeLabel(value: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return value
    val match = Regex("^(\\d+) (Semanas|Meses|Años)$").matchEntire(value) ?: return value
    val unit = when (match.groupValues[2]) {
        "Semanas" -> if (match.groupValues[1] == "1") "week" else "weeks"
        "Meses" -> if (match.groupValues[1] == "1") "month" else "months"
        else -> if (match.groupValues[1] == "1") "year" else "years"
    }
    return "${match.groupValues[1]} $unit"
}
