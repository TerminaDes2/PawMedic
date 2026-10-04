package com.pawsmedic.features.pets.domain.usecase

import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.repository.PetRepository

class GetPetsUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(ownerId: String): Result<List<Pet>> {
        return repository.listPets(ownerId)
    }
}
