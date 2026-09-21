package com.pawsmedic.features.pets.data.repository

import com.pawsmedic.features.pets.data.datasource.PetDataSource
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.repository.PetRepository

class DefaultPetRepository(
    private val dataSource: PetDataSource
) : PetRepository {
    override suspend fun listPets(ownerId: String): Result<List<Pet>> = try {
        Result.success(dataSource.listPets(ownerId).map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
