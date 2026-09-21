package com.pawsmedic.features.products.domain.repository

import com.pawsmedic.features.products.domain.model.Product

interface ProductRepository {
    suspend fun listProducts(businessId: String): Result<List<Product>>
}
