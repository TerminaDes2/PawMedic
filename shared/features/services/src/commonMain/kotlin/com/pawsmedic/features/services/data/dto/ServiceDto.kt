package com.pawsmedic.features.services.data.dto

import com.pawsmedic.features.services.domain.model.Service
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    val id: String,
    val businessId: String,
    val name: String,
    val description: String? = null,
    val price: Double? = null
) {
    fun toDomain() = Service(id, businessId, name, description, price)
}
