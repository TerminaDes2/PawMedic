package com.pawsmedic.features.businesses.data.dto

import com.pawsmedic.features.businesses.domain.model.Business
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDto(
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
) {
    fun toDomain() = Business(
        id = id,
        name = name,
        city = city,
        address = address,
        phone = phone,
        description = description,
        rating = rating,
        reviewCount = reviewCount,
        specialties = specialties,
        hours = hours,
        imageUrl = imageUrl
    )
}
