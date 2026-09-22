package com.pawsmedic.features.medical_records.domain.model

data class MedicalRecord(
    val id: String,
    val petId: String,
    val businessId: String,
    val recordedAt: String,
    val summary: String,
    val details: String? = null
)
