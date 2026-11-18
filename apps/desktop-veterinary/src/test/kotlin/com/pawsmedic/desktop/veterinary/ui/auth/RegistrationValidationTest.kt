package com.pawsmedic.desktop.veterinary.ui.auth

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RegistrationValidationTest {
    @Test
    fun namesRequireLengthUppercaseAndCleanText() {
        assertNull(validateBusinessOrPersonName("Veterinaria San Miguel"))
        assertNotNull(validateBusinessOrPersonName("Vet"))
        assertNotNull(validateBusinessOrPersonName("veterinaria san miguel"))
        assertNotNull(validateBusinessOrPersonName("Veterinaria Pu7a"))
        assertNotNull(validateBusinessOrPersonName("Clínica Pendejo"))
        assertNull(validateBusinessOrPersonName("Clínica Computadora"))
    }

    @Test
    fun rfcAcceptsOnlyTwelveOrThirteenUppercaseAlphanumerics() {
        assertNull(validateRfc("ABC123456XY1"))
        assertNull(validateRfc("ABCD123456XY1"))
        assertNotNull(validateRfc("ABC123456XY"))
        assertNotNull(validateRfc("ABCD123456XY12"))
        assertNotNull(validateRfc("abc123456xy1"))
        assertNotNull(validateRfc("ABC 123456XY1"))
        assertNotNull(validateRfc("ABC-123456XY1"))
    }

    @Test
    fun addressAllowsLettersDigitsCommasAndHash() {
        assertNull(validateClinicAddress("Av. Reforma".replace(".", "") + " #123, Centro"))
        assertNotNull(validateClinicAddress("A 1"))
        assertNotNull(validateClinicAddress("Calle 5 @ norte"))
        assertNotNull(validateClinicAddress("Av. Reforma 12"))
    }

    @Test
    fun licenseAcceptsSevenOrEightDigits() {
        assertNull(validateProfessionalLicense("1234567"))
        assertNull(validateProfessionalLicense("12345678"))
        assertNotNull(validateProfessionalLicense("123456"))
        assertNotNull(validateProfessionalLicense("123456789"))
        assertNotNull(validateProfessionalLicense("12345A7"))
        assertNotNull(validateProfessionalLicense("1234 567"))
    }
}
