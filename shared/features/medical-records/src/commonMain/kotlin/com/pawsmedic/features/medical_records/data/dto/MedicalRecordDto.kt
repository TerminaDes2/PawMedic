package com.pawsmedic.features.medical_records.data.dto

import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import kotlinx.serialization.Serializable

@Serializable
data class MedicalRecordDto(
    val id: String,
    val petId: String,
    val businessId: String,
    val recordedAt: String,
    val summary: String,
    val details: String? = null
) {
    fun toDomain() = MedicalRecord(id, petId, businessId, recordedAt, summary, details)
}
