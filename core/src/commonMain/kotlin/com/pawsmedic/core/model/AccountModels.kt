package com.pawsmedic.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProfileRole {
    USER,
    VETERINARY_BUSINESS,
    SUPERADMIN
}

@Serializable
data class UserSession(
    val userId: String,
    val accessToken: String,
    val expiresAtEpochSeconds: Long
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val role: ProfileRole
)

@Serializable
data class AuthCredentials(
    val email: String,
    val password: String
)
