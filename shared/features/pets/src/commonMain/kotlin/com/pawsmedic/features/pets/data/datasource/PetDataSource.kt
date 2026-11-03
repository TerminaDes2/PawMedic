package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto

interface PetDataSource {
    suspend fun listPets(ownerId: String): List<PetDto>
    suspend fun getPetById(id: String): PetDto?
    suspend fun addPet(pet: PetDto): PetDto
    suspend fun updatePet(pet: PetDto): PetDto
    suspend fun deletePet(id: String): Boolean
}
