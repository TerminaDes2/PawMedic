package com.pawsmedic.features.auth.presentation.recovery

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RecoveryValidationTest {
    @Test
    fun emailRequiresAddress() {
        assertNotNull(recoveryEmailError("invalid"))
        assertNull(recoveryEmailError(" person@example.com "))
    }

    @Test
    fun passwordRequiresCodeLengthAndConfirmation() {
        assertNotNull(recoveryPasswordError("", false, "password", "password"))
        assertNotNull(recoveryPasswordError("123456", false, "short", "short"))
        assertNotNull(recoveryPasswordError("123456", false, "password", "other"))
        assertNull(recoveryPasswordError("123456", false, "password", "password"))
        assertNull(recoveryPasswordError("", true, "password", "password"))
    }
}
