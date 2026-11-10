package com.pawsmedic.features.medical_records.data.repository

import com.pawsmedic.features.medical_records.data.datasource.MedicalRecordDataSource
import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class DefaultMedicalRecordRepository(
    private val dataSource: MedicalRecordDataSource
) : MedicalRecordRepository {

    override suspend fun getAuthorizedMedicalHistory(petId: String): Result<List<MedicalRecord>> = try {
        val dtos = dataSource.getRecordsByPet(petId)
        Result.success(dtos.map { it.toDomain() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllMedicalRecords(): Result<List<MedicalRecord>> = try {
        val dtos = dataSource.getAllRecords()
        Result.success(dtos.map { it.toDomain() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPetMedicalRecord(petId: String): Result<PetMedicalRecord> = try {
        val record = dataSource.getPetMedicalRecord(petId)
        Result.success(record)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord>,
        studies: List<ClinicalStudyRecord>
    ): Result<ClinicalConsultation> = try {
        val result = dataSource.saveConsultation(consultation, prescriptions, studies)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateBackground(background: PetBackground): Result<PetBackground> = try {
        val result = dataSource.updateBackground(background)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addVaccination(vaccination: VaccinationRecord): Result<VaccinationRecord> = try {
        val result = dataSource.addVaccination(vaccination)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addDeworming(deworming: DewormingRecord): Result<DewormingRecord> = try {
        val result = dataSource.addDeworming(deworming)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addPrescription(prescription: PrescriptionRecord): Result<PrescriptionRecord> = try {
        val result = dataSource.addPrescription(prescription)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addClinicalStudy(study: ClinicalStudyRecord): Result<ClinicalStudyRecord> = try {
        val result = dataSource.addClinicalStudy(study)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAuditLogs(petId: String): Result<List<AuditLogEntry>> = try {
        val logs = dataSource.getAuditLogs(petId)
        Result.success(logs)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
