package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Horario de atención configurado por una sucursal veterinaria.
 * Mapeado desde la tabla 'business_schedules' en Supabase.
 *
 * @property id UUID del bloque de horario.
 * @property tenantId UUID de la sucursal (FK -> tenants.id).
 * @property diaSemana Día de la semana (1 = Lunes, 7 = Domingo).
 * @property horaApertura Hora de apertura en formato HH:mm:ss.
 * @property horaCierre Hora de cierre en formato HH:mm:ss.
 */
@Serializable
data class BusinessScheduleModel(
    @SerialName("id") val id: String? = null,
    @SerialName("tenant_id") val tenantId: String,
<<<<<<< HEAD
    @SerialName("weekday") val diaSemana: Int,
    @SerialName("opens_at") val horaApertura: String,
    @SerialName("closes_at") val horaCierre: String
=======
    @SerialName("day_of_week") val diaSemana: Int,
    @SerialName("open_time") val horaApertura: String,
    @SerialName("close_time") val horaCierre: String
>>>>>>> BapoGod
)
