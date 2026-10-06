package com.pawsmedic.features.pets.domain.usecase

import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.repository.PetRepository

class AddPetUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.name.isNotBlank()) { "El nombre de la mascota es requerido" }
        require(pet.species.isNotBlank()) { "La especie de la mascota es requerida" }
        return repository.addPet(pet)
    }
}
