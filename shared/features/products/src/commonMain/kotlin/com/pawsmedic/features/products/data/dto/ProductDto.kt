package com.pawsmedic.features.products.data.dto

import com.pawsmedic.features.products.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    @SerialName("tenant_id") val tenantId: String,
    val sku: String = "",
    val name: String,
    val description: String = "",
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    val active: Boolean = true
) {
    fun toDomain() = Product(
        id = id,
        tenantId = tenantId,
        name = name,
        description = description,
        priceCents = priceCents,
        sku = sku,
        stockQuantity = stockQuantity,
        active = active
    )

    companion object {
        fun fromDomain(product: Product, tenantId: String) = ProductInsertDto(
            tenantId = tenantId,
            sku = product.sku.ifBlank { "P-${product.name.hashCode().toUInt()}" },
            name = product.name,
            description = product.description.orEmpty(),
            priceCents = product.priceCents,
            stockQuantity = product.stockQuantity,
            active = product.active
        )
    }
}

@Serializable
data class ProductInsertDto(
    @SerialName("tenant_id") val tenantId: String,
    val sku: String,
    val name: String,
    val description: String = "",
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    val active: Boolean = true
)
