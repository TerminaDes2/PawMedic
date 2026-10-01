package com.pawsmedic.features.services.data.datasource

import com.pawsmedic.shared.core.model.AvailabilityBlockModel
import com.pawsmedic.shared.core.model.BusinessScheduleModel
import com.pawsmedic.shared.core.model.ServiceModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * Fuente de datos remota para Servicios, Horarios y Bloqueos de Disponibilidad
 * usando Supabase Postgrest (Issues #50 y #51).
 */
class SupabaseServiceDataSource(
    private val supabase: SupabaseClient
) {
    /**
     * Obtiene los servicios ofrecidos por una sucursal específica (Tenant).
     * Lectura pública permitida por RLS.
     */
    suspend fun listServicesByTenant(tenantId: String): List<ServiceModel> {
        return supabase.postgrest["services"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                }
            }
            .decodeList<ServiceModel>()
    }

    /**
     * Obtiene los horarios de atención de una sucursal específica.
     */
    suspend fun listSchedulesByTenant(tenantId: String): List<BusinessScheduleModel> {
        return supabase.postgrest["business_schedules"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                }
            }
            .decodeList<BusinessScheduleModel>()
    }

    /**
     * Obtiene los bloqueos de agenda/disponibilidad de una sucursal.
     */
    suspend fun listAvailabilityBlocksByTenant(tenantId: String): List<AvailabilityBlockModel> {
        return supabase.postgrest["availability_blocks"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                }
            }
            .decodeList<AvailabilityBlockModel>()
    }

    /**
     * Crea un nuevo servicio para una sucursal.
     * RLS verificará que el usuario autenticado sea el dueño del tenant.
     */
    suspend fun insertService(service: ServiceModel): ServiceModel {
        return supabase.postgrest["services"]
            .insert(service) {
                select()
            }
            .decodeSingle<ServiceModel>()
    }
}
