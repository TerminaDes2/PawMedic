package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClinicalStudyRecord(
    val id: String = "",
    val consultationId: String? = null,
    val petId: String = "",
    val studyType: String = "LABORATORY", // LABORATORY, XRAY, ULTRASOUND, CYTOLOGY, HEMOGRAM, BLOOD_CHEMISTRY, OTHER
    val name: String = "",
    val status: String = "COMPLETED",
    val requestedAt: String = "",
    val resultsNotes: String = "",
    val attachmentUrls: List<String> = emptyList()
)
