package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto

interface PetDataSource {
    suspend fun listPets(ownerId: String): List<PetDto>
}

class EmptyPetDataSource : PetDataSource {
    override suspend fun listPets(ownerId: String): List<PetDto> = emptyList()
}
