package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.data.dto.toDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : SupabaseAuthDataSource {

    override suspend fun currentSession(): AuthSessionDto? {
        val session = supabaseClient.auth.currentSessionOrNull()
        return session?.toDto()
    }

    override suspend fun signIn(email: String, password: String): AuthSessionDto {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("No se pudo obtener la sesión tras iniciar sesión")
        return session.toDto()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?
    ): AuthSessionDto {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("role", role.name)
                fullName?.let { put("full_name", it) }
            }
        }
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("No se pudo obtener la sesión tras el registro")
        return session.toDto()
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}