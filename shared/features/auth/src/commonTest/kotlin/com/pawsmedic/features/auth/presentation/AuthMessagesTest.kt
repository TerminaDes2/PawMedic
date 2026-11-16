package com.pawsmedic.features.auth.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthMessagesTest {
    @Test
    fun localizesKnownMessagesAndPreservesUnknownErrors() {
        val message = "Las contraseñas no coinciden."
        assertEquals(message, localizeAuthMessage(message, AppLanguage.SPANISH))
        assertEquals("Passwords do not match.", localizeAuthMessage(message, AppLanguage.ENGLISH))
        assertEquals("Unexpected server error", localizeAuthMessage("Unexpected server error", AppLanguage.ENGLISH))
    }
}
