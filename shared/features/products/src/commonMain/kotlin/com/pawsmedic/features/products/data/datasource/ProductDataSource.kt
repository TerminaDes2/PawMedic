package com.pawsmedic.features.products.data.datasource

import com.pawsmedic.features.products.data.dto.ProductDto

interface ProductDataSource {
    suspend fun getProductsByTenant(tenantId: String): List<ProductDto>

    suspend fun addProduct(product: ProductDto): ProductDto
}

class EmptyProductDataSource : ProductDataSource {
    override suspend fun getProductsByTenant(tenantId: String): List<ProductDto> = emptyList()

    override suspend fun addProduct(product: ProductDto): ProductDto = product
}
