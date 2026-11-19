package com.pawsmedic.desktop.veterinary.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientRow(
    val id: String,
    val name: String? = null,
    val breed: String? = null,
    val species: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("sex") val gender: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    val allergies: String? = null
)

@Serializable
data class OwnerProfileRow(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val phone: String? = null,
    val email: String? = null
)

@Serializable
data class NewPetDesktopRecord(
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val species: String,
    val breed: String,
    @SerialName("sex") val gender: String = "Macho",
    @SerialName("weight_grams") val weightGrams: Int? = null,
    @SerialName("medical_id") val medicalId: String,
    val allergies: String? = null,
    val notes: String? = null
)

@Serializable
data class GrantAccessParams(
    @SerialName("p_pet_id") val petId: String,
    @SerialName("p_reason") val reason: String = "Atención Médica por ID Médico"
)
