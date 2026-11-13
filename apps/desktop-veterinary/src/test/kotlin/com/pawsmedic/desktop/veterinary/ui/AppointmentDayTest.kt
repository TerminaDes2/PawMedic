package com.pawsmedic.desktop.veterinary.ui

import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppointmentDayTest {
    @Test
    fun `appointment opens on clinic calendar date and remains available afterwards`() {
        val zone = ZoneId.of("America/Mexico_City")
        val start = "2026-11-14T06:30:00Z"
        assertFalse(appointmentDayReached(start, zone, LocalDate.of(2026, 11, 13)))
        assertTrue(appointmentDayReached(start, zone, LocalDate.of(2026, 11, 14)))
        assertTrue(appointmentDayReached(start, zone, LocalDate.of(2026, 11, 15)))
    }
}
