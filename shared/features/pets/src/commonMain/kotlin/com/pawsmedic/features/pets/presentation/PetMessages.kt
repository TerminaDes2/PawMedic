package com.pawsmedic.features.pets.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage

internal fun localizePetMessage(message: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return message
    val uploadPrefix = "No se pudo subir la imagen. Verifica que exista el bucket "
    if (message.startsWith(uploadPrefix) && message.endsWith(" en Supabase.")) {
        return "Could not upload the image. Verify that the bucket " +
            message.removePrefix(uploadPrefix).removeSuffix(" en Supabase.") + " exists in Supabase."
    }
    return when (message) {
        "Error al cargar mascotas" -> "Could not load pets"
        "Error al eliminar" -> "Could not delete"
        "El nombre de la mascota debe tener al menos 2 caracteres (sin números ni símbolos)" -> "The pet's name must contain at least 2 letters (no numbers or symbols)"
        "Selecciona la especie de la mascota" -> "Select the pet's species"
        "Selecciona la raza de la mascota" -> "Select the pet's breed"
        "Ingresa una fecha de nacimiento válida o una edad aproximada." -> "Enter a valid birth date or an approximate age."
        "La fecha de nacimiento no puede ser futura." -> "The birth date cannot be in the future."
        "Ingresa un peso mayor a cero." -> "Enter a weight greater than zero."
        "Error al guardar mascota" -> "Could not save pet"
        "No se pudo cargar el perfil" -> "Could not load the profile"
        "No se pudo guardar la foto" -> "Could not save the photo"
        "No se pudo subir la imagen" -> "Could not upload the image"
        "Selecciona una imagen antes de continuar" -> "Select an image before continuing"
        "No se pudo leer la imagen seleccionada" -> "Could not read the selected image"
        "La imagen seleccionada está vacía" -> "The selected image is empty"
        "La imagen es demasiado grande. Elige una más pequeña." -> "The image is too large. Choose a smaller one."
        "El username debe tener entre 3 y 30 caracteres" -> "The username must contain between 3 and 30 characters"
        "No se pudo guardar el username" -> "Could not save the username"
        "No se pudo eliminar la cuenta. Inténtalo nuevamente." -> "Could not delete the account. Try again."
        "No se pudo desactivar el dispositivo. Comprueba tu conexión e inténtalo de nuevo." -> "Could not deactivate the device. Check your connection and try again."
        "Inténtalo nuevamente." -> "Try again."
        "No se pudieron activar las notificaciones. Comprueba tu conexión e inténtalo de nuevo." -> "Could not enable notifications. Check your connection and try again."
        "Permite las notificaciones en los ajustes de Android para activarlas." -> "Allow notifications in Android settings to enable them."
        "Permite las notificaciones de PawMedic en los ajustes de Android." -> "Allow PawMedic notifications in Android settings."
        "No se pudieron desactivar los avisos. Comprueba tu conexión e inténtalo de nuevo." -> "Could not disable notifications. Check your connection and try again."
        else -> message
    }
}
