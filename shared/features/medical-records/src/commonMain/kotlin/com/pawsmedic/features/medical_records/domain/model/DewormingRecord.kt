package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DewormingRecord(
    val id: String = "",
    val petId: String = "",
    val consultationId: String? = null,
    val productName: String = "",
    val dosage: String = "",
    val appliedDate: String = "",
    val nextDueDate: String? = null,
    val appliedByName: String = "",
    val notes: String = ""
)
