package com.pawsmedic.features.appointments.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage

internal fun localizeAppointmentError(message: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return message
    val generic = "Ocurrió un problema. Inténtalo de nuevo más tarde."
    if (message.startsWith(generic)) {
        return "Something went wrong. Try again later." + message.removePrefix(generic)
    }
    return when (message) {
        "Ese horario ya no está disponible. Elige otro." -> "That time is no longer available. Choose another."
        "Sin conexión. Verifica tu internet e inténtalo de nuevo." -> "No connection. Check your internet and try again."
        "Selecciona una fecha desde mañana y hasta dentro de dos años." -> "Select a date from tomorrow up to two years from now."
        "No hay servicios disponibles para reservar." -> "No services are available to book."
        "No hay mascotas disponibles para reservar." -> "No pets are available to book for."
        "Selecciona una fecha y un horario disponibles." -> "Select an available date and time."
        "Faltan datos reales de clínica, servicio, mascota, fecha u horario." -> "Clinic, service, pet, date or time information is missing."
        else -> message
    }
}
