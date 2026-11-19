package com.pawsmedic.desktop.veterinary.ui

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateAvailabilityTest {
    @Test
    fun `calendar aligns monday first and includes leap day`() {
        val days = calendarDays(YearMonth.of(2028, 2))
        assertNull(days[0])
        assertEquals(LocalDate.of(2028, 2, 1), days[1])
        assertEquals(LocalDate.of(2028, 2, 29), days.filterNotNull().last())
        assertEquals(0, days.size % 7)
    }
}
