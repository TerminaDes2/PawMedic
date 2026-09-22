package com.pawsmedic.features.businesses.data.dto

import com.pawsmedic.features.businesses.domain.model.Business
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val description: String? = null
) {
    fun toDomain() = Business(id, name, city, description)
}
