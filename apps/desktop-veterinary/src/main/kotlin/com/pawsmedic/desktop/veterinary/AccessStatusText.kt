package com.pawsmedic.desktop.veterinary

internal fun knownAccessStatusEnglish(message: String): String? = when (message) {
    "Tu solicitud de registro veterinario está pendiente de aprobación por el Superadministrador." ->
        "Your veterinary registration application is awaiting superadmin approval."
    "Tu solicitud de registro veterinario está pendiente de aprobación." ->
        "Your veterinary registration application is awaiting approval."
    "Tu solicitud de registro fue rechazada por el Superadministrador." ->
        "Your registration application was rejected by the superadmin."
    "Tu solicitud de registro veterinario fue rechazada." ->
        "Your veterinary registration application was rejected."
    "Tu clínica está suspendida." -> "Your clinic is suspended."
    "No se pudo confirmar la aprobación de tu solicitud veterinaria." ->
        "Could not confirm the approval of your veterinary application."
    "Esta cuenta no tiene acceso a la aplicación de escritorio." ->
        "This account does not have access to the desktop application."
    "No se pudo restaurar la sesión. Revisa tu conexión e inicia sesión de nuevo." ->
        "Could not restore the session. Check your connection and sign in again."
    "No se pudo cerrar la sesión. Revisa tu conexión e inténtalo de nuevo." ->
        "Could not sign out. Check your connection and try again."
    else -> null
}
