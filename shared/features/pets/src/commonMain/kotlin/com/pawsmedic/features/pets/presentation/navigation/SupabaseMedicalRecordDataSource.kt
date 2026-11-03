package com.pawsmedic.features.pets.presentation.navigation

import com.pawsmedic.features.medical_records.data.datasource.MedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class SupabaseMedicalRecordDataSource(
    private val supabase: SupabaseClient,
    private val ownerId: String
) : MedicalRecordDataSource {
    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para consultar el historial." }
        val pets = supabase.postgrest["pets"]
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<OwnedPetRecord>()
        val records = buildList {
            pets.forEach { pet ->
                addAll(getRecordsForPet(pet))
            }
        }
        return records.sortedByDescending(MedicalRecordDto::date)
    }

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para consultar el historial." }
        val pet = supabase.postgrest["pets"]
            .select {
                filter {
                    eq("id", petId)
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<OwnedPetRecord>()
            .singleOrNull() ?: return emptyList()
        return getRecordsForPet(pet)
    }

    private suspend fun getRecordsForPet(pet: OwnedPetRecord): List<MedicalRecordDto> {
        val records = supabase.postgrest["medical_records"]
            .select { filter { eq("pet_id", pet.id) } }
            .decodeList<MedicalRecordRecord>()

        return records.map { record ->
            val clinicName = supabase.postgrest["tenants"]
                .select { filter { eq("id", record.tenantId) } }
                .decodeList<TenantRecord>()
                .singleOrNull()
                ?.name
                ?: "Clínica no disponible (${record.tenantId})"

            MedicalRecordDto(
                id = record.id,
                petId = pet.id,
                petName = pet.name,
                clinicName = clinicName,
                doctorName = "Autor: ${record.authoredBy}",
                date = record.createdAt.substringBefore('T'),
                title = record.title,
                diagnosis = record.content,
                treatment = "No especificado en el registro.",
                vetNotes = null
            )
        }
    }
}

@Serializable
private data class OwnedPetRecord(val id: String, val name: String)

@Serializable
private data class MedicalRecordRecord(
    val id: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("authored_by") val authoredBy: String,
    val title: String,
    val content: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class TenantRecord(val name: String)
