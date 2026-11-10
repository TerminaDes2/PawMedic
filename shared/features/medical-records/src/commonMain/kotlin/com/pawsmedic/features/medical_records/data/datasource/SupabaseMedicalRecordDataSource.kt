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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseMedicalRecordDataSource(
    private val supabase: SupabaseClient
) : MedicalRecordDataSource {

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        return supabase.postgrest["medical_records"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList()
    }

    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        return supabase.postgrest["medical_records"].select().decodeList()
    }

    override suspend fun getPetMedicalRecord(petId: String): PetMedicalRecord {
        val background = supabase.postgrest["pet_backgrounds"]
            .select { filter { eq("pet_id", petId) } }
            .decodeSingleOrNull<PetBackgroundDto>()?.toDomain() ?: PetBackground(petId = petId)
        val consultationsDto = supabase.postgrest["clinical_consultations"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList<ClinicalConsultationDto>()
        val prescriptionsDto = supabase.postgrest["prescriptions"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList<PrescriptionRecordDto>()
        val vaccinationsDto = supabase.postgrest["vaccinations"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList<VaccinationRecordDto>()
        val dewormingsDto = supabase.postgrest["dewormings"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList<DewormingRecordDto>()
        val studiesDto = supabase.postgrest["clinical_studies"]
            .select { filter { eq("pet_id", petId) } }
            .decodeList<ClinicalStudyRecordDto>()
        val auditLogsDto = supabase.postgrest["audit_logs"]
            .select { filter { eq("entity_id", petId) } }
            .decodeList<AuditLogEntryDto>()

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

        return PetMedicalRecord(
            recordNumber = "EXP-${petId.take(8).uppercase()}",
            petId = petId,
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
        return supabase.postgrest["audit_logs"]
            .select { filter { eq("entity_id", petId) } }
            .decodeList<AuditLogEntryDto>()
            .map { it.toDomain() }
    }
}
