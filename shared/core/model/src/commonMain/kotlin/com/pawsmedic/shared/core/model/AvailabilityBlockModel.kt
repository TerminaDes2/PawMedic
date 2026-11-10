package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bloqueo de agenda / disponibilidad configurado por una sucursal veterinaria.
 * Mapeado desde la tabla 'availability_blocks' en Supabase (Issue #50 / #51).
 *
 * @property id UUID del bloqueo.
 * @property tenantId UUID de la sucursal (FK -> tenants.id).
 * @property motivo Razón del bloqueo (ej. 'Mantenimiento', 'Día festivo').
 * @property fechaInicio Inicio del bloqueo en formato ISO Timestamp.
 * @property fechaFin Fin del bloqueo en formato ISO Timestamp.
 * @property createdAt Fecha de creación del registro.
 */
@Serializable
data class AvailabilityBlockModel(
    @SerialName("id") val id: String? = null,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("reason") val motivo: String = "",
    @SerialName("starts_at") val fechaInicio: String,
    @SerialName("ends_at") val fechaFin: String,
    @SerialName("created_at") val createdAt: String? = null
)
