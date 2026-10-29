package com.pawsmedic.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterParams(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: PawMedicRole = PawMedicRole.USER
)