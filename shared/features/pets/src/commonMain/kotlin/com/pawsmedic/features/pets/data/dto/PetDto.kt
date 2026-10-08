package com.pawsmedic.features.pets.data.dto

import com.pawsmedic.features.pets.domain.model.Pet
import kotlinx.serialization.Serializable

@Serializable
data class PetDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val allergies: String? = null,
    val photoUrl: String? = null,
    val medicalId: String? = null,
    val weight: String? = null,
    val isInsured: Boolean = false
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
