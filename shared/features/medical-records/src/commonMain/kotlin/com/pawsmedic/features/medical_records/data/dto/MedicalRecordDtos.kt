package com.pawsmedic.features.medical_records.data.dto

import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.SystemsExploration
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import com.pawsmedic.features.medical_records.domain.model.VitalSigns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class PetBackgroundDto(
    @SerialName("pet_id") val petId: String,
    @SerialName("last_deworming_date") val lastDewormingDate: String? = null,
    @SerialName("deworming_product") val dewormingProduct: String = "",
    @SerialName("diet") val diet: String = "",
    @SerialName("allergies") val allergies: String = "",
    @SerialName("past_diseases") val pastDiseases: String = "",
    @SerialName("past_surgeries") val pastSurgeries: String = "",
    @SerialName("family_history") val familyHistory: String = "",
    @SerialName("reproductive_status") val reproductiveStatus: String = "",
    @SerialName("habitat") val habitat: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null
) {
    fun toDomain() = PetBackground(
        petId = petId,
        lastDewormingDate = lastDewormingDate,
        dewormingProduct = dewormingProduct,
        diet = diet,
        allergies = allergies,
        pastDiseases = pastDiseases,
        pastSurgeries = pastSurgeries,
        familyHistory = familyHistory,
        reproductiveStatus = reproductiveStatus,
        habitat = habitat,
        updatedAt = updatedAt,
        updatedBy = updatedBy
    )

    companion object {
        fun fromDomain(domain: PetBackground) = PetBackgroundDto(
            petId = domain.petId,
            lastDewormingDate = domain.lastDewormingDate,
            dewormingProduct = domain.dewormingProduct,
            diet = domain.diet,
            allergies = domain.allergies,
            pastDiseases = domain.pastDiseases,
            pastSurgeries = domain.pastSurgeries,
            familyHistory = domain.familyHistory,
            reproductiveStatus = domain.reproductiveStatus,
            habitat = domain.habitat,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy
        )
    }
}

@Serializable
data class ClinicalConsultationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("idempotency_key") val idempotencyKey: String = "",
    @SerialName("pet_id") val petId: String,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("veterinarian_id") val veterinarianId: String = "",
    @SerialName("reason") val reason: String = "",
    @SerialName("anamnesis") val anamnesis: String = "",
    @SerialName("vital_signs") val vitalSigns: VitalSigns = VitalSigns(),
    @SerialName("systems_exploration") val systemsExploration: SystemsExploration = SystemsExploration(),
    @SerialName("problem_list") val problemList: List<String> = emptyList(),
    @SerialName("master_problem_list") val masterProblemList: List<String> = emptyList(),
    @SerialName("differential_diagnoses") val differentialDiagnoses: String = "",
    @SerialName("definitive_diagnosis") val definitiveDiagnosis: String = "",
    @SerialName("diagnostic_plan") val diagnosticPlan: String = "",
    @SerialName("treatment_plan") val treatmentPlan: String = "",
    @SerialName("observations") val observations: String = "",
    @SerialName("next_appointment_date") val nextAppointmentDate: String? = null,
    @SerialName("status") val status: String = "DRAFT",
    @SerialName("occurred_at") val occurredAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(
        vetName: String = "Dr. Veterinario",
        clinicName: String = "Clínica Veterinaria",
        prescriptions: List<PrescriptionRecord> = emptyList(),
        studies: List<ClinicalStudyRecord> = emptyList()
    ) = ClinicalConsultation(
        id = id ?: "",
        petId = petId,
        tenantId = tenantId,
        clinicName = clinicName,
        veterinarianId = veterinarianId,
        veterinarianName = vetName,
        occurredAt = occurredAt ?: createdAt ?: "",
        reason = reason,
        anamnesis = anamnesis,
        vitalSigns = vitalSigns,
        systemsExploration = systemsExploration,
        problemList = problemList,
        masterProblemList = masterProblemList,
        differentialDiagnoses = differentialDiagnoses,
        definitiveDiagnosis = definitiveDiagnosis,
        diagnosticPlan = diagnosticPlan,
        treatmentPlan = treatmentPlan,
        administeredMedications = prescriptions.filter { it.isAdministeredInConsultation },
        prescribedMedications = prescriptions.filter { !it.isAdministeredInConsultation },
        observations = observations,
        studies = studies,
        nextAppointmentDate = nextAppointmentDate,
        status = status
    )

    companion object {
        fun fromDomain(domain: ClinicalConsultation) = ClinicalConsultationDto(
            id = if (domain.id.isBlank()) null else domain.id,
            idempotencyKey = randomUuid(),
            petId = domain.petId,
            tenantId = domain.tenantId,
            veterinarianId = domain.veterinarianId,
            reason = domain.reason,
            anamnesis = domain.anamnesis,
            vitalSigns = domain.vitalSigns,
            systemsExploration = domain.systemsExploration,
            problemList = domain.problemList,
            masterProblemList = domain.masterProblemList,
            differentialDiagnoses = domain.differentialDiagnoses,
            definitiveDiagnosis = domain.definitiveDiagnosis,
            diagnosticPlan = domain.diagnosticPlan,
            treatmentPlan = domain.treatmentPlan,
            observations = domain.observations,
            nextAppointmentDate = domain.nextAppointmentDate,
            status = domain.status
        )

        private fun randomUuid(): String {
            val bytes = Random.nextBytes(16)
            bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
            bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
            val hex = bytes.joinToString("") { "%02x".format(it) }
            return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-" +
                "${hex.substring(16, 20)}-${hex.substring(20)}"
        }
    }
}

@Serializable
data class PrescriptionRecordDto(
    @SerialName("id") val id: String? = null,
    @SerialName("consultation_id") val consultationId: String? = null,
    @SerialName("pet_id") val petId: String,
    @SerialName("drug_name") val drugName: String,
    @SerialName("dosage") val dosage: String = "",
    @SerialName("unit") val unit: String = "mg",
    @SerialName("route") val route: String = "Oral",
    @SerialName("frequency") val frequency: String = "Cada 12h",
    @SerialName("duration") val duration: String = "7 días",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_administered_in_consultation") val isAdministeredInConsultation: Boolean = false,
    @SerialName("prescribing_vet_id") val prescribingVetId: String? = null,
    @SerialName("notes") val notes: String = "",
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(vetName: String = "Dr. Veterinario") = PrescriptionRecord(
        id = id ?: "",
        consultationId = consultationId,
        petId = petId,
        drugName = drugName,
        dosage = dosage,
        unit = unit,
        route = route,
        frequency = frequency,
        duration = duration,
        startDate = startDate,
        endDate = endDate,
        isAdministeredInConsultation = isAdministeredInConsultation,
        prescribingVetName = vetName,
        notes = notes
    )

    companion object {
        fun fromDomain(domain: PrescriptionRecord) = PrescriptionRecordDto(
            id = if (domain.id.isBlank()) null else domain.id,
            consultationId = domain.consultationId,
            petId = domain.petId,
            drugName = domain.drugName,
            dosage = domain.dosage,
            unit = domain.unit,
            route = domain.route,
            frequency = domain.frequency,
            duration = domain.duration,
            startDate = domain.startDate,
            endDate = domain.endDate,
            isAdministeredInConsultation = domain.isAdministeredInConsultation,
            notes = domain.notes
        )
    }
}

@Serializable
data class VaccinationRecordDto(
    @SerialName("id") val id: String? = null,
    @SerialName("pet_id") val petId: String,
    @SerialName("consultation_id") val consultationId: String? = null,
    @SerialName("vaccine_name") val vaccineName: String,
    @SerialName("batch_number") val batchNumber: String = "",
    @SerialName("applied_date") val appliedDate: String,
    @SerialName("next_due_date") val nextDueDate: String? = null,
    @SerialName("applied_by") val appliedBy: String? = null,
    @SerialName("notes") val notes: String = ""
) {
    fun toDomain(appliedByName: String = "Dr. Veterinario") = VaccinationRecord(
        id = id ?: "",
        petId = petId,
        consultationId = consultationId,
        vaccineName = vaccineName,
        batchNumber = batchNumber,
        appliedDate = appliedDate,
        nextDueDate = nextDueDate,
        appliedByName = appliedByName,
        notes = notes
    )

    companion object {
        fun fromDomain(domain: VaccinationRecord) = VaccinationRecordDto(
            id = if (domain.id.isBlank()) null else domain.id,
            petId = domain.petId,
            consultationId = domain.consultationId,
            vaccineName = domain.vaccineName,
            batchNumber = domain.batchNumber,
            appliedDate = domain.appliedDate,
            nextDueDate = domain.nextDueDate,
            notes = domain.notes
        )
    }
}

@Serializable
data class DewormingRecordDto(
    @SerialName("id") val id: String? = null,
    @SerialName("pet_id") val petId: String,
    @SerialName("consultation_id") val consultationId: String? = null,
    @SerialName("product_name") val productName: String,
    @SerialName("dosage") val dosage: String = "",
    @SerialName("applied_date") val appliedDate: String,
    @SerialName("next_due_date") val nextDueDate: String? = null,
    @SerialName("applied_by") val appliedBy: String? = null,
    @SerialName("notes") val notes: String = ""
) {
    fun toDomain(appliedByName: String = "Dr. Veterinario") = DewormingRecord(
        id = id ?: "",
        petId = petId,
        consultationId = consultationId,
        productName = productName,
        dosage = dosage,
        appliedDate = appliedDate,
        nextDueDate = nextDueDate,
        appliedByName = appliedByName,
        notes = notes
    )

    companion object {
        fun fromDomain(domain: DewormingRecord) = DewormingRecordDto(
            id = if (domain.id.isBlank()) null else domain.id,
            petId = domain.petId,
            consultationId = domain.consultationId,
            productName = domain.productName,
            dosage = domain.dosage,
            appliedDate = domain.appliedDate,
            nextDueDate = domain.nextDueDate,
            notes = domain.notes
        )
    }
}

@Serializable
data class ClinicalStudyRecordDto(
    @SerialName("id") val id: String? = null,
    @SerialName("consultation_id") val consultationId: String? = null,
    @SerialName("pet_id") val petId: String,
    @SerialName("study_type") val studyType: String = "LABORATORY",
    @SerialName("name") val name: String,
    @SerialName("status") val status: String = "COMPLETED",
    @SerialName("requested_at") val requestedAt: String = "",
    @SerialName("results_notes") val resultsNotes: String = "",
    @SerialName("attachment_urls") val attachmentUrls: List<String> = emptyList()
) {
    fun toDomain() = ClinicalStudyRecord(
        id = id ?: "",
        consultationId = consultationId,
        petId = petId,
        studyType = studyType,
        name = name,
        status = status,
        requestedAt = requestedAt,
        resultsNotes = resultsNotes,
        attachmentUrls = attachmentUrls
    )

    companion object {
        fun fromDomain(domain: ClinicalStudyRecord) = ClinicalStudyRecordDto(
            id = if (domain.id.isBlank()) null else domain.id,
            consultationId = domain.consultationId,
            petId = domain.petId,
            studyType = domain.studyType,
            name = domain.name,
            status = domain.status,
            requestedAt = domain.requestedAt,
            resultsNotes = domain.resultsNotes,
            attachmentUrls = domain.attachmentUrls
        )
    }
}

@Serializable
data class AuditLogEntryDto(
    @SerialName("id") val id: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
    @SerialName("action") val action: String = "",
    @SerialName("entity_type") val entityType: String = "",
    @SerialName("entity_id") val entityId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(actorName: String = "Sistema", actorRole: String = "VETERINARIO") = AuditLogEntry(
        id = id ?: "",
        actorId = actorId ?: "",
        actorName = actorName,
        actorRole = actorRole,
        timestamp = createdAt ?: "",
        entityType = entityType,
        entityId = entityId ?: "",
        action = action,
        details = "Operación $action ejecutada sobre $entityType"
    )
}
