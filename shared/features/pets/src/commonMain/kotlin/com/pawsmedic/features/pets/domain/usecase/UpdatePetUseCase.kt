package com.pawsmedic.features.pets.domain.usecase

import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.repository.PetRepository

class UpdatePetUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.id.isNotBlank()) { "El ID de la mascota es requerido" }
        require(pet.name.isNotBlank()) { "El nombre de la mascota es requerido" }
        return repository.updatePet(pet)
    }
}
