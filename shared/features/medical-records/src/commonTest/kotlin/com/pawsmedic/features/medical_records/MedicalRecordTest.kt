package com.pawsmedic.features.medical_records

import com.pawsmedic.features.medical_records.data.datasource.InMemoryMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.presentation.MedicalRecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MedicalRecordTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetPetMedicalRecordReturnsValidData() = runTest {
        val dataSource = InMemoryMedicalRecordDataSource()
        val repository = DefaultMedicalRecordRepository(dataSource)

        val result = repository.getPetMedicalRecord("pet-1")

        assertTrue(result.isSuccess)
        val record = result.getOrNull()
        assertNotNull(record)
        assertEquals("pet-1", record.petId)
        assertNotNull(record.petName)
        assertTrue(record.consultations.isNotEmpty())
    }

    @Test
    fun testPetOwnerCannotOpenNewConsultation() = runTest {
        val dataSource = InMemoryMedicalRecordDataSource()
        val repository = DefaultMedicalRecordRepository(dataSource)
        val viewModel = MedicalRecordViewModel(
            repository = repository,
            petId = "pet-1",
            userRole = "USER", // Pet Owner role
            userName = "María López"
        )

        viewModel.openNewConsultationDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showNewConsultationDialog)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Acceso denegado"))
    }

    @Test
    fun testVeterinarianCanOpenAndSubmitConsultation() = runTest {
        val dataSource = InMemoryMedicalRecordDataSource()
        val repository = DefaultMedicalRecordRepository(dataSource)
        val viewModel = MedicalRecordViewModel(
            repository = repository,
            petId = "pet-1",
            userRole = "VETERINARY_BUSINESS", // Veterinarian role
            userName = "Dra. Ana Milena"
        )

        viewModel.openNewConsultationDialog()
        assertTrue(viewModel.uiState.value.showNewConsultationDialog)

        val newConsultation = ClinicalConsultation(
            petId = "pet-1",
            reason = "Consulta de prueba",
            anamnesis = "Mascota sana",
            definitiveDiagnosis = "Estado de salud óptimo"
        )

        val prescriptions = listOf(
            PrescriptionRecord(
                petId = "pet-1",
                drugName = "Antiparasitario oral",
                dosage = "1 comprimido",
                route = "Oral",
                frequency = "Dosis única"
            )
        )

        viewModel.submitNewConsultation(newConsultation, prescriptions, emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedRecordResult = repository.getPetMedicalRecord("pet-1")
        val updatedRecord = updatedRecordResult.getOrNull()

        assertNotNull(updatedRecord)
        assertTrue(updatedRecord.consultations.any { it.reason == "Consulta de prueba" })
    }

    @Test
    fun testUpdateBackgroundAsVet() = runTest {
        val dataSource = InMemoryMedicalRecordDataSource()
        val repository = DefaultMedicalRecordRepository(dataSource)
        val viewModel = MedicalRecordViewModel(
            repository = repository,
            petId = "pet-1",
            userRole = "VETERINARY_BUSINESS",
            userName = "Dra. Ana Milena"
        )

        val newBackground = PetBackground(
            petId = "pet-1",
            diet = "Dieta prescrita Renal K",
            allergies = "Mariscos y polen"
        )

        viewModel.updateBackground(newBackground)
        testDispatcher.scheduler.advanceUntilIdle()

        val recordResult = repository.getPetMedicalRecord("pet-1")
        val bg = recordResult.getOrNull()?.background

        assertNotNull(bg)
        assertEquals("Dieta prescrita Renal K", bg.diet)
        assertEquals("Mariscos y polen", bg.allergies)
    }
}
