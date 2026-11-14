package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.AuditLogEntryDto
import com.pawsmedic.features.medical_records.data.dto.ClinicalConsultationDto
import com.pawsmedic.features.medical_records.data.dto.ClinicalStudyRecordDto
import com.pawsmedic.features.medical_records.data.dto.DewormingRecordDto
import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import com.pawsmedic.features.medical_records.data.dto.PetBackgroundDto
import com.pawsmedic.features.medical_records.data.dto.PrescriptionRecordDto
import com.pawsmedic.features.medical_records.data.dto.VaccinationRecordDto
import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import com.pawsmedic.shared.core.common.calculateAgeFromIsoDate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SupabasePetRow(
    @SerialName("id") val id: String,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("species") val species: String? = null,
    @SerialName("especie") val especie: String? = null,
    @SerialName("breed") val breed: String? = null,
    @SerialName("raza") val raza: String? = null,
    @SerialName("sex") val sex: String? = null,
    @SerialName("sexo") val sexo: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    @SerialName("peso") val peso: Double? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("allergies") val allergies: String? = null,
    @SerialName("medical_id") val medicalId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class SupabaseProfileRow(
    @SerialName("id") val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null
)

class SupabaseMedicalRecordDataSource(
    private val supabase: SupabaseClient,
    private val ownerId: String? = null
) : MedicalRecordDataSource {

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        return try {
            val consultations = supabase.postgrest["clinical_consultations"]
                .select {
                    filter { eq("pet_id", petId) }
                }
                .decodeList<ClinicalConsultationDto>()

            consultations.map { c ->
                MedicalRecordDto(
                    id = c.id ?: "",
                    petId = c.petId,
                    petName = "",
                    clinicName = "Clínica Veterinaria",
                    doctorName = "Dr. Veterinario",
                    date = c.occurredAt ?: c.createdAt ?: "",
                    title = c.reason.ifBlank { "Consulta Médica" },
                    diagnosis = c.definitiveDiagnosis.ifBlank { c.differentialDiagnoses },
                    treatment = c.treatmentPlan,
                    vetNotes = c.observations
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        return try {
            val userPets = if (!ownerId.isNullOrBlank()) {
                supabase.postgrest["pets"]
                    .select { filter { eq("owner_id", ownerId) } }
                    .decodeList<SupabasePetRow>()
            } else {
                emptyList()
            }

            if (userPets.isEmpty()) {
                return emptyList()
            }

            val petMap = userPets.associateBy { it.id }
            val petIds = userPets.map { it.id }

            val consultations = try {
                supabase.postgrest["clinical_consultations"]
                    .select {
                        filter {
                            isIn("pet_id", petIds)
                        }
                    }
                    .decodeList<ClinicalConsultationDto>()
            } catch (e: Exception) {
                emptyList()
            }

            val records = consultations.map { c ->
                val petInfo = petMap[c.petId]
                val realPetName = petInfo?.nombre ?: petInfo?.name ?: "Mascota"
                MedicalRecordDto(
                    id = c.id ?: "",
                    petId = c.petId,
                    petName = realPetName,
                    clinicName = "Clínica Veterinaria",
                    doctorName = "Dr. Veterinario",
                    date = c.occurredAt?.take(10) ?: c.createdAt?.take(10) ?: "",
                    title = c.reason.ifBlank { "Consulta Médica" },
                    diagnosis = c.definitiveDiagnosis.ifBlank { c.differentialDiagnoses.ifBlank { "Evaluación general en clínica" } },
                    treatment = c.treatmentPlan.ifBlank { "Sin tratamiento farmacológico requerido" },
                    vetNotes = c.observations
                )
            }.toMutableList()

            userPets.forEach { pet ->
                if (records.none { it.petId == pet.id }) {
                    val pName = pet.nombre ?: pet.name ?: "Mascota"
                    val pSpecies = pet.especie ?: pet.species ?: "Mascota"
                    val pBreed = pet.raza ?: pet.breed ?: ""
                    records.add(
                        MedicalRecordDto(
                            id = "reg-${pet.id}",
                            petId = pet.id,
                            petName = pName,
                            clinicName = "PawMedic Digital",
                            doctorName = "Expediente Activo",
                            date = pet.createdAt?.take(10) ?: "Reciente",
                            title = "Registro Clínico de $pName ($pSpecies $pBreed)",
                            diagnosis = "Expediente de salud registrado y activo en el sistema.",
                            treatment = if (!pet.allergies.isNullOrBlank() && !pet.allergies.contains("Ninguna", true)) {
                                "Notas de cuidado / Alergias: ${pet.allergies}"
                            } else {
                                "Sin alergias reportadas."
                            },
                            vetNotes = "Expediente listo para consultas, vacunas y prescripciones."
                        )
                    )
                }
            }

            records.sortedByDescending { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPetMedicalRecord(petId: String): PetMedicalRecord {
        // 1. Obtener datos de la mascota desde Supabase
        val petRow = try {
            supabase.postgrest["pets"]
                .select { filter { eq("id", petId) } }
                .decodeSingleOrNull<SupabasePetRow>()
        } catch (e: Exception) {
            null
        }

        // 2. Obtener datos del propietario si existe
        val ownerIdVal = petRow?.ownerId
        val ownerRow = if (!ownerIdVal.isNullOrBlank()) {
            try {
                supabase.postgrest["profiles"]
                    .select { filter { eq("id", ownerIdVal) } }
                    .decodeSingleOrNull<SupabaseProfileRow>()
            } catch (e: Exception) {
                null
            }
        } else null

        // 3. Obtener antecedentes médicos
        val background = try {
            supabase.postgrest["pet_backgrounds"]
                .select { filter { eq("pet_id", petId) } }
                .decodeSingleOrNull<PetBackgroundDto>()?.toDomain() ?: PetBackground(
                    petId = petId,
                    allergies = petRow?.allergies ?: ""
                )
        } catch (e: Exception) {
            PetBackground(petId = petId, allergies = petRow?.allergies ?: "")
        }

        // 4. Obtener consultas clínicas
        val consultationsDto = try {
            supabase.postgrest["clinical_consultations"]
                .select { filter { eq("pet_id", petId) } }
                .decodeList<ClinicalConsultationDto>()
        } catch (e: Exception) {
            emptyList()
        }

        // 5. Obtener recetas
        val prescriptionsDto = try {
            supabase.postgrest["prescriptions"]
                .select { filter { eq("pet_id", petId) } }
                .decodeList<PrescriptionRecordDto>()
        } catch (e: Exception) {
            emptyList()
        }

        // 6. Obtener vacunas
        val vaccinationsDto = try {
            supabase.postgrest["vaccinations"]
                .select { filter { eq("pet_id", petId) } }
                .decodeList<VaccinationRecordDto>()
        } catch (e: Exception) {
            emptyList()
        }

        // 7. Obtener desparasitaciones
        val dewormingsDto = try {
            supabase.postgrest["dewormings"]
                .select { filter { eq("pet_id", petId) } }
                .decodeList<DewormingRecordDto>()
        } catch (e: Exception) {
            emptyList()
        }

        // 8. Obtener estudios clínicos
        val studiesDto = try {
            supabase.postgrest["clinical_studies"]
                .select { filter { eq("pet_id", petId) } }
                .decodeList<ClinicalStudyRecordDto>()
        } catch (e: Exception) {
            emptyList()
        }

        // 9. Obtener bitácora de auditoría
        val auditLogsDto = try {
            supabase.postgrest["audit_logs"]
                .select { filter { eq("entity_id", petId) } }
                .decodeList<AuditLogEntryDto>()
        } catch (e: Exception) {
            emptyList()
        }

        val prescriptions = prescriptionsDto.map { it.toDomain() }
        val studies = studiesDto.map { it.toDomain() }
        val vaccinations = vaccinationsDto.map { it.toDomain() }
        val dewormings = dewormingsDto.map { it.toDomain() }
        val auditLogs = auditLogsDto.map { it.toDomain() }

        val consultations = consultationsDto.map { cDto ->
            cDto.toDomain(
                prescriptions = prescriptions.filter { it.consultationId == cDto.id },
                studies = studies.filter { it.consultationId == cDto.id }
            )
        }

        val petNameVal = petRow?.nombre ?: petRow?.name ?: "Mascota"
        val petSpeciesVal = petRow?.especie ?: petRow?.species ?: "Perro"
        val petBreedVal = petRow?.raza ?: petRow?.breed ?: "Mestizo"
        val petSexVal = petRow?.sexo ?: petRow?.sex ?: "Macho"
        val petWeightGramsVal = petRow?.weightGrams ?: ((petRow?.peso ?: 4.2) * 1000).toInt()
        val petPhotoVal = petRow?.fotoUrl ?: petRow?.photoUrl
        val ownerNameVal = listOfNotNull(ownerRow?.firstName, ownerRow?.lastName).joinToString(" ").ifBlank {
            ownerRow?.displayName ?: "Propietario Registrado"
        }

        val calculatedAgeVal = calculateAgeFromIsoDate(petRow?.dateOfBirth) ?: "No especificada"

        return PetMedicalRecord(
            recordNumber = petRow?.medicalId ?: "EXP-${petId.take(8).uppercase()}",
            petId = petId,
            petName = petNameVal,
            petSpecies = petSpeciesVal,
            petBreed = petBreedVal,
            petSex = petSexVal,
            petDateOfBirth = petRow?.dateOfBirth,
            petAgeCalculated = calculatedAgeVal,
            petColor = "No especificado",
            petWeightGrams = petWeightGramsVal,
            petPhotoUrl = petPhotoVal,
            petSpecialMarks = petRow?.notes ?: "Sin marcas particulares",
            petReproductiveStatus = background.reproductiveStatus.ifBlank { "Intacto" },
            ownerName = ownerNameVal,
            ownerEmail = ownerRow?.email ?: "",
            ownerPhone = ownerRow?.phone ?: "",
            createdAt = petRow?.createdAt ?: "",
            background = background,
            consultations = consultations,
            vaccinations = vaccinations,
            dewormings = dewormings,
            prescriptions = prescriptions,
            studies = studies,
            auditLogs = auditLogs
        )
    }

    override suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord>,
        studies: List<ClinicalStudyRecord>
    ): ClinicalConsultation {
        val dto = ClinicalConsultationDto.fromDomain(consultation)
        val insertedDto = supabase.postgrest["clinical_consultations"]
            .insert(dto) { select() }
            .decodeSingle<ClinicalConsultationDto>()

        val insertedId = insertedDto.id ?: consultation.id

        prescriptions.forEach { p ->
            val pDto = PrescriptionRecordDto.fromDomain(p.copy(consultationId = insertedId, petId = consultation.petId))
            supabase.postgrest["prescriptions"].insert(pDto)
        }

        studies.forEach { s ->
            val sDto = ClinicalStudyRecordDto.fromDomain(s.copy(consultationId = insertedId, petId = consultation.petId))
            supabase.postgrest["clinical_studies"].insert(sDto)
        }

        return insertedDto.toDomain(
            prescriptions = prescriptions,
            studies = studies
        )
    }

    override suspend fun updateBackground(background: PetBackground): PetBackground {
        val dto = PetBackgroundDto.fromDomain(background)
        val resultDto = supabase.postgrest["pet_backgrounds"]
            .upsert(dto) { select() }
            .decodeSingle<PetBackgroundDto>()
        return resultDto.toDomain()
    }

    override suspend fun addVaccination(vaccination: VaccinationRecord): VaccinationRecord {
        val dto = VaccinationRecordDto.fromDomain(vaccination)
        val resultDto = supabase.postgrest["vaccinations"]
            .insert(dto) { select() }
            .decodeSingle<VaccinationRecordDto>()
        return resultDto.toDomain()
    }

    override suspend fun addDeworming(deworming: DewormingRecord): DewormingRecord {
        val dto = DewormingRecordDto.fromDomain(deworming)
        val resultDto = supabase.postgrest["dewormings"]
            .insert(dto) { select() }
            .decodeSingle<DewormingRecordDto>()
        return resultDto.toDomain()
    }

    override suspend fun addPrescription(prescription: PrescriptionRecord): PrescriptionRecord {
        val dto = PrescriptionRecordDto.fromDomain(prescription)
        val resultDto = supabase.postgrest["prescriptions"]
            .insert(dto) { select() }
            .decodeSingle<PrescriptionRecordDto>()
        return resultDto.toDomain()
    }

    override suspend fun addClinicalStudy(study: ClinicalStudyRecord): ClinicalStudyRecord {
        val dto = ClinicalStudyRecordDto.fromDomain(study)
        val resultDto = supabase.postgrest["clinical_studies"]
            .insert(dto) { select() }
            .decodeSingle<ClinicalStudyRecordDto>()
        return resultDto.toDomain()
    }

    override suspend fun getAuditLogs(petId: String): List<AuditLogEntry> {
        return try {
            supabase.postgrest["audit_logs"]
                .select { filter { eq("entity_id", petId) } }
                .decodeList<AuditLogEntryDto>()
                .map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
