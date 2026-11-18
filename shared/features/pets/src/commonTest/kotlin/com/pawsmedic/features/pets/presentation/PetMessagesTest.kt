package com.pawsmedic.features.pets.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class PetMessagesTest {
    @Test
    fun translatesValidationAndRetainsUnknownErrors() {
        assertEquals("Select the pet's species", localizePetMessage("Selecciona la especie de la mascota", AppLanguage.ENGLISH))
        assertEquals("Selecciona la especie de la mascota", localizePetMessage("Selecciona la especie de la mascota", AppLanguage.SPANISH))
        assertEquals("custom error", localizePetMessage("custom error", AppLanguage.ENGLISH))
        assertEquals(
            "Could not upload the image. Verify that the bucket \"pets\" exists in Supabase.",
            localizePetMessage("No se pudo subir la imagen. Verifica que exista el bucket \"pets\" en Supabase.", AppLanguage.ENGLISH)
        )
    }
}
