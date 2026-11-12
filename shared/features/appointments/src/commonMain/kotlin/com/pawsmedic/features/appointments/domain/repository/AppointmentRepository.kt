package com.pawsmedic.features.appointments.domain.repository

import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
import com.pawsmedic.features.appointments.domain.model.VeterinaryService

interface AppointmentRepository {
    suspend fun getAppointments(): Result<List<Appointment>>
    suspend fun getServices(tenantId: String): Result<List<VeterinaryService>>
    suspend fun getAvailableSlots(
        tenantId: String,
        serviceId: String,
        date: String
    ): Result<List<AvailableAppointmentSlot>>
    suspend fun requestAppointment(appointment: Appointment): Result<Appointment>
}
