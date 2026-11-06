package com.pawsmedic.features.medical_records.domain.usecase

import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class UpdatePetBackgroundUseCase(
    private val repository: MedicalRecordRepository
) {
    suspend operator fun invoke(background: PetBackground): Result<PetBackground> {
        return repository.updateBackground(background)
    }
}
