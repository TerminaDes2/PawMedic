package com.pawsmedic.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    val email: String,
    val password: String
)
