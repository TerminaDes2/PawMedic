package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole

class NoOpSupabaseAuthDataSource : SupabaseAuthDataSource {
    override suspend fun currentSession(): AuthSessionDto? = null

    override suspend fun signIn(email: String, password: String): AuthSessionDto {
        error("Supabase Auth is not configured. Provide deployment configuration; never add secrets to source.")
    }

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?
    ): AuthSessionDto {
        error("Supabase Auth is not configured. Provide deployment configuration; never add secrets to source.")
    }

    override suspend fun signOut() = Unit
}