package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabasePetDataSource(
    private val supabase: SupabaseClient
) : PetDataSource {
    override suspend fun listPets(ownerId: String): List<PetDto> =
        supabase.postgrest["pets"]
            .select {
                filter {
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<PetRecord>()
            .map { it.toPetDto() }

    override suspend fun getPetById(id: String): PetDto? =
        supabase.postgrest["pets"]
            .select {
                filter { eq("id", id) }
            }
            .decodeList<PetRecord>()
            .singleOrNull()
            ?.toPetDto()

    override suspend fun addPet(pet: PetDto): PetDto =
        supabase.postgrest["pets"]
            .insert(pet.toWriteRecord()) {
                select()
            }
            .decodeSingle<PetRecord>()
            .toPetDto()

    override suspend fun updatePet(pet: PetDto): PetDto =
        supabase.postgrest["pets"]
            .update(pet.toWriteRecord()) {
                filter { eq("id", pet.id) }
                select()
            }
            .decodeSingle<PetRecord>()
            .toPetDto()

    override suspend fun deletePet(id: String): Boolean =
        supabase.postgrest["pets"]
            .delete {
                filter { eq("id", id) }
                select()
            }
            .decodeList<PetRecord>()
            .isNotEmpty()

    private fun PetDto.toWriteRecord() = PetWriteRecord(
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed,
        dateOfBirth = dateOfBirth,
        gender = gender,
        photo_url = photo_url,
        weightGrams = weight?.removeSuffix(" kg")?.toDoubleOrNull()
            ?.let { (it * 1000).toInt() },
        allergies = allergies
    )

    private fun PetRecord.toPetDto() = PetDto(
        id = id,
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed,
        dateOfBirth = dateOfBirth,
        gender = gender,
        photo_url = photo_url,
        allergies = allergies,
        weight = weightGrams?.let { grams -> "${grams / 1000.0} kg" }
    )
}

@Serializable
private data class PetRecord(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("sex") val gender: String? = null,
    @SerialName("photo_url") val photo_url: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    val allergies: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("is_insured") val isInsured: Boolean? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class PetWriteRecord(
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("sex") val gender: String? = null,
    @SerialName("photo_url") val photo_url: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    val allergies: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("is_insured") val isInsured: Boolean? = null,
    val notes: String? = null
)
