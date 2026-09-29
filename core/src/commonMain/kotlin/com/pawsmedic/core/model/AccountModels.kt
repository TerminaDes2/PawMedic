package com.pawsmedic.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Roles disponibles en el sistema PawMedic.
 * Corresponden con la columna 'role' de la tabla 'profiles' en Supabase.
 */
@Serializable
enum class ProfileRole {
    @SerialName("USER")
    USER,

    @SerialName("VETERINARY_BUSINESS")
    VETERINARY_BUSINESS,

    @SerialName("SUPERADMIN")
    SUPERADMIN
}

/**
 * Modelo de sesión del usuario autenticado en la app.
 */
@Serializable
data class UserSession(
    val userId: String,
    val accessToken: String,
    val expiresAtEpochSeconds: Long
)

/**
 * Modelo del perfil de usuario registrado en Supabase ('profiles').
 */
@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("role") val role: ProfileRole = ProfileRole.USER,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("apellidos") val apellidos: String = "",
    @SerialName("num_tel") val numTel: String? = null
)

/**
 * Credenciales ingresadas en la pantalla de inicio de sesión.
 */
@Serializable
data class AuthCredentials(
    val email: String,
    val password: String
)
