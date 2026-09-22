package com.pawsmedic.features.appointments.domain.repository

import com.pawsmedic.features.appointments.domain.model.Appointment

interface AppointmentRepository {
    suspend fun requestAppointment(appointment: Appointment): Result<Appointment>
}
