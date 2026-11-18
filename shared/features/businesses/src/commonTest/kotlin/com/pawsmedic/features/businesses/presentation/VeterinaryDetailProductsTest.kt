package com.pawsmedic.features.businesses.presentation

import com.pawsmedic.features.businesses.data.datasource.PublicProduct
import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.repository.BusinessRepository
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VeterinaryDetailProductsTest {
    private val business = Business("clinic-1", "Clinic")
    private val repository = object : BusinessRepository {
        override suspend fun getBusinesses() = Result.success(listOf(business))
        override suspend fun getBusinessById(id: String) = Result.success(business)
        override suspend fun searchBusinesses(query: String) = Result.success(listOf(business))
    }

    @Test
    fun loadsProductsForTheSelectedClinic() = runTest {
        var requested = ""
        val product = PublicProduct("1", "Food", "Description", 1250, "")
        val vm = VeterinaryDetailViewModel(repository, business.id, { id ->
            requested = id
            listOf(product)
        }, backgroundScope)
        runCurrent()
        assertEquals(business.id, requested)
        assertEquals(listOf(product), vm.uiState.value.products)
        assertFalse(vm.uiState.value.productsError)
    }

    @Test
    fun reportsFailureAndAllowsRetry() = runTest {
        var fail = true
        val vm = VeterinaryDetailViewModel(repository, business.id, {
            if (fail) error("network error")
            emptyList()
        }, backgroundScope)
        runCurrent()
        assertTrue(vm.uiState.value.productsError)
        fail = false
        vm.refreshProducts()
        runCurrent()
        assertFalse(vm.uiState.value.productsError)
        assertFalse(vm.uiState.value.productsLoading)
    }
}
