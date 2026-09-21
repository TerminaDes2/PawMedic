package com.pawsmedic.features.products.data.repository

import com.pawsmedic.features.products.data.datasource.ProductDataSource
import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.repository.ProductRepository

class DefaultProductRepository(
    private val dataSource: ProductDataSource
) : ProductRepository {
    override suspend fun listProducts(businessId: String): Result<List<Product>> = try {
        Result.success(dataSource.listProducts(businessId).map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
