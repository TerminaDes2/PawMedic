package com.pawsmedic.desktop.veterinary.ui

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReportDatesTest {
    @Test
    fun validRangeIncludesBothDates() {
        assertEquals(
            LocalDate.of(2026, 2, 1) to LocalDate.of(2026, 2, 28),
            reportDates(" 2026-02-01 ", "2026-02-28")
        )
        assertEquals(LocalDate.of(2024, 2, 29) to LocalDate.of(2024, 2, 29), reportDates("2024-02-29", "2024-02-29"))
    }

    @Test
    fun rejectsInvalidOrReversedRanges() {
        assertNull(reportDates("2026-02-29", "2026-03-01"))
        assertNull(reportDates("2026-03-01", "2026-02-28"))
        assertNull(reportDates("", "2026-01-01"))
        assertNull(reportDates("0000-01-01", "2026-01-01"))
        assertNull(reportDates("2026-01-01", "9999-12-31"))
    }
}
