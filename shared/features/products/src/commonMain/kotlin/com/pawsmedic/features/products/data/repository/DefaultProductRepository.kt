package com.pawsmedic.features.products.data.repository

import com.pawsmedic.features.products.data.datasource.ProductDataSource
import com.pawsmedic.features.products.data.dto.ProductDto
import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.repository.ProductRepository

class DefaultProductRepository(
    private val dataSource: ProductDataSource
) : ProductRepository {
    override suspend fun getProductsByTenant(tenantId: String): List<Product> =
        dataSource.getProductsByTenant(tenantId).map(ProductDto::toDomain)

    override suspend fun addProduct(product: Product, tenantId: String): Product =
        dataSource.addProduct(ProductDto.fromDomain(product, tenantId)).toDomain()
}
