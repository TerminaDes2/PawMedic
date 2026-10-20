package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

/**
 * Fuente de datos remota para Mascotas utilizando Supabase Postgrest (Solo Lectura).
 * Las inserciones y actualizaciones de mascotas han sido deshabilitadas en esta rama
 * ya que están siendo desarrolladas en una rama independiente por otro desarrollador.
 */
class SupabasePetDataSource(
    private val supabase: SupabaseClient
) : PetDataSource {

    override suspend fun listPets(ownerId: String): List<PetDto> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull() ?: return emptyList()
            val validOwnerId = if (ownerId.isNotBlank() && ownerId != "owner-1") ownerId else currentUser.id

            supabase.postgrest["pets"]
                .select {
                    filter {
                        eq("owner_id", validOwnerId)
                    }
                }
                .decodeList<PetDto>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getPetById(id: String): PetDto? {
        return try {
            supabase.postgrest["pets"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<PetDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun addPet(pet: PetDto): PetDto {
        // Deshabilitado: La inserción de mascotas se gestiona en otra rama.
        throw UnsupportedOperationException("El registro de mascotas se gestiona en otra rama.")
    }

    override suspend fun updatePet(pet: PetDto): PetDto {
        // Deshabilitado: La actualización de mascotas se gestiona en otra rama.
        throw UnsupportedOperationException("La actualización de mascotas se gestiona en otra rama.")
    }

    override suspend fun deletePet(id: String): Boolean {
        return false
    }
}
