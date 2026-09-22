package com.pawsmedic.features.services.domain.model

data class Service(
    val id: String,
    val businessId: String,
    val name: String,
    val description: String? = null,
    val price: Double? = null
)
