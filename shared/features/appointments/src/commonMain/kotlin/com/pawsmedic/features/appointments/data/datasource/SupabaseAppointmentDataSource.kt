package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class SupabaseAppointmentDataSource(
    private val supabase: SupabaseClient
) : AppointmentDataSource {

    override suspend fun getAppointments(): List<AppointmentDto> {
        return try {
            supabase.postgrest["appointments"].select().decodeList<AppointmentDto>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuario no autenticado. Inicie sesión para agendar una cita.")

        val generatedId = supabase.postgrest.rpc(
            "book_appointment",
            mapOf(
                "p_owner_id" to currentUser.id,
                "p_pet_id" to appointment.petId,
                "p_tenant_id" to appointment.businessId,
                "p_service_id" to appointment.serviceId,
                "p_fecha" to appointment.date, 
                "p_hora" to appointment.time   
            )
        ).decodeAs<String>()

        return appointment.copy(id = generatedId)
    }
}
