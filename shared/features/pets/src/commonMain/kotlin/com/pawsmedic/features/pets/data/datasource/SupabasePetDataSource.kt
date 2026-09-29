package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.shared.core.model.PetModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * Fuente de datos remota para Mascotas utilizando Supabase Postgrest.
 * Todas las consultas respetan las políticas RLS activas en la tabla 'pets'.
 */
class SupabasePetDataSource(
    private val supabase: SupabaseClient
) {
    /**
     * Consulta las mascotas del usuario autenticado.
     * Gracias a RLS en la tabla 'pets', el filtro 'owner_id = ownerId' es validado
     * automáticamente por la sesión activa de Supabase Auth.
     */
    suspend fun listPetsByOwner(ownerId: String): List<PetModel> {
        return supabase.postgrest["pets"]
            .select {
                filter {
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<PetModel>()
    }

    /**
     * Registra una nueva mascota asociada al cliente autenticado.
     */
    suspend fun insertPet(pet: PetModel): PetModel {
        return supabase.postgrest["pets"]
            .insert(pet) {
                select()
            }
            .decodeSingle<PetModel>()
    }
}
