package com.pawsmedic.features.products.domain.model

data class Product(
    val id: String,
    val businessId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stockQuantity: Int = 0
)
