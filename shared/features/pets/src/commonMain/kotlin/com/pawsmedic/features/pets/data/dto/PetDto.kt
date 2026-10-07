package com.pawsmedic.features.pets.data.dto

import com.pawsmedic.features.pets.domain.model.Pet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PetDto(
    @SerialName("id") val id: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("nombre") val name: String = "",
    @SerialName("especie") val species: String = "Perro",
    @SerialName("raza") val breed: String? = null,
    @SerialName("edad") val age: String? = null,
    @SerialName("sexo") val gender: String? = null,
    @SerialName("alergias") val allergies: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("peso") val weight: String? = null,
    @SerialName("is_insured") val isInsured: Boolean = false
) {
    fun toDomain() = Pet(
        id = id,
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed,
        age = age,
        gender = gender,
        allergies = allergies,
        photoUrl = photoUrl,
        medicalId = medicalId,
        weight = weight,
        isInsured = isInsured
    )

    companion object {
        fun fromDomain(pet: Pet) = PetDto(
            id = pet.id,
            ownerId = pet.ownerId,
            name = pet.name,
            species = pet.species,
            breed = pet.breed,
            age = pet.age,
            gender = pet.gender,
            allergies = pet.allergies,
            photoUrl = pet.photoUrl,
            medicalId = pet.medicalId,
            weight = pet.weight,
            isInsured = pet.isInsured
        )
    }
}