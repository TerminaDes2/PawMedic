package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord

interface MedicalRecordDataSource {
    suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto>
    suspend fun getAllRecords(): List<MedicalRecordDto>

    suspend fun getPetMedicalRecord(petId: String): PetMedicalRecord
    suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord> = emptyList(),
        studies: List<ClinicalStudyRecord> = emptyList()
    ): ClinicalConsultation

    suspend fun updateBackground(background: PetBackground): PetBackground
    suspend fun addVaccination(vaccination: VaccinationRecord): VaccinationRecord
    suspend fun addDeworming(deworming: DewormingRecord): DewormingRecord
    suspend fun addPrescription(prescription: PrescriptionRecord): PrescriptionRecord
    suspend fun addClinicalStudy(study: ClinicalStudyRecord): ClinicalStudyRecord
    suspend fun getAuditLogs(petId: String): List<AuditLogEntry>
}
