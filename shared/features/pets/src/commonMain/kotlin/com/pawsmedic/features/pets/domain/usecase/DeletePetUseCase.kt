package com.pawsmedic.features.pets.domain.usecase

import com.pawsmedic.features.pets.domain.repository.PetRepository

class DeletePetUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(id: String): Result<Boolean> {
        require(id.isNotBlank()) { "El ID de la mascota es requerido" }
        return repository.deletePet(id)
    }
}
