package com.pawsmedic.features.pets.data.repository

import com.pawsmedic.features.pets.data.datasource.PetDataSource
import com.pawsmedic.features.pets.data.dto.PetDto
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

    override suspend fun getPetById(id: String): Result<Pet?> = try {
        Result.success(dataSource.getPetById(id)?.toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun addPet(pet: Pet): Result<Pet> = try {
        val dto = PetDto.fromDomain(pet)
        Result.success(dataSource.addPet(dto).toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun updatePet(pet: Pet): Result<Pet> = try {
        val dto = PetDto.fromDomain(pet)
        Result.success(dataSource.updatePet(dto).toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun deletePet(id: String): Result<Boolean> = try {
        Result.success(dataSource.deletePet(id))
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
