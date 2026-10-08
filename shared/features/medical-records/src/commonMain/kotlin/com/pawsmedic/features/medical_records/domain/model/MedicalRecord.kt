package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MedicalRecord(
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
)
