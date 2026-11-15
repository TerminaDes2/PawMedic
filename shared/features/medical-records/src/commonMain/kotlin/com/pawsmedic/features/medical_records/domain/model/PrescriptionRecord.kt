package com.pawsmedic.features.medical_records.domain.model

import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import kotlinx.serialization.Serializable

@Serializable
data class PrescriptionRecord(
    val id: String = "",
    val consultationId: String? = null,
    val petId: String = "",
    val petName: String = "",
    val clinicName: String = "Clínica Veterinaria",
    val drugName: String = "",
    val dosage: String = "",
    val unit: String = "mg",
    val route: String = "Oral", // Oral, Intravenosa, Intramuscular, Subcutánea, Tópica
    val frequency: String = "Cada 12h",
    val duration: String = "7 días",
    val startDate: String? = null,
    val endDate: String? = null, // Mandatory Expiration Date
    val isAdministeredInConsultation: Boolean = false,
    val prescribingVetName: String = "",
    val notes: String = ""
) {
    val isActive: Boolean
        get() {
            if (endDate.isNullOrBlank()) return true
            val todayIso = convertMillisToIsoDateString(System.currentTimeMillis())
            return endDate >= todayIso
        }
}
