package com.pawsmedic.features.pets.domain.usecase

import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.repository.PetRepository

class GetPetByIdUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(id: String): Result<Pet?> {
        return repository.getPetById(id)
    }
}
