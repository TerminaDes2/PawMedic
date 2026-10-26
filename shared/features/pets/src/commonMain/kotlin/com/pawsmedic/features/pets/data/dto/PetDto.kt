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
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val allergies: String? = null,
    val foto_url: String? = null,
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
        dateOfBirth = dateOfBirth,
        gender = gender,
        allergies = allergies,
        foto_url = foto_url,
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
            dateOfBirth = pet.dateOfBirth,
            gender = pet.gender,
            allergies = pet.allergies,
            foto_url = pet.foto_url,
            medicalId = pet.medicalId,
            weight = pet.weight,
            isInsured = pet.isInsured
        )
    }
}
