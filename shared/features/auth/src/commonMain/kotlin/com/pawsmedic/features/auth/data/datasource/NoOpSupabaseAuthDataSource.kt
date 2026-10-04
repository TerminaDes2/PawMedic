package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams

class NoOpSupabaseAuthDataSource : SupabaseAuthDataSource {
    override suspend fun currentSession(): AuthSessionDto? = null
    override suspend fun signIn(email: String, password: String): AuthSessionDto {
        return AuthSessionDto(
            accessToken = "mock-demo-token",
            userId = "user-123",
            email = email,
            role = PawMedicRole.USER
        )
    }
    override suspend fun signUp(params: RegisterParams): AuthSessionDto {
        return AuthSessionDto(
            accessToken = "mock-demo-token-reg",
            userId = "user-456",
            email = params.email,
            role = PawMedicRole.USER
        )
    }
    override suspend fun signOut() = Unit
}
