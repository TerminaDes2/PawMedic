package com.pawsmedic.features.auth.data.dto

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

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

fun UserSession.toDto(): AuthSessionDto {
    val userMetadata = user?.userMetadata
    val roleName = userMetadata?.get("role")?.jsonPrimitive?.content
    val parsedRole = roleName?.let { name ->
        runCatching { PawMedicRole.valueOf(name) }.getOrNull()
    } ?: PawMedicRole.USER

    return AuthSessionDto(
        accessToken = accessToken,
        userId = user?.id.orEmpty(),
        email = user?.email,
        role = parsedRole,
        tenantId = userMetadata?.get("tenant_id")?.jsonPrimitive?.content
    )
}