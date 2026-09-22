package com.pawsmedic.features.pets.data.dto

import com.pawsmedic.features.pets.domain.model.Pet
import kotlinx.serialization.Serializable

@Serializable
data class PetDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null
) {
    fun toDomain() = Pet(id, ownerId, name, species, breed)
}
