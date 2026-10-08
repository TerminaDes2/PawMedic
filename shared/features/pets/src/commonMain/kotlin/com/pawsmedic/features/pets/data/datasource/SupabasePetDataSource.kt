package com.pawsmedic.features.pets.data.datasource

import com.pawsmedic.features.pets.data.dto.PetDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

/**
 * Fuente de datos remota para Mascotas utilizando Supabase Postgrest.
 */
class SupabasePetDataSource(
    private val supabase: SupabaseClient
) : PetDataSource {

    override suspend fun listPets(ownerId: String): List<PetDto> {
        val activeOwnerId = if (ownerId.isBlank() || ownerId == "owner-1") {
            supabase.auth.currentUserOrNull()?.id ?: ownerId
        } else {
            ownerId
        }
        return supabase.postgrest["pets"]
            .select {
                filter {
                    if (activeOwnerId.isNotBlank() && activeOwnerId != "owner-1") {
                        eq("owner_id", activeOwnerId)
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
        val activeOwnerId = if (pet.ownerId.isBlank() || pet.ownerId == "owner-1") {
            supabase.auth.currentUserOrNull()?.id ?: pet.ownerId
        } else {
            pet.ownerId
        }
        val petToInsert = pet.copy(ownerId = activeOwnerId)
        return supabase.postgrest["pets"]
            .insert(petToInsert) {
                select()
            }
            .decodeSingle<PetDto>()
    }

    override suspend fun updatePet(pet: PetDto): PetDto {
        val activeOwnerId = if (pet.ownerId.isBlank() || pet.ownerId == "owner-1") {
            supabase.auth.currentUserOrNull()?.id ?: pet.ownerId
        } else {
            pet.ownerId
        }
        val petToUpdate = pet.copy(ownerId = activeOwnerId)
        return supabase.postgrest["pets"]
            .update(petToUpdate) {
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
