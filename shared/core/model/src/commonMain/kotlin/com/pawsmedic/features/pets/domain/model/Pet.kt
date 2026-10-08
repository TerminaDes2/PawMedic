package com.pawsmedic.features.pets.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val allergies: String? = null,
    val photoUrl: String? = null,
    val medicalId: String? = null,
    val weight: Double? = null,
    val isInsured: Boolean = false
)
