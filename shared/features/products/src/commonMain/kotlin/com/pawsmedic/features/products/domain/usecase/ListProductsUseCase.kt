package com.pawsmedic.features.products.domain.usecase

import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.repository.ProductRepository

class ListProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(tenantId: String): List<Product> =
        repository.getProductsByTenant(tenantId)
}
