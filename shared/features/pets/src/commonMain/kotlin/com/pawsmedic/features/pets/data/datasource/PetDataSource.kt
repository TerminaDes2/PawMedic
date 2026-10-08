package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto

interface PetDataSource {
    suspend fun listPets(ownerId: String): List<PetDto>
    suspend fun getPetById(id: String): PetDto?
    suspend fun addPet(pet: PetDto): PetDto
    suspend fun updatePet(pet: PetDto): PetDto
    suspend fun deletePet(id: String): Boolean
}

class InMemoryPetDataSource : PetDataSource {
    private val pets = mutableListOf(
        PetDto(
            id = "pet-1",
            ownerId = "owner-1",
            name = "Tobías",
            species = listOf("Gato"),
            breed = listOf("Persa Mestizo"),
            age = 2,
            gender = "Macho",
            allergies = "Alergia grave a la penicilina. Requiere limpieza de ojos diaria por su raza.",
            photoUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
            medicalId = "PM-8942-A",
            weight = 4.2,
            isInsured = true
        ),
        PetDto(
            id = "pet-2",
            ownerId = "owner-1",
            name = "Joey",
            species = listOf("Perro"),
            breed = listOf("Australian Shepard"),
            age = 14,
            gender = "Male",
            allergies = "Ninguna conocida",
            photoUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1",
            medicalId = "PM-1204-B",
            weight = 27.2,
            isInsured = true
        ),
        PetDto(
            id = "pet-3",
            ownerId = "owner-1",
            name = "Rudy",
            species = listOf("Perro"),
            breed = listOf("Dálmata"),
            age = 3,
            gender = "Macho",
            allergies = "Sensibilidad a ciertos granos",
            photoUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e",
            medicalId = "PM-5581-C",
            weight = 18.0,
            isInsured = false
        ),
        PetDto(
            id = "pet-4",
            ownerId = "owner-1",
            name = "Skippy",
            species = listOf("Gato"),
            breed = listOf("Siamés"),
            age = 1,
            gender = "Hembra",
            allergies = "Sin alergias",
            photoUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5",
            medicalId = "PM-9011-D",
            weight = 3.1,
            isInsured = false
        )
    )

    override suspend fun listPets(ownerId: String): List<PetDto> {
        return pets.filter { ownerId.isEmpty() || it.ownerId == ownerId }
    }

    override suspend fun getPetById(id: String): PetDto? {
        return pets.find { it.id == id }
    }

    override suspend fun addPet(pet: PetDto): PetDto {
        val newPet = if (pet.id.isBlank()) pet.copy(id = "pet-${pets.size + 1}") else pet
        pets.add(0, newPet)
        return newPet
    }

    override suspend fun updatePet(pet: PetDto): PetDto {
        val index = pets.indexOfFirst { it.id == pet.id }
        if (index != -1) {
            pets[index] = pet
            return pet
        } else {
            error("Pet not found with ID: ${pet.id}")
        }
    }

    override suspend fun deletePet(id: String): Boolean {
        return pets.removeAll { it.id == id }
    }
}
