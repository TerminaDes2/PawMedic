package com.pawsmedic.desktop.veterinary.ui

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ScheduleValidationTest {
    private val shift = ClinicShift("1", 1, "09:00:00", "12:00:00", true)

    @Test
    fun acceptsMultipleNonOverlappingShifts() {
        assertNull(shiftValidationError(1, "12:00", "18:00", listOf(shift)))
        assertNull(shiftValidationError(2, "09:00", "12:00", listOf(shift)))
    }

    @Test
    fun rejectsInvalidAndOverlappingTimes() {
        assertNotNull(shiftValidationError(1, "9:00", "12:00", emptyList()))
        assertNotNull(shiftValidationError(1, "12:00", "09:00", emptyList()))
        assertNotNull(shiftValidationError(1, "11:30", "13:00", listOf(shift)))
        assertNotNull(shiftValidationError(1, "08:00", "13:00", listOf(shift)))
        assertNotNull(shiftValidationError(7, "09:00", "12:00", emptyList()))
    }

    @Test
    fun allowsEditingSameShiftAndIgnoresInactiveShifts() {
        assertNull(shiftValidationError(1, "10:00", "12:00", listOf(shift), "1"))
        assertNull(shiftValidationError(1, "10:00", "12:00", listOf(shift.copy(active = false))))
        assertNotNull(shiftValidationError(1, "09:30", "12:00", emptyList()))
    }

    @Test
    fun validatesAllSelectedDaysBeforeSaving() {
        assertNull(shiftsValidationError(setOf(0, 2, 3), "09:00", "12:00", listOf(shift)))
        assertTrue(shiftsValidationError(emptySet(), "09:00", "12:00", emptyList())!!.contains("al menos un día"))
        assertTrue(shiftsValidationError(setOf(1, 2, 3), "10:00", "13:00", listOf(shift))!!.contains("Lunes"))
        assertNull(shiftsValidationError((0..6).toSet(), "12:00", "18:00", listOf(shift)))
    }

    @Test
    fun groupsSelectedHoursIncludingMidnight() {
        assertEquals(listOf("00:00" to "24:00"), selectedHourRanges((0..23).toSet()))
        assertEquals(listOf("08:00" to "10:00", "23:00" to "24:00"), selectedHourRanges(setOf(8, 9, 23)))
        assertEquals(emptyList(), selectedHourRanges(emptySet()))
        assertNull(shiftsValidationError(setOf(1), "23:00", "24:00", emptyList()))
    }

    @Test
    fun groupsShiftsOncePerDayWithoutDroppingIndividualRecords() {
        val mondayEarly = ClinicShift("2", 1, "06:00:00", "07:00:00", true)
        val mondayLater = ClinicShift("3", 1, "08:00:00", "10:00:00", false)
        val sunday = ClinicShift("4", 0, "09:00:00", "10:00:00", true)
        val tuesday = ClinicShift("5", 2, "09:00:00", "10:00:00", true)
        val grouped = groupShiftsByDay(listOf(sunday, mondayLater, tuesday, mondayEarly))
        assertEquals(listOf(1, 2, 0), grouped.map { it.first })
        assertEquals(listOf(mondayEarly, mondayLater), grouped.first().second)
        assertEquals(listOf(tuesday), grouped[1].second)
        assertEquals(listOf(sunday), grouped[2].second)
        assertTrue(groupShiftsByDay(emptyList()).isEmpty())
    }

    @Test
    fun selectAllForDeletionIsRestrictedToOneDay() {
        val monday = shift.copy(id = "monday")
        val inactiveMonday = shift.copy(id = "inactive", active = false)
        val tuesday = shift.copy(id = "tuesday", day = 2)
        val shifts = listOf(monday, inactiveMonday, tuesday)
        val selected = selectShiftsForDeletion(shifts, 1)
        assertEquals(setOf("monday", "inactive"), selected)
        assertEquals(setOf("monday"), selected - "inactive")
        assertEquals(emptySet(), selectShiftsForDeletion(shifts, 3))
    }
}
