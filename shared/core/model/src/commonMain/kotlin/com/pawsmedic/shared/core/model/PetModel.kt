package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Modelo alineado con la tabla `pets` de Supabase. */
@Serializable
data class PetModel(
    @SerialName("id") val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("name") val name: String,
    @SerialName("breed_id") val breedId: Int? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("allergies") val allergies: String? = null,
    @SerialName("sex") val sex: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    @SerialName("photo_url") val photo_url: String? = null,
    @SerialName("species") val species: String,
    @SerialName("breed") val breed: String,
    @SerialName("is_insured") val isInsured: Boolean? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
