package com.pawsmedic.features.pets.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("sex") val gender: String? = null,
    val allergies: String? = null,
    val photo_url: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    val weight: String? = null,
    @SerialName("is_insured") val isInsured: Boolean = false,
    @SerialName("breed_id") val breedId: Int? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
