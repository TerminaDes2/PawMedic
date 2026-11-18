package com.pawsmedic.features.products.data.dto

import com.pawsmedic.features.products.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    @SerialName("tenant_id") val tenantId: String,
    val name: String,
    @SerialName("type_id") val typeId: Int? = null,
    val description: String? = null,
    @SerialName("price_cents") val priceCents: Int,
    val sku: String? = null,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null,
    val active: Boolean = true,
    val published: Boolean = true,
    @SerialName("product_types") val productType: ProductTypeDto? = null
) {
    fun toDomain() = Product(
        id = id,
        tenantId = tenantId,
        name = name,
        typeId = typeId,
        typeName = productType?.name,
        description = description,
        priceCents = priceCents,
        sku = sku.orEmpty(),
        stockQuantity = stockQuantity,
        imageUrl = imageUrl,
        active = active,
        published = published
    )

    companion object {
        fun fromDomain(product: Product, tenantId: String) = ProductDto(
            id = product.id,
            tenantId = tenantId,
            name = product.name,
            typeId = product.typeId,
            sku = product.sku.ifBlank { "P-${product.name.hashCode().toUInt()}" },
            description = product.description,
            priceCents = product.priceCents,
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            active = product.active,
            published = product.published
        )
    }
}

@Serializable
data class ProductTypeDto(
    val id: Int? = null,
    val name: String
)
