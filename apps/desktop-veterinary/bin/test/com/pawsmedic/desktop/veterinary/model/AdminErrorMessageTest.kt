package com.pawsmedic.desktop.veterinary.model

import com.pawsmedic.desktop.admin.ui.adminErrorMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AdminErrorMessageTest {
    @Test
    fun arbitraryExceptionDetailsAreNotDisplayed() {
        val message = adminErrorMessage(IllegalStateException("database detail 12345"), "Inténtalo de nuevo.")
        assertEquals("Inténtalo de nuevo.", message)
        assertFalse(message.contains("database detail"))
    }

    @Test
    fun categoryFailureDoesNotExposeTechnicalDetails() {
        val error = IllegalStateException(
            "null value in column category of relation services violates not-null constraint\nHeaders: Authorization=Bearer secret"
        )
        val message = adminErrorMessage(error, "Error")
        assertEquals("Error", message)
        assertFalse(message.contains("secret"))
    }

    @Test
    fun unexpectedFailureNeverReturnsRawMessage() {
        val message = adminErrorMessage(IllegalStateException("Authorization=Bearer secret"), "Error")
        assertEquals("Error", message)
    }
}
