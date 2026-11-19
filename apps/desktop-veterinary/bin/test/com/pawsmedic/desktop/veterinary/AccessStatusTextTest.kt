package com.pawsmedic.desktop.veterinary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccessStatusTextTest {
    @Test
    fun `known messages are translated and custom rejection reasons remain available for local translation`() {
        assertEquals("Your clinic is suspended.", knownAccessStatusEnglish("Tu clínica está suspendida."))
        assertEquals(
            "Your veterinary registration application is awaiting superadmin approval.",
            knownAccessStatusEnglish("Tu solicitud de registro veterinario está pendiente de aprobación por el Superadministrador.")
        )
        assertEquals(
            "Could not restore the session. Check your connection and sign in again.",
            knownAccessStatusEnglish("No se pudo restaurar la sesión. Revisa tu conexión e inicia sesión de nuevo.")
        )
        assertNull(knownAccessStatusEnglish("Falta documentación de la clínica."))
    }
}
