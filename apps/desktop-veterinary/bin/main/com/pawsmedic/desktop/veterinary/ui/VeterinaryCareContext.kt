package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.veterinary.model.PatientRow

object VeterinaryCareContext {
    var appointmentId: String? = null
    var serviceName: String = ""
    var categoryType: String = "CONSULTA_GENERAL" // CONSULTA_GENERAL, CIRUGIA, VACUNACION, ESTETICA, DIAGNOSTICO, HOSPITALIZACION, ADMINISTRATIVA, CUSTOM
    var patient: PatientRow? = null
    var prefilledReason: String = ""
    var tenantId: String? = null
    var vetUserId: String? = null
    var vetUserName: String = "Dr. Veterinario"

    fun inferCategory(service: String): String {
        val s = service.lowercase()
        return when {
            s.contains("cirug") || s.contains("surgery") -> "CIRUGIA"
            s.contains("vacun") || s.contains("vaccin") || s.contains("preventiv") -> "VACUNACION"
            s.contains("estét") || s.contains("estet") || s.contains("grooming") || s.contains("groom") || s.contains("baño") || s.contains("corte") -> "ESTETICA"
            s.contains("diag") || s.contains("laborator") || s.contains("rayos") || s.contains("ecograf") || s.contains("imagen") -> "DIAGNOSTICO"
            s.contains("hospit") || s.contains("intern") -> "HOSPITALIZACION"
            s.contains("admin") || s.contains("certific") -> "ADMINISTRATIVA"
            else -> "CONSULTA_GENERAL"
        }
    }
}
