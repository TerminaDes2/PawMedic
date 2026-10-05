package com.pawsmedic.features.medical_records.data.repository

import com.pawsmedic.features.medical_records.data.datasource.MedicalRecordDataSource
import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class DefaultMedicalRecordRepository(
    private val dataSource: MedicalRecordDataSource
) : MedicalRecordRepository {
    override suspend fun getAuthorizedMedicalHistory(
        petId: String
    ): Result<List<MedicalRecord>> = try {
        Result.success(
            dataSource.getRecordsByPet(petId).map { it.toDomain() }
        )
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun getAllMedicalRecords(): Result<List<MedicalRecord>> = try {
        Result.success(
            dataSource.getAllRecords().map { it.toDomain() }
        )
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
