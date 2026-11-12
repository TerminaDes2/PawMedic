package com.pawsmedic.desktop.veterinary.model

import com.pawsmedic.shared.core.model.BusinessApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VeterinaryAccessStatusTest {
    private fun application(status: String, createdAt: String = "2026-01-01") =
        BusinessApplicationDto(status = status, createdAt = createdAt)

    @Test
    fun missingApplicationDoesNotGrantAccess() {
        assertNull(selectVeterinaryAccessStatus(emptyList(), emptyList()))
    }

    @Test
    fun pendingAndSuspendedStatesOverrideApproval() {
        assertEquals(
            "PENDING",
            selectVeterinaryAccessStatus(listOf(application("PENDING")), listOf(application("APPROVED")))?.status
        )
        assertEquals(
            "SUSPENDED",
            selectVeterinaryAccessStatus(listOf(application("APPROVED")), listOf(application("SUSPENDED")))?.status
        )
    }

    @Test
    fun latestRecordIsUsedWithinEachTable() {
        val applications = listOf(application("REJECTED"), application("APPROVED", "2026-02-01"))
        assertEquals("APPROVED", selectVeterinaryAccessStatus(applications, listOf(application("APPROVED")))?.status)
    }

    @Test
    fun unknownStatusDoesNotBecomeApproval() {
        assertEquals(
            "UNKNOWN",
            selectVeterinaryAccessStatus(listOf(application("UNKNOWN")), listOf(application("APPROVED")))?.status
        )
    }

    @Test
    fun applicationUsesEnglishDatabaseColumns() {
        val json = Json.encodeToString(
            BusinessApplication(
                id = "application",
                nombreNegocio = "Clinic",
                domicilio = "Address",
                municipio = "City",
                taxId = "Tax",
                contactPhone = "Phone",
                veterinarianName = "Veterinarian",
                professionalLicense = "License",
                contactEmail = "clinic@example.test"
            )
        )
        val fields = Json.parseToJsonElement(json).toString()
        listOf(
            "business_name", "address", "city", "tax_id", "contact_phone",
            "veterinarian_name", "professional_license", "contact_email"
        ).forEach { field -> kotlin.test.assertTrue(fields.contains("\"$field\"")) }
    }
}
