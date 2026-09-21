package com.pawsmedic.features.businesses.domain.model

data class Business(
    val id: String,
    val name: String,
    val city: String? = null,
    val description: String? = null
)
