package com.pawsmedic.features.businesses.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: String,
    val name: String,
    val city: String? = "Bogotá",
    val address: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val rating: Double = 4.9,
    val reviewCount: String = "120+ opiniones",
    val specialties: List<String> = emptyList(),
    val hours: List<String> = emptyList(),
    val imageUrl: String? = null
)
