package com.pawsmedic.features.products.data.datasource

import com.pawsmedic.features.products.data.dto.ProductDto

interface ProductDataSource {
    suspend fun listProducts(businessId: String): List<ProductDto>
}

class EmptyProductDataSource : ProductDataSource {
    override suspend fun listProducts(businessId: String): List<ProductDto> = emptyList()
}
