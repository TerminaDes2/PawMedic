package com.pawsmedic.features.pets.domain.repository

import com.pawsmedic.features.pets.domain.model.Pet

interface PetRepository {
    suspend fun listPets(ownerId: String): Result<List<Pet>>
    suspend fun getPetById(id: String): Result<Pet?>
    suspend fun addPet(pet: Pet): Result<Pet>
    suspend fun updatePet(pet: Pet): Result<Pet>
    suspend fun deletePet(id: String): Result<Boolean>
}
