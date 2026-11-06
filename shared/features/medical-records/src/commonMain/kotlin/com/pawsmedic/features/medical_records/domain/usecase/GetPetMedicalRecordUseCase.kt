package com.pawsmedic.features.medical_records.domain.usecase

import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class GetPetMedicalRecordUseCase(
    private val repository: MedicalRecordRepository
) {
    suspend operator fun invoke(petId: String): Result<PetMedicalRecord> {
        return repository.getPetMedicalRecord(petId)
    }
}
