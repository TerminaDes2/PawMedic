package com.pawsmedic.features.medical_records.domain.repository

import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord

interface MedicalRecordRepository {
    suspend fun getAuthorizedMedicalHistory(petId: String): Result<List<MedicalRecord>>
    suspend fun getAllMedicalRecords(): Result<List<MedicalRecord>>

    suspend fun getPetMedicalRecord(petId: String): Result<PetMedicalRecord>
    suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord> = emptyList(),
        studies: List<ClinicalStudyRecord> = emptyList()
    ): Result<ClinicalConsultation>

    suspend fun updateBackground(background: PetBackground): Result<PetBackground>
    suspend fun addVaccination(vaccination: VaccinationRecord): Result<VaccinationRecord>
    suspend fun addDeworming(deworming: DewormingRecord): Result<DewormingRecord>
    suspend fun addPrescription(prescription: PrescriptionRecord): Result<PrescriptionRecord>
    suspend fun addClinicalStudy(study: ClinicalStudyRecord): Result<ClinicalStudyRecord>
    suspend fun getAuditLogs(petId: String): Result<List<AuditLogEntry>>
}
