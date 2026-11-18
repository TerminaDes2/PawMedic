package com.pawsmedic.features.appointments.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentMessagesTest {
    @Test
    fun localizesErrorsWithoutDiscardingDetails() {
        assertEquals("No connection. Check your internet and try again.", localizeAppointmentError("Sin conexión. Verifica tu internet e inténtalo de nuevo.", AppLanguage.ENGLISH))
        assertEquals("Something went wrong. Try again later. (detail)", localizeAppointmentError("Ocurrió un problema. Inténtalo de nuevo más tarde. (detail)", AppLanguage.ENGLISH))
        assertEquals("unknown", localizeAppointmentError("unknown", AppLanguage.ENGLISH))
    }
}
