package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.SystemsExploration
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import com.pawsmedic.features.medical_records.domain.model.VitalSigns

class InMemoryMedicalRecordDataSource : MedicalRecordDataSource {

    private val legacyRecords = mutableListOf<MedicalRecordDto>()

    private val backgrounds = mutableMapOf(
        "pet-1" to PetBackground(
            petId = "pet-1",
            lastDewormingDate = "2023-09-02",
            dewormingProduct = "Endogard Felino 2.5mg",
            diet = "Croquetas Hills Science Diet Adulto + Alimento húmedo Royal Canin (1 sobrito diario)",
            allergies = "Polen de pino y derivados de pollo",
            pastDiseases = "Gastroenteritis aguda tratada en marzo 2022",
            pastSurgeries = "Orquiectomía / Castración (Junio 2021)",
            familyHistory = "Madre sin antecedentes reportados. Padre con otitis recurrente.",
            reproductiveStatus = "Esterilizado / Castrado",
            habitat = "Interior de departamento / Casa urbana sin acceso libre a la calle",
            updatedAt = "2023-10-15 11:30:00"
        ),
        "pet-2" to PetBackground(
            petId = "pet-2",
            lastDewormingDate = "2023-08-20",
            dewormingProduct = "Bravecto 500mg",
            diet = "Pro Plan Puppy Large Breed",
            allergies = "Ninguna conocida",
            pastDiseases = "Ninguna",
            pastSurgeries = "Ninguna",
            familyHistory = "Displasia de cadera en línea paterna",
            reproductiveStatus = "Intacto",
            habitat = "Jardín amplio y salidas diarias a parque",
            updatedAt = "2023-08-20 14:00:00"
        )
    )

    private val consultations = mutableListOf(
        ClinicalConsultation(
            id = "cons-101",
            petId = "pet-1",
            tenantId = "tenant-1",
            clinicName = "Clinipet Central",
            veterinarianId = "vet-001",
            veterinarianName = "Dra. Ana Milena",
            occurredAt = "2023-10-15 10:30",
            reason = "Revision anual, vacunación y leve estornudo ocasional",
            anamnesis = "El propietario reporta estornudos desde hace 3 días, consumo normal de alimento y agua, actitud activa.",
            vitalSigns = VitalSigns(
                temperatureCelsius = 38.4,
                heartRateBpm = 140,
                respiratoryRateRpm = 28,
                pulseQuality = "Fuerte y regular",
                weightGrams = 4200,
                tllcSeconds = "< 2 seg",
                hydrationStatus = "Normal (0-5%)",
                bodyConditionScore = "3/5 (Ideal)",
                attitude = "Alerta y activo",
                consciousnessState = "Consciente"
            ),
            systemsExploration = SystemsExploration(
                mucosas = "Rosadas y húmedas",
                ojos = "Permeables, sin secreción purulenta",
                oidos = "Limpios, sin eritroderma ni ácaros",
                nodulosLinfaticos = "Submandibulares palpables, tamaño normal",
                pielAnexos = "Pelaje brillante, sin ectopárásitos",
                locomocion = "Sin claudicación",
                musculoEsqueletico = "Sin dolor a la palpación",
                sistemaNervioso = "Reflejos oculares y espinales normales",
                sistemaCardiovascular = "Sin soplos ni arritmias",
                sistemaRespiratorio = "Campos pulmonares claros, leve rinitis serosa",
                sistemaDigestivo = "Abdomen blando, no doloroso",
                sistemaGenitourinario = "Vejiga normal, sin disuria"
            ),
            problemList = listOf("Estornudo ocasional", "Leve secreción nasal serosa"),
            masterProblemList = listOf("Rinitis serosa aguda leve"),
            differentialDiagnoses = "Rinotraqueítis Felina (FHV-1) leve vs Alergia ambiental a polvo/polen",
            definitiveDiagnosis = "Rinitis alérgica estacional leve",
            diagnosticPlan = "Observación clínica durante 7 días y control de alérgenos en el hogar.",
            treatmentPlan = "Nebulizaciones con solución salina estéril 2 veces al día + limpieza ocular y nasal.",
            administeredMedications = listOf(
                PrescriptionRecord(
                    id = "pres-01",
                    consultationId = "cons-101",
                    petId = "pet-1",
                    drugName = "Vacuna Triple Felina (Nobivac)",
                    dosage = "1.0",
                    unit = "mL",
                    route = "Subcutánea",
                    frequency = "Dosis única",
                    duration = "Dosis anual",
                    isAdministeredInConsultation = true,
                    prescribingVetName = "Dra. Ana Milena",
                    notes = "Aplicada en zona escapular izquierda"
                )
            ),
            prescribedMedications = listOf(
                PrescriptionRecord(
                    id = "pres-02",
                    consultationId = "cons-101",
                    petId = "pet-1",
                    drugName = "Antihistamínico Clorfeniramina",
                    dosage = "2.0",
                    unit = "mg",
                    route = "Oral",
                    frequency = "Cada 12 horas",
                    duration = "5 días",
                    isAdministeredInConsultation = false,
                    prescribingVetName = "Dra. Ana Milena",
                    notes = "Administrar junto con alimento húmedo por las mañanas y noches."
                )
            ),
            observations = "Evolución favorable proyectada. Citar si aparece letargia o anorexia.",
            studies = listOf(
                ClinicalStudyRecord(
                    id = "study-01",
                    consultationId = "cons-101",
                    petId = "pet-1",
                    studyType = "LABORATORY",
                    name = "Citología Nasal & Hemograma Completo",
                    status = "COMPLETED",
                    requestedAt = "2023-10-15",
                    resultsNotes = "Eosinófilos moderados compatibles con reacción alérgica. Leucocitos normales.",
                    attachmentUrls = listOf("https://pawmedic.app/studies/citologia_tobias_101.pdf")
                )
            ),
            nextAppointmentDate = "2024-10-15"
        )
    )

    private val auditLogs = mutableListOf(
        AuditLogEntry(
            id = "audit-1",
            actorId = "vet-001",
            actorName = "Dra. Ana Milena",
            actorRole = "VETERINARIO",
            timestamp = "2023-10-15 10:45:00",
            entityType = "clinical_consultations",
            entityId = "cons-101",
            action = "INSERT",
            details = "Registro de nueva consulta clínica con diagnóstico 'Rinitis alérgica estacional leve'."
        ),
        AuditLogEntry(
            id = "audit-2",
            actorId = "vet-001",
            actorName = "Dra. Ana Milena",
            actorRole = "VETERINARIO",
            timestamp = "2023-10-15 11:00:00",
            entityType = "pet_backgrounds",
            entityId = "pet-1",
            action = "UPDATE",
            details = "Actualización de antecedentes: vacunación y desparasitación al día."
        )
    )

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        return legacyRecords.filter { it.petId == petId }
    }

    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        return legacyRecords
    }

    override suspend fun getPetMedicalRecord(petId: String): PetMedicalRecord {
        val bg = backgrounds[petId] ?: PetBackground(petId = petId)
        val petConsultations = consultations.filter { it.petId == petId }
        val petVaccinations = petConsultations.flatMap { it.administeredMedications }
            .filter { it.drugName.contains("Vacuna", true) }
            .map {
                VaccinationRecord(
                    id = it.id,
                    petId = petId,
                    consultationId = it.consultationId,
                    vaccineName = it.drugName,
                    appliedDate = it.startDate ?: "2023-10-15",
                    appliedByName = it.prescribingVetName
                )
            }

        val petDewormings = listOf(
            DewormingRecord(
                id = "dew-1",
                petId = petId,
                productName = bg.dewormingProduct.ifBlank { "Endogard Felino" },
                dosage = "1 tableta",
                appliedDate = bg.lastDewormingDate ?: "2023-09-02",
                appliedByName = "Dra. Ana Milena"
            )
        )

        val petPrescriptions = petConsultations.flatMap { it.administeredMedications + it.prescribedMedications }
        val petStudies = petConsultations.flatMap { it.studies }
        val petAudit = auditLogs.filter { it.entityId == petId || petConsultations.any { c -> c.id == it.entityId } }

        return PetMedicalRecord(
            recordNumber = "EXP-${petId.take(8).uppercase()}",
            petId = petId,
            petName = if (petId == "pet-1") "Tobías" else "Mascota",
            petSpecies = if (petId == "pet-1") "Gato" else "Perro",
            petBreed = if (petId == "pet-1") "Persa Mestizo" else "Mestizo",
            petSex = "Macho",
            petDateOfBirth = "2021-05-10",
            petAgeCalculated = "2 años 5 meses",
            petColor = "Blanco con Gris",
            petWeightGrams = 4200,
            petSpecialMarks = "Mancha gris en oreja izquierda",
            petReproductiveStatus = bg.reproductiveStatus.ifBlank { "Castrado" },
            ownerName = "María Fernanda López",
            ownerEmail = "maria.lopez@example.com",
            ownerPhone = "55-9831-2983",
            createdAt = "2023-01-10",
            background = bg,
            consultations = petConsultations,
            vaccinations = petVaccinations,
            dewormings = petDewormings,
            prescriptions = petPrescriptions,
            studies = petStudies,
            auditLogs = petAudit
        )
    }

    override suspend fun saveConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord>,
        studies: List<ClinicalStudyRecord>
    ): ClinicalConsultation {
        val newId = "cons-${consultations.size + 101}"
        val finalConsultation = consultation.copy(
            id = newId,
            administeredMedications = prescriptions.filter { it.isAdministeredInConsultation },
            prescribedMedications = prescriptions.filter { !it.isAdministeredInConsultation },
            studies = studies
        )
        consultations.add(0, finalConsultation)

        auditLogs.add(
            0,
            AuditLogEntry(
                id = "audit-${auditLogs.size + 1}",
                actorId = consultation.veterinarianId.ifBlank { "vet-001" },
                actorName = consultation.veterinarianName.ifBlank { "Dr. Veterinario" },
                actorRole = "VETERINARIO",
                timestamp = consultation.occurredAt.ifBlank { "Ahora" },
                entityType = "clinical_consultations",
                entityId = newId,
                action = "INSERT",
                details = "Registro de nueva consulta médica con motivo: ${consultation.reason}"
            )
        )

        return finalConsultation
    }

    override suspend fun updateBackground(background: PetBackground): PetBackground {
        backgrounds[background.petId] = background
        auditLogs.add(
            0,
            AuditLogEntry(
                id = "audit-${auditLogs.size + 1}",
                actorId = background.updatedBy ?: "vet-001",
                actorName = "Dr. Veterinario",
                actorRole = "VETERINARIO",
                timestamp = "Ahora",
                entityType = "pet_backgrounds",
                entityId = background.petId,
                action = "UPDATE",
                details = "Actualización de antecedentes médicos y condiciones de vida."
            )
        )
        return background
    }

    override suspend fun addVaccination(vaccination: VaccinationRecord): VaccinationRecord {
        val newRecord = vaccination.copy(id = "vac-${System.currentTimeMillis()}")
        return newRecord
    }

    override suspend fun addDeworming(deworming: DewormingRecord): DewormingRecord {
        val newRecord = deworming.copy(id = "dew-${System.currentTimeMillis()}")
        val currentBg = backgrounds[deworming.petId] ?: PetBackground(petId = deworming.petId)
        backgrounds[deworming.petId] = currentBg.copy(
            lastDewormingDate = newRecord.appliedDate,
            dewormingProduct = newRecord.productName
        )
        return newRecord
    }

    override suspend fun addPrescription(prescription: PrescriptionRecord): PrescriptionRecord {
        return prescription.copy(id = "pres-${System.currentTimeMillis()}")
    }

    override suspend fun addClinicalStudy(study: ClinicalStudyRecord): ClinicalStudyRecord {
        return study.copy(id = "std-${System.currentTimeMillis()}")
    }

    override suspend fun getAuditLogs(petId: String): List<AuditLogEntry> {
        return auditLogs.filter { it.entityId == petId || consultations.any { c -> c.id == it.entityId && c.petId == petId } }
    }
}
