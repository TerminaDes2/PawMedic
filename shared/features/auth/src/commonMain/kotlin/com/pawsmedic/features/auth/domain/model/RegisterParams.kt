package com.pawsmedic.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterParams(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)
