package com.pawsmedic.desktop.veterinary.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductRequirementsTest {
    private val categories = listOf(ProductCategory(1, "Alimentos"), ProductCategory(2, "Medicamentos"))

    @Test
    fun requiresAnExistingCategoryAndAnImage() {
        assertTrue(productRequirementsMet(1, categories, true))
        assertFalse(productRequirementsMet(null, categories, true))
        assertFalse(productRequirementsMet(3, categories, true))
        assertFalse(productRequirementsMet(1, categories, false))
        assertFalse(productRequirementsMet(1, emptyList(), true))
    }
}
