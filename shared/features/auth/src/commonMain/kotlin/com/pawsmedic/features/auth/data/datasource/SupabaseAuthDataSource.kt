package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole

interface SupabaseAuthDataSource {
    suspend fun currentSession(): AuthSessionDto?
    suspend fun signIn(email: String, password: String): AuthSessionDto
    suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String? = null
    ): AuthSessionDto
    suspend fun signOut()
}