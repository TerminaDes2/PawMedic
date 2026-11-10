package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PetMedicalRecord(
    val recordNumber: String = "",
    val petId: String = "",
    val petName: String = "",
    val petSpecies: String = "",
    val petBreed: String = "",
    val petSex: String = "",
    val petDateOfBirth: String? = null,
    val petAgeCalculated: String = "",
    val petColor: String = "",
    val petWeightGrams: Int? = null,
    val petPhotoUrl: String? = null,
    val petSpecialMarks: String = "",
    val petReproductiveStatus: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",
    val ownerPhone: String = "",
    val createdAt: String = "",
    val background: PetBackground = PetBackground(),
    val consultations: List<ClinicalConsultation> = emptyList(),
    val vaccinations: List<VaccinationRecord> = emptyList(),
    val dewormings: List<DewormingRecord> = emptyList(),
    val prescriptions: List<PrescriptionRecord> = emptyList(),
    val studies: List<ClinicalStudyRecord> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList()
)
