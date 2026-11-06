package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PrescriptionRecord(
    val id: String = "",
    val consultationId: String? = null,
    val petId: String = "",
    val drugName: String = "",
    val dosage: String = "",
    val unit: String = "mg",
    val route: String = "Oral", // Oral, Intravenosa, Intramuscular, Subcutánea, Tópica
    val frequency: String = "Cada 12h",
    val duration: String = "7 días",
    val startDate: String? = null,
    val endDate: String? = null,
    val isAdministeredInConsultation: Boolean = false,
    val prescribingVetName: String = "",
    val notes: String = ""
)
