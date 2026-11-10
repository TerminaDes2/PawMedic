package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para las Sucursales / Negocios veterinarios (Multi-tenancy).
 * Mapeado directamente desde la tabla 'tenants' en Supabase PostgreSQL.
 *
 * @property id UUID único del tenant / negocio.
 * @property ownerId UUID del usuario dueño del negocio (FK -> profiles.id).
 * @property nombre Nombre comercial de la veterinaria / sucursal.
 * @property domicilio Dirección física de la sucursal.
 * @property municipio Municipio o ciudad donde opera.
 * @property numeroTel Teléfono de atención del negocio.
 * @property estadoOperativo Estado del negocio (ej. 'ACTIVO', 'INACTIVO').
 * @property createdAt Fecha de registro de la sucursal.
 */
@Serializable
data class Tenant(
    @SerialName("id") val id: String,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("name") val nombre: String = "",
    @SerialName("address") val domicilio: String = "",
    @SerialName("city") val municipio: String = "",
    @SerialName("phone") val numeroTel: String = "",
    @SerialName("operational_status") val estadoOperativo: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String? = null
)
