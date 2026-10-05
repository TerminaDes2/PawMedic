package com.pawsmedic.features.medical_records.data.datasource

import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto

class InMemoryMedicalRecordDataSource : MedicalRecordDataSource {
    private val records = listOf(
        MedicalRecordDto(
            id = "rec-1",
            petId = "pet-1",
            petName = "Tobías",
            clinicName = "Clinipet Central",
            doctorName = "Dr. Carlos Ruiz",
            date = "15 Oct 2023",
            title = "Vacunación Triple Felina",
            diagnosis = "Paciente felino sano. Control de vacunación anual al día.",
            treatment = "Aplicación de dosis Triple Felina (refuerzo).",
            vetNotes = "Programar control de desparasitación interna en tres meses."
        ),
        MedicalRecordDto(
            id = "rec-2",
            petId = "pet-1",
            petName = "Tobías",
            clinicName = "Clinipet Central",
            doctorName = "Dra. Ana Milena",
            date = "02 Sep 2023",
            title = "Control de Parásitos",
            diagnosis = "Parásitos gastrointestinales leves detectados en examen de rutina.",
            treatment = "Esquema oral antiparasitario (Febantel/Pirantel) en dos dosis.",
            vetNotes = "Dieta blanda por 48 horas debido a sensibilidad gástrica."
        ),
        MedicalRecordDto(
            id = "rec-3",
            petId = "pet-2",
            petName = "Joey",
            clinicName = "Hospital Vet Norte",
            doctorName = "Dr. Carlos Ruiz",
            date = "10 Ene 2024",
            title = "Revisión Odontológica",
            diagnosis = "Tártaro dental moderado en premolares.",
            treatment = "Profilaxis dental y limpieza especial.",
            vetNotes = "Usar juguetes masticables dentales."
        )
    )

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        return records.filter { it.petId == petId }
    }

    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        return records
    }
}
