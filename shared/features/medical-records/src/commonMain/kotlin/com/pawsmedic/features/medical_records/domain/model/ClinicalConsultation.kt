package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClinicalConsultation(
    val id: String = "",
    val petId: String = "",
    val tenantId: String? = null,
    val clinicName: String = "",
    val veterinarianId: String = "",
    val veterinarianName: String = "",
    val occurredAt: String = "",
    val reason: String = "",
    val anamnesis: String = "",
    val vitalSigns: VitalSigns = VitalSigns(),
    val systemsExploration: SystemsExploration = SystemsExploration(),
    val problemList: List<String> = emptyList(),
    val masterProblemList: List<String> = emptyList(),
    val differentialDiagnoses: String = "",
    val definitiveDiagnosis: String = "",
    val diagnosticPlan: String = "",
    val treatmentPlan: String = "",
    val administeredMedications: List<PrescriptionRecord> = emptyList(),
    val prescribedMedications: List<PrescriptionRecord> = emptyList(),
    val observations: String = "",
    val studies: List<ClinicalStudyRecord> = emptyList(),
    val nextAppointmentDate: String? = null,
    val status: String = "COMPLETED"
)
