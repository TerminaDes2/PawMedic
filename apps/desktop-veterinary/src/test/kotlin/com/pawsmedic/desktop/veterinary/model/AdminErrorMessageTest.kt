package com.pawsmedic.desktop.veterinary.model

import com.pawsmedic.desktop.admin.ui.adminErrorMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AdminErrorMessageTest {
    @Test
    fun categoryFailureIsHelpfulWithoutExposingHeaders() {
        val error = IllegalStateException(
            "null value in column category of relation services violates not-null constraint\nHeaders: Authorization=Bearer secret"
        )
        val message = adminErrorMessage(error, "Error")
        assertEquals(
            "No se pudo aprobar: el servicio inicial necesita una categoría. Revisa la configuración de servicios en Supabase.",
            message
        )
        assertFalse(message.contains("secret"))
    }

    @Test
    fun unexpectedFailureNeverReturnsRawMessage() {
        val message = adminErrorMessage(IllegalStateException("Authorization=Bearer secret"), "Error")
        assertEquals("Error", message)
    }
}
