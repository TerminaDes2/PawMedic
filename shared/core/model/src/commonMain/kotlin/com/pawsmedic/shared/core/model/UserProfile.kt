package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rol del usuario en la plataforma PawMedic.
 * Coincide con el tipo ENUM 'profile_role' definido en la base de datos de Supabase.
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
 * Modelo de datos del perfil de usuario (Mapeado desde la tabla 'profiles' en Supabase).
 */
@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("role") val role: ProfileRole = ProfileRole.USER,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("apellidos") val apellidos: String = "",
    @SerialName("num_tel") val numTel: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
