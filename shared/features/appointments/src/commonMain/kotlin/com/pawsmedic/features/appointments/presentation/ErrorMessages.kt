package com.pawsmedic.features.appointments.presentation

internal fun friendlyErrorMessage(error: Throwable): String {
    val raw = error.message.orEmpty().lowercase()
    return when {
        "no longer available" in raw -> "Ese horario ya no está disponible. Elige otro."
        "network" in raw || "unable to resolve" in raw || "timeout" in raw ->
            "Sin conexión. Verifica tu internet e inténtalo de nuevo."
        else -> "Ocurrió un problema. Inténtalo de nuevo más tarde."
    }
}
