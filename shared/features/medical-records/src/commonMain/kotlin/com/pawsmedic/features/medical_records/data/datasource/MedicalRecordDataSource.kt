package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto

interface MedicalRecordDataSource {
    suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto>
    suspend fun getAllRecords(): List<MedicalRecordDto>
}
