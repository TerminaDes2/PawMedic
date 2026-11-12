package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Servicio ofrecido por un negocio/sucursal veterinaria (Tenant).
 * Mapeado desde la tabla 'services' en Supabase PostgreSQL.
 *
 * @property id UUID del servicio.
 * @property tenantId UUID de la sucursal que oferta el servicio (FK -> tenants.id).
 * @property nombre Nombre comercial del servicio (ej. 'Consulta general', 'Vacunación').
 * @property descripcion Detalle o alcance del servicio.
 * @property precioCentavos Precio del servicio expresado en centavos (ej. 50000 = $500.00).
 * @property duracionMinutos Duración estimada en minutos.
 * @property createdAt Fecha de creación del registro.
 */
@Serializable
data class ServiceModel(
    @SerialName("id") val id: String? = null,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("name") val nombre: String,
    @SerialName("description") val descripcion: String = "",
    @SerialName("price_cents") val precioCentavos: Int,
    @SerialName("duration_minutes") val duracionMinutos: Int = 30,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
