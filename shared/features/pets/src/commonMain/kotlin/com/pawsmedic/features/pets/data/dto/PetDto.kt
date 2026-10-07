package com.pawsmedic.features.pets.data.dto

import com.pawsmedic.features.pets.domain.model.Pet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO para la tabla 'pets' en Supabase PostgreSQL.
 * Mapea 'especie' y 'raza' como listas (`List<String>?`) para ser 100% compatible con columnas de tipo Array (`TEXT[]`)
 * si están configuradas así en la base de datos de Supabase.
 */
@Serializable
data class PetDto(
    @SerialName("id") val id: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("nombre") val name: String = "",
    @SerialName("especie") val speciesList: List<String>? = null,
    @SerialName("raza") val breedList: List<String>? = null,
    @SerialName("edad") val age: Int? = null,
    @SerialName("sexo") val gender: String? = null,
    @SerialName("alergias") val allergies: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("peso") val weight: Double? = null,
    @SerialName("is_insured") val isInsured: Boolean = false
) {
    val species: String
        get() = speciesList?.firstOrNull() ?: "Perro"

    val breed: String?
        get() = breedList?.firstOrNull() ?: breedList?.joinToString(", ")

    constructor(
        id: String = "",
        ownerId: String = "",
        name: String = "",
        species: String = "Perro",
        breed: String? = null,
        age: String? = null,
        gender: String? = null,
        allergies: String? = null,
        photoUrl: String? = null,
        medicalId: String? = null,
        weight: String? = null,
        isInsured: Boolean = false
    ) : this(
        id = id,
        ownerId = ownerId,
        name = name,
        speciesList = listOf(species.ifBlank { "Perro" }),
        breedList = breed?.takeIf { it.isNotBlank() }?.let { listOf(it) },
        age = age?.filter { it.isDigit() }?.toIntOrNull(),
        gender = gender,
        allergies = allergies,
        photoUrl = photoUrl,
        medicalId = medicalId,
        weight = weight?.replace("kg", "")?.replace("lbs", "")?.trim()?.toDoubleOrNull(),
        isInsured = isInsured
    )

    fun toDomain() = Pet(
        id = id,
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed,
        age = age?.toString(),
        gender = gender,
        allergies = allergies,
        photoUrl = photoUrl,
        medicalId = medicalId,
        weight = weight?.toString(),
        isInsured = isInsured
    )

    companion object {
        fun fromDomain(pet: Pet) = PetDto(
            id = pet.id,
            ownerId = pet.ownerId,
            name = pet.name,
            speciesList = listOf(pet.species.ifBlank { "Perro" }),
            breedList = pet.breed?.takeIf { it.isNotBlank() }?.let { listOf(it) },
            age = pet.age?.filter { it.isDigit() }?.toIntOrNull(),
            gender = pet.gender,
            allergies = pet.allergies,
            photoUrl = pet.photoUrl,
            medicalId = pet.medicalId,
            weight = pet.weight?.replace("kg", "")?.replace("lbs", "")?.trim()?.toDoubleOrNull(),
            isInsured = pet.isInsured
        )
    }
}
