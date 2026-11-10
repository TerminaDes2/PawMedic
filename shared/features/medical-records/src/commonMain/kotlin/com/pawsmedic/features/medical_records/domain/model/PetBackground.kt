package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PetBackground(
    val petId: String = "",
    val lastDewormingDate: String? = null,
    val dewormingProduct: String = "",
    val diet: String = "",
    val allergies: String = "",
    val pastDiseases: String = "",
    val pastSurgeries: String = "",
    val familyHistory: String = "",
    val reproductiveStatus: String = "",
    val habitat: String = "",
    val updatedAt: String? = null,
    val updatedBy: String? = null
)
