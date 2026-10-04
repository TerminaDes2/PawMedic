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
            species = "Gato",
            breed = "Persa Mestizo",
            age = "2 años",
            gender = "Macho",
            allergies = "Alergia grave a la penicilina. Requiere limpieza de ojos diaria por su raza.",
            photoUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
            medicalId = "PM-8942-A",
            weight = "4.2 kg",
            isInsured = true
        ),
        PetDto(
            id = "pet-2",
            ownerId = "owner-1",
            name = "Joey",
            species = "Perro",
            breed = "Australian Shepard",
            age = "14 years old",
            gender = "Male",
            allergies = "Ninguna conocida",
            photoUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1",
            medicalId = "PM-1204-B",
            weight = "60 lbs",
            isInsured = true
        ),
        PetDto(
            id = "pet-3",
            ownerId = "owner-1",
            name = "Rudy",
            species = "Perro",
            breed = "Dálmata",
            age = "3 años",
            gender = "Macho",
            allergies = "Sensibilidad a ciertos granos",
            photoUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e",
            medicalId = "PM-5581-C",
            weight = "18 kg",
            isInsured = false
        ),
        PetDto(
            id = "pet-4",
            ownerId = "owner-1",
            name = "Skippy",
            species = "Gato",
            breed = "Siamés",
            age = "1 año",
            gender = "Hembra",
            allergies = "Sin alergias",
            photoUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5",
            medicalId = "PM-9011-D",
            weight = "3.1 kg",
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
