package com.pawsmedic.desktop.veterinary.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AgendaStatusTest {
    @Test
    fun `appointment statuses are localized without changing unknown values`() {
        val statuses = listOf(
            Triple("REQUESTED", "Pendiente", "Pending"),
            Triple("CONFIRMED", "Confirmada", "Confirmed"),
            Triple("IN_PROGRESS", "En progreso", "In progress"),
            Triple("CANCELLED", "Cancelada", "Cancelled"),
            Triple("COMPLETED", "Completada", "Completed"),
            Triple("NO_SHOW", "No asistió", "No-show"),
            Triple("REJECTED", "Rechazada", "Rejected")
        )
        statuses.forEach { (status, spanish, english) ->
            assertEquals(spanish, agendaStatus(status, false))
            assertEquals(english, agendaStatus(status.lowercase(), true))
        }
        assertEquals("UNKNOWN", agendaStatus("UNKNOWN", true))
    }
}
