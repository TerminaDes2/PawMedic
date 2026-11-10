package com.pawsmedic.features.pets.presentation.navigation

import com.pawsmedic.features.medical_records.data.datasource.MedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource as MedicalRecordsDataSource
import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import io.github.jan.supabase.SupabaseClient

internal class SupabaseMedicalRecordDataSource(
    supabase: SupabaseClient,
    @Suppress("UNUSED_PARAMETER") ownerId: String
) : MedicalRecordDataSource {
    private val delegate = MedicalRecordsDataSource(supabase)

    override suspend fun getAllRecords(): List<MedicalRecordDto> = delegate.getAllRecords()

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> =
        delegate.getRecordsByPet(petId)

    override suspend fun getPetMedicalRecord(petId: String) = delegate.getPetMedicalRecord(petId)

    override suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord>,
        studies: List<ClinicalStudyRecord>
    ) = delegate.saveConsultation(consultation, prescriptions, studies)

    override suspend fun updateBackground(background: PetBackground) = delegate.updateBackground(background)

    override suspend fun addVaccination(vaccination: VaccinationRecord) = delegate.addVaccination(vaccination)

    override suspend fun addDeworming(deworming: DewormingRecord) = delegate.addDeworming(deworming)

    override suspend fun addPrescription(prescription: PrescriptionRecord) =
        delegate.addPrescription(prescription)

    override suspend fun addClinicalStudy(study: ClinicalStudyRecord) =
        delegate.addClinicalStudy(study)

    override suspend fun getAuditLogs(petId: String): List<AuditLogEntry> =
        delegate.getAuditLogs(petId)
}
