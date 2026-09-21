package com.pawsmedic.features.auth.data.dto

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import kotlinx.serialization.Serializable

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val userId: String,
    val email: String? = null,
    val role: PawMedicRole = PawMedicRole.USER,
    val tenantId: String? = null
) {
    fun toDomain() = AuthenticatedSession(accessToken, userId, email, role, tenantId)
}
