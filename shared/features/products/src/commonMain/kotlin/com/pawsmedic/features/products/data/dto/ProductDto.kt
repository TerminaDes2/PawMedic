package com.pawsmedic.features.products.data.dto

import com.pawsmedic.features.products.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    @SerialName("tenant_id") val tenantId: String,
    val sku: String? = null,
    @SerialName("nombre") val name: String,
    @SerialName("descripcion") val description: String,
    @SerialName("precio_centavos") val priceCents: Int,
    @SerialName("cantidad") val stockQuantity: Int = 0,
    @SerialName("estado") val status: String = "ACTIVO",
    @SerialName("activo") val active: Boolean = true
) {
    fun toDomain() = Product(
        id = id,
        tenantId = tenantId,
        name = name,
        description = description,
        priceCents = priceCents,
        sku = sku.orEmpty(),
        stockQuantity = stockQuantity,
        active = active
    )

    companion object {
        fun fromDomain(product: Product, tenantId: String) = ProductDto(
            id = product.id,
            tenantId = tenantId,
            sku = product.sku.ifBlank { "P-${product.name.hashCode().toUInt()}" },
            name = product.name,
            description = product.description.orEmpty(),
            priceCents = product.priceCents,
            stockQuantity = product.stockQuantity,
            status = if (product.active) "ACTIVO" else "INACTIVO",
            active = product.active
        )
    }
}
