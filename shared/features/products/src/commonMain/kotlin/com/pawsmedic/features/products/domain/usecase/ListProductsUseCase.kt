package com.pawsmedic.features.products.domain.usecase

import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.repository.ProductRepository

class ListProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(businessId: String): Result<List<Product>> =
        repository.listProducts(businessId)
}
