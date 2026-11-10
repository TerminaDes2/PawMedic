package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estado del trámite de aprobación para registrar una nueva veterinaria.
 * Mapeado con el ENUM 'application_status' en Supabase.
 */
@Serializable
enum class ApplicationStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("APPROVED")
    APPROVED,

    @SerialName("REJECTED")
    REJECTED,

    @SerialName("PENDIENTE")
    PENDIENTE,

    @SerialName("APROBADA")
    APROBADA,

    @SerialName("RECHAZADA")
    RECHAZADA
}

/**
 * Solicitud enviada por un usuario para dar de alta su negocio veterinario.
 * Mapeada desde la tabla 'business_applications' en Supabase.
 *
 * @property id UUID de la solicitud.
 * @property applicantId UUID del perfil del solicitante (FK -> profiles.id).
 * @property nombreNegocio Nombre propuesto para el negocio.
 * @property domicilio Dirección propuesta.
 * @property municipio Municipio o ciudad.
 * @property estado Estado de la revisión por parte del Administrador.
 * @property createdAt Fecha de envío de la solicitud.
 */
@Serializable
data class BusinessApplication(
    @SerialName("id") val id: String,
    @SerialName("applicant_id") val applicantId: String = "",
    @SerialName("business_name") val nombreNegocio: String = "",
    @SerialName("address") val domicilio: String = "",
    @SerialName("city") val municipio: String = "",
    @SerialName("status") val estado: ApplicationStatus = ApplicationStatus.PENDING,
    @SerialName("created_at") val createdAt: String? = null
)
