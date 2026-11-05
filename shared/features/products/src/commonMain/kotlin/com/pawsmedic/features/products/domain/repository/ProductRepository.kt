package com.pawsmedic.features.products.domain.repository

import com.pawsmedic.features.products.domain.model.Product

interface ProductRepository {
    suspend fun getProductsByTenant(tenantId: String): List<Product>

    suspend fun addProduct(product: Product, tenantId: String): Product
}
