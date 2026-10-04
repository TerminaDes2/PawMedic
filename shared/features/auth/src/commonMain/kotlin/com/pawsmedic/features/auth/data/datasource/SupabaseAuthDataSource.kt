package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.RegisterParams

interface SupabaseAuthDataSource {
    suspend fun currentSession(): AuthSessionDto?
    suspend fun signIn(email: String, password: String): AuthSessionDto
    suspend fun signUp(params: RegisterParams): AuthSessionDto
    suspend fun signOut()
}
