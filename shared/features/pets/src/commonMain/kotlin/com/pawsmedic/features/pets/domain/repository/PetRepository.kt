package com.pawsmedic.features.pets.domain.repository

import com.pawsmedic.features.pets.domain.model.Pet

interface PetRepository {
    suspend fun listPets(ownerId: String): Result<List<Pet>>
}
