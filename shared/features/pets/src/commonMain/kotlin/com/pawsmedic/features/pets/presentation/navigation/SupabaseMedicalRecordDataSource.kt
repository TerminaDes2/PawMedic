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
        val clinics = getClinics()
        val records = buildList {
            pets.forEach { pet ->
                addAll(getRecordsForPet(pet, clinics))
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
        return getRecordsForPet(pet, getClinics())
    }

    private suspend fun getClinics(): Map<String, String> =
        supabase.postgrest.rpc("get_owned_record_clinics").decodeList<TenantRecord>()
            .associate { it.id to it.name }

    private suspend fun getRecordsForPet(pet: OwnedPetRecord, clinics: Map<String, String>): List<MedicalRecordDto> {
        val records = supabase.postgrest["medical_records"]
            .select { filter { eq("pet_id", pet.id) } }
            .decodeList<MedicalRecordRecord>()

        return records.map { record ->
            val clinicName = clinics[record.tenantId] ?: "Clínica no disponible"

            MedicalRecordDto(
                id = record.id,
                petId = pet.id,
                petName = pet.name,
                clinicName = clinicName,
                doctorName = record.authoredBy?.let { "Autor: $it" } ?: "Autor no disponible",
                date = record.createdAt.substringBefore('T'),
                title = record.title,
                diagnosis = record.diagnosis?.takeIf { it.isNotBlank() } ?: record.content,
                treatment = record.treatment?.takeIf { it.isNotBlank() } ?: "No especificado en el registro.",
                vetNotes = record.observations?.takeIf { it.isNotBlank() }
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
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("authored_by") val authoredBy: String? = null,
    val title: String,
    val content: String,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val observations: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class TenantRecord(@SerialName("tenant_id") val id: String, val name: String)
