package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto

interface MedicalRecordDataSource {
    suspend fun getAuthorizedMedicalHistory(petId: String): List<MedicalRecordDto>
}

class EmptyMedicalRecordDataSource : MedicalRecordDataSource {
    override suspend fun getAuthorizedMedicalHistory(petId: String): List<MedicalRecordDto> =
        emptyList()
}
