package com.pawsmedic.shared.core.designsystem.language

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {
    @Test
    fun preferenceDefaultsToSpanishAndRestoresSelections() {
        assertEquals(AppLanguage.SPANISH, languageFromPreference(null))
        assertEquals(AppLanguage.SPANISH, languageFromPreference("invalid"))
        AppLanguage.entries.forEach { assertEquals(it, languageFromPreference(it.name)) }
    }

    @Test
    fun textUsesSelectedLanguage() {
        assertEquals("Hola", AppLanguage.SPANISH.text("Hola", "Hello"))
        assertEquals("Hello", AppLanguage.ENGLISH.text("Hola", "Hello"))
    }

    @Test
    fun labelsTranslateOnlyKnownValues() {
        assertEquals("Pending confirmation", appointmentStatusLabel("Pendiente de confirmación", AppLanguage.ENGLISH))
        assertEquals("Cancelada", appointmentStatusLabel("Cancelada", AppLanguage.SPANISH))
        assertEquals("Custom status", appointmentStatusLabel("Custom status", AppLanguage.ENGLISH))
        assertEquals("Dog", petCatalogLabel("Canino", AppLanguage.ENGLISH))
        assertEquals("Female", petCatalogLabel("Hembra", AppLanguage.ENGLISH))
        assertEquals("Pastor Alemán", petCatalogLabel("Pastor Alemán", AppLanguage.SPANISH))
        assertEquals("Custom breed", petCatalogLabel("Custom breed", AppLanguage.ENGLISH))
        assertEquals("1 year", petAgeLabel("1 Años", AppLanguage.ENGLISH))
        assertEquals("2 months", petAgeLabel("2 Meses", AppLanguage.ENGLISH))
        assertEquals("2 years 1 month", petAgeLabel("2 años 1 mes", AppLanguage.ENGLISH))
        assertEquals("Not specified", petAgeLabel("No especificada", AppLanguage.ENGLISH))
        assertEquals("2 Años", petAgeLabel("2 Años", AppLanguage.SPANISH))
        assertEquals("unknown age", petAgeLabel("unknown age", AppLanguage.ENGLISH))
        assertEquals("Vaccination", serviceCategoryLabel("vacunacion", AppLanguage.ENGLISH))
        assertEquals("Vacunación", serviceCategoryLabel("vacunacion", AppLanguage.SPANISH))
        assertEquals("Servicio propio", serviceCategoryLabel("Servicio propio", AppLanguage.ENGLISH))
    }
}
