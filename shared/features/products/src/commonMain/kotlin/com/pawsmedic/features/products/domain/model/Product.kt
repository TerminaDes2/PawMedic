package com.pawsmedic.features.products.domain.model

data class Product(
    val id: Int? = null,
    val tenantId: String,
    val name: String,
    val description: String? = null,
    val priceCents: Int,
    val sku: String = "",
    val stockQuantity: Int = 0,
    val active: Boolean = true
)

fun Product.formattedPrice(): String {
    val dollars = priceCents / 100
    val cents = (priceCents % 100).toString().padStart(2, '0')
    return "\$$dollars.$cents"
}
