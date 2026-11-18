package com.pawsmedic.features.auth.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage

internal fun localizeAuthMessage(message: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return message
    return when (message) {
        "Ingresa tu correo electrónico" -> "Enter your email address"
        "Ingresa un correo electrónico válido (ej. usuario@dominio.com)" -> "Enter a valid email address (e.g. user@example.com)"
        "Ingresa tu contraseña" -> "Enter your password"
        "El correo electrónico es requerido" -> "Email address is required"
        "La contraseña es requerida" -> "Password is required"
        "El nombre es requerido" -> "First name is required"
        "El apellido es requerido" -> "Last name is required"
        "No se pudo obtener el token de Google" -> "Could not obtain the Google token"
        "Esta aplicación móvil es exclusiva para clientes. Las cuentas de veterinaria o superadmin deben iniciar sesión desde la aplicación de escritorio." -> "This mobile app is for customers only. Veterinary and superadmin accounts must sign in on the desktop app."
        "No se pudo iniciar sesión con Google" -> "Could not sign in with Google"
        "No se pudo registrar con Google" -> "Could not register with Google"
        "El correo o la contraseña son incorrectos" -> "The email or password is incorrect"
        "Tu correo electrónico no ha sido verificado aún. Por favor confirma tu cuenta" -> "Your email has not been verified yet. Please confirm your account"
        "Error al iniciar sesión. Verifica tu conexión e inténtalo de nuevo." -> "Could not sign in. Check your connection and try again."
        "El inicio de sesión con Google no está configurado" -> "Google sign-in is not configured"
        "Error al iniciar sesión con Google" -> "Could not sign in with Google"
        "Ingresa tus nombres" -> "Enter your first name"
        "Ingresa tus apellidos" -> "Enter your last name"
        "El nombre de usuario debe tener entre 3 y 30 caracteres" -> "Your username must be between 3 and 30 characters"
        "Ingresa un correo electrónico válido" -> "Enter a valid email address"
        "Ingresa un teléfono válido de 10 dígitos" -> "Enter a valid 10-digit phone number"
        "La contraseña debe tener al menos 6 caracteres" -> "Your password must be at least 6 characters"
        "Debes aceptar los Términos y Condiciones" -> "You must accept the terms and conditions"
        "Este correo electrónico ya está registrado. Intenta iniciar sesión." -> "This email address is already registered. Try signing in."
        "Error al registrarse. Inténtalo de nuevo." -> "Registration failed. Try again."
        "Ingresa el código completo de 6 dígitos" -> "Enter the complete 6-digit code"
        "La verificación de correo no está configurada" -> "Email verification is not configured"
        "El código ingresado es incorrecto o ha expirado. Verifica el código e inténtalo de nuevo." -> "The code is incorrect or has expired. Check it and try again."
        "El reenvío de códigos no está configurado" -> "Code resending is not configured"
        "Se ha reenviado un nuevo código a tu correo." -> "A new code has been sent to your email."
        "Error al reenviar el código" -> "Could not resend the code"
        "El registro con Google no está configurado" -> "Google registration is not configured"
        "Error al registrarse con Google" -> "Could not register with Google"
        "Ingresa un correo electrónico válido." -> "Enter a valid email address."
        "Ingresa el código recibido." -> "Enter the code you received."
        "La contraseña debe tener al menos 6 caracteres." -> "Your password must be at least 6 characters."
        "Las contraseñas no coinciden." -> "Passwords do not match."
        "No se pudo cerrar la sesión de recuperación. Verifica tu conexión." -> "Could not close the recovery session. Check your connection."
        "No se pudo solicitar el código. Inténtalo de nuevo." -> "Could not request the code. Try again."
        "No se pudo verificar el código. Compruébalo e inténtalo de nuevo." -> "Could not verify the code. Check it and try again."
        "La contraseña se actualizó, pero no se pudo cerrar la sesión de recuperación." -> "The password was updated, but the recovery session could not be closed."
        "No se pudo actualizar la contraseña. Inténtalo de nuevo." -> "Could not update the password. Try again."
        else -> message
    }
}
