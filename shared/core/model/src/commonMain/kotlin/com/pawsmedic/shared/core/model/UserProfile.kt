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
 *
 * @property id Identificador único UUID generado por Supabase Auth (auth.users.id).
 * @property role Rol asignado en el sistema (USER, VETERINARY_BUSINESS, SUPERADMIN).
 * @property nombre Nombre del usuario.
 * @property apellidos Apellidos del usuario.
 * @property numTel Número telefónico de contacto (opcional).
 * @property createdAt Fecha de creación en formato ISO / Timestamp UTC.
 * @property updatedAt Fecha de última actualización en formato ISO / Timestamp UTC.
 */
@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("role") val role: ProfileRole = ProfileRole.USER,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("apellidos") val apellidos: String = "",
    @SerialName("num_tel") val numTel: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
