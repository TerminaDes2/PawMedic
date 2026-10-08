package com.pawsmedic.features.medical_records.data.dto

import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import kotlinx.serialization.Serializable

@Serializable
data class MedicalRecordDto(
    val id: String,
    val petId: String,
    val petName: String,
    val clinicName: String,
    val doctorName: String,
    val date: String,
    val title: String,
    val diagnosis: String,
    val treatment: String,
    val vetNotes: String? = null
) {
    fun toDomain() = MedicalRecord(
        id = id,
        petId = petId,
        petName = petName,
        clinicName = clinicName,
        doctorName = doctorName,
        date = date,
        title = title,
        diagnosis = diagnosis,
        treatment = treatment,
        vetNotes = vetNotes
    )
}
