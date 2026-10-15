package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams

data class SignUpResultDto(
    val isVerified: Boolean,
    val session: AuthSessionDto? = null
)

interface SupabaseAuthDataSource {
    suspend fun currentSession(): AuthSessionDto?
    suspend fun signIn(email: String, password: String): AuthSessionDto
    suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String? = null,
        phone: String? = null
    ): SignUpResultDto
    suspend fun signUp(params: RegisterParams): SignUpResultDto = signUp(
        email = params.email,
        password = params.password,
        role = PawMedicRole.USER,
        fullName = params.fullName,
        phone = params.phone
    )
    suspend fun verifyEmailOtp(email: String, code: String): AuthSessionDto
    suspend fun resendEmailOtp(email: String)
    suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String? = null): AuthSessionDto
    suspend fun signOut()
}
