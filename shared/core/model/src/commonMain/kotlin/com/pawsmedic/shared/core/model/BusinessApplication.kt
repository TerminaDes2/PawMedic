package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estado del trámite de aprobación para registrar una nueva veterinaria.
 * Mapeado con el ENUM 'application_status' en Supabase.
 */
@Serializable
enum class ApplicationStatus {
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
    @SerialName("applicant_id") val applicantId: String,
    @SerialName("nombre_negocio") val nombreNegocio: String,
    @SerialName("domicilio") val domicilio: String,
    @SerialName("municipio") val municipio: String,
    @SerialName("estado") val estado: ApplicationStatus = ApplicationStatus.PENDIENTE,
    @SerialName("created_at") val createdAt: String? = null
)
