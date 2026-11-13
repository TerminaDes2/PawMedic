package com.pawsmedic.features.appointments.presentation.booking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingDateRangeTest {
    @Test
    fun `only dates from tomorrow through two calendar years are valid`() {
        val today = "2026-12-31"
        assertFalse(isBookableDate("2026-12-30", today))
        assertFalse(isBookableDate(today, today))
        assertTrue(isBookableDate("2027-01-01", today))
        assertTrue(isBookableDate("2028-12-31", today))
        assertFalse(isBookableDate("2029-01-01", today))
        assertFalse(isBookableDate("2027-02-29", today))
        assertTrue(isBookableDate("2028-02-29", today))
        assertFalse(isBookableDate("2027-13-01", today))
    }

    @Test
    fun `two year limit clamps leap day`() {
        assertEquals("2026-02-28", bookingDateLimit("2024-02-29"))
        assertTrue(isBookableDate("2026-02-28", "2024-02-29"))
        assertFalse(isBookableDate("2026-03-01", "2024-02-29"))
    }
}
