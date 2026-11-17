package com.pawsmedic.desktop.veterinary.ui.auth

internal data class AuthMessage(val spanish: String, val english: String) {
    fun text(english: Boolean): String = if (english) this.english else spanish
}

internal fun signInError(message: String): AuthMessage = when {
    message.contains("missing email or phone", ignoreCase = true) ||
        message.contains("invalid_credentials", ignoreCase = true) ||
        message.contains("invalid login", ignoreCase = true) ->
        AuthMessage("Correo o contraseña incorrectos, o la cuenta no ha sido verificada.", "Incorrect email or password, or the account has not been verified.")
    message == "Por favor ingrese correo electrónico y contraseña" ||
        message == "Por favor ingrese correo y contraseña." ->
        AuthMessage("Por favor ingrese correo electrónico y contraseña.", "Enter your email and password.")
    message == "Ingresa un correo electrónico válido." ->
        AuthMessage(message, "Enter a valid email address.")
    message == "Supabase no devolvió una sesión autenticada." ->
        AuthMessage(message, "Supabase did not return an authenticated session.")
    message == "La sesión no contiene un identificador de usuario." ->
        AuthMessage(message, "The session does not contain a user ID.")
    message == "No se pudo guardar la preferencia de sesión de forma segura." ->
        AuthMessage(message, "Could not securely save the session preference.")
    message == "Esta cuenta no tiene permisos de Superadmin." ->
        AuthMessage(message, "This account does not have superadmin permissions.")
    message == "Esta aplicación de escritorio es exclusiva para negocios veterinarios. Las cuentas de usuario de clientes deben ingresar desde la app móvil." ->
        AuthMessage(message, "This desktop application is for veterinary businesses. Customer accounts must use the mobile app.")
    else -> AuthMessage("No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo.", "Could not sign in. Check your connection and try again.")
}

internal fun photoErrorMessage(message: String): AuthMessage {
    val messages = listOf(
        AuthMessage("Selecciona una imagen JPG o PNG.", "Select a JPG or PNG image."),
        AuthMessage("Selecciona una imagen JPG o PNG válida.", "Select a valid JPG or PNG image."),
        AuthMessage("La foto debe tener un tamaño máximo de 5 MB.", "The photo must be no larger than 5 MB."),
        AuthMessage("La extensión de la foto no coincide con su contenido.", "The photo extension does not match its contents."),
        AuthMessage("La imagen no debe superar 4096 × 4096 píxeles.", "The image must not exceed 4096 × 4096 pixels."),
        AuthMessage("No se pudo preparar la foto.", "Could not prepare the photo.")
    )
    return messages.firstOrNull { message == it.spanish || message == it.english }
        ?: AuthMessage("No se pudo preparar la foto.", "Could not prepare the photo.")
}
