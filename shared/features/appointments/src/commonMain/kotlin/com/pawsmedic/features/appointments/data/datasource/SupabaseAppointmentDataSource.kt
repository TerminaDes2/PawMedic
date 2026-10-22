package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.rpc

/**
 * Implementación de la fuente de datos de Citas utilizando el RPC 'book_appointment'
 * en Supabase para garantizar reservas atómicas sin doble reserva.
 */
class SupabaseAppointmentDataSource(
    private val supabase: SupabaseClient
) : AppointmentDataSource {

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        // Obtener el ID del usuario autenticado actual desde Supabase Auth
        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuario no autenticado. Inicie sesión para agendar una cita.")

        val ownerId = currentUser.id

        // Separar fecha y hora de requestedAt (ej. "2026-10-01 10:00:00")
        val parts = appointment.requestedAt.trim().split(" ")
        val fecha = parts.getOrNull(0) ?: "2026-10-01"
        val hora = parts.getOrNull(1) ?: "10:00:00"

        // Llamar a la función RPC atómica en PostgreSQL
        val generatedId = supabase.postgrest.rpc(
            "book_appointment",
            mapOf(
                "p_owner_id" to ownerId,
                "p_pet_id" to appointment.petId,
                "p_tenant_id" to appointment.businessId,
                "p_service_id" to appointment.serviceId,
                "p_fecha" to fecha,
                "p_hora" to hora
            )
        ).decodeAs<String>()

        return appointment.copy(id = generatedId)
    }
}
