package com.pawsmedic.features.medical_records.domain.usecase

import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class GetAuthorizedMedicalHistoryUseCase(
    private val repository: MedicalRecordRepository
) {
    suspend operator fun invoke(petId: String): Result<List<MedicalRecord>> =
        repository.getAuthorizedMedicalHistory(petId)
}
