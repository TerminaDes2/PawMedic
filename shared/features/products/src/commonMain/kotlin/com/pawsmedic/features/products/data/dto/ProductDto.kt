package com.pawsmedic.features.products.data.dto

import com.pawsmedic.features.products.domain.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val businessId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stockQuantity: Int = 0
) {
    fun toDomain() = Product(id, businessId, name, description, price, stockQuantity)
}
