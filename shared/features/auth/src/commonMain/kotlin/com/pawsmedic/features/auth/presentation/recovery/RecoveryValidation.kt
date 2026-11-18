package com.pawsmedic.features.auth.presentation.recovery

internal fun recoveryEmailError(email: String): String? =
    if (Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) null
    else "Ingresa un correo electrónico válido."

internal fun recoveryPasswordError(code: String, verified: Boolean, password: String, confirmation: String): String? =
    when {
        !verified && code.isBlank() -> "Ingresa el código recibido."
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        password != confirmation -> "Las contraseñas no coinciden."
        else -> null
    }
