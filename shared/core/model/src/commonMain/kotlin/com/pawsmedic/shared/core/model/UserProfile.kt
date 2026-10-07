package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProfileRole {
    @SerialName("USER")
    USER,

    @SerialName("VETERINARY_BUSINESS")
    VETERINARY_BUSINESS,

    @SerialName("SUPERADMIN")
    SUPERADMIN
}

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("role") val role: ProfileRole = ProfileRole.USER,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("apellidos") val apellidos: String? = null,
    @SerialName("num_tel") val numTel: String? = null,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    /**
     * Retorna el nombre completo o "Usuario" si los campos están vacíos o contienen "EMPTY".
     */
    val displayName: String
        get() {
            val full = listOfNotNull(nombre, apellidos)
                .filter { it.isNotBlank() && it != "EMPTY" }
                .joinToString(" ")
            return full.ifBlank { "Usuario" }
        }
}