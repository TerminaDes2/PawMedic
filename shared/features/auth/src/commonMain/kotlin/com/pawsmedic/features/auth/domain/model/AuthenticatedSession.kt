package com.pawsmedic.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PawMedicRole {
    USER,
    VETERINARY_BUSINESS,
    SUPERADMIN
}

@Serializable
data class AuthenticatedSession(
    val accessToken: String,
    val userId: String,
    val email: String?,
    val role: PawMedicRole,
    val tenantId: String? = null
)
