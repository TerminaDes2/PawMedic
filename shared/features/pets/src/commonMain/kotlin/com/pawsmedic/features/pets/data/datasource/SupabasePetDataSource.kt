package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * Fuente de datos remota para Mascotas utilizando Supabase Postgrest.
 */
class SupabasePetDataSource(
    private val supabase: SupabaseClient
) : PetDataSource {

    override suspend fun listPets(ownerId: String): List<PetDto> {
        return supabase.postgrest["pets"]
            .select {
                filter {
                    if (ownerId.isNotBlank()) {
                        eq("owner_id", ownerId)
                    }
                }
            }
            .decodeList<PetDto>()
    }

    override suspend fun getPetById(id: String): PetDto? {
        return supabase.postgrest["pets"]
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<PetDto>()
    }

    override suspend fun addPet(pet: PetDto): PetDto {
        return supabase.postgrest["pets"]
            .insert(pet) {
                select()
            }
            .decodeSingle<PetDto>()
    }

    override suspend fun updatePet(pet: PetDto): PetDto {
        return supabase.postgrest["pets"]
            .update(pet) {
                filter {
                    eq("id", pet.id)
                }
                select()
            }
            .decodeSingle<PetDto>()
    }

    override suspend fun deletePet(id: String): Boolean {
        return try {
            supabase.postgrest["pets"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }
}