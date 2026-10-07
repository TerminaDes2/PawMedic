package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Implementación de la fuente de datos de Citas para Supabase.
 */
class SupabaseAppointmentDataSource(
    private val supabase: SupabaseClient
) : AppointmentDataSource {
    override suspend fun getAppointments(): List<AppointmentDto> {
        return emptyList()
    }

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        // Obtener el ID del usuario autenticado actual desde Supabase Auth
        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuario no autenticado. Inicie sesión para agendar una cita.")

        val ownerId = currentUser.id

        // Construir los parámetros en formato JSON serializable
        val params = buildJsonObject {
            put("p_owner_id", ownerId)
            put("p_pet_id", appointment.petId)
            put("p_tenant_id", appointment.businessId)
            put("p_service_id", appointment.serviceId)
            put("p_fecha", appointment.date)
            put("p_hora", appointment.time)
        }

        // Llamar a la función RPC atómica en PostgreSQL
        val generatedId = supabase.postgrest.rpc(
            function = "book_appointment",
            parameters = params
        ).decodeAs<String>()

        return appointment.copy(id = generatedId)
    }
}