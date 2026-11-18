package com.pawsmedic.features.products.data.datasource

import com.pawsmedic.features.products.data.dto.ProductDto
import com.pawsmedic.features.products.data.dto.ProductInsertDto

interface ProductDataSource {
    suspend fun getProductsByTenant(tenantId: String): List<ProductDto>

    suspend fun addProduct(product: ProductInsertDto): ProductDto
}

class EmptyProductDataSource : ProductDataSource {
    override suspend fun getProductsByTenant(tenantId: String): List<ProductDto> = emptyList()

    override suspend fun addProduct(product: ProductInsertDto): ProductDto =
        ProductDto(
            tenantId = product.tenantId,
            sku = product.sku,
            name = product.name,
            description = product.description,
            priceCents = product.priceCents,
            stockQuantity = product.stockQuantity,
            active = product.active
        )
}
