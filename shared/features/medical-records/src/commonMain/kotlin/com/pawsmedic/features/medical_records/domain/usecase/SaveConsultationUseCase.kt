package com.pawsmedic.features.medical_records.domain.usecase

import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository

class SaveConsultationUseCase(
    private val repository: MedicalRecordRepository
) {
    suspend operator fun invoke(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord> = emptyList(),
        studies: List<ClinicalStudyRecord> = emptyList()
    ): Result<ClinicalConsultation> {
        return repository.saveConsultation(consultation, prescriptions, studies)
    }
}
