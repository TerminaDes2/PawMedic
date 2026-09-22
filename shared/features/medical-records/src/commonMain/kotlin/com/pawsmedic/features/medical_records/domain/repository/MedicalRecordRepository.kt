package com.pawsmedic.features.medical_records.domain.repository

import com.pawsmedic.features.medical_records.domain.model.MedicalRecord

interface MedicalRecordRepository {
    suspend fun getAuthorizedMedicalHistory(petId: String): Result<List<MedicalRecord>>
}
