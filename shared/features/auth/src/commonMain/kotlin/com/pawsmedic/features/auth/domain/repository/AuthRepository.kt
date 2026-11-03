package com.pawsmedic.features.auth.domain.repository

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.LoginParams
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams

data class SignUpResult(
    val isVerified: Boolean,
    val session: AuthenticatedSession? = null
)

interface AuthRepository {
    suspend fun restoreSession(): AuthenticatedSession?

    suspend fun signIn(
        email: String,
        password: String,
        requiredRole: PawMedicRole? = null
    ): AuthenticatedSession

    suspend fun signIn(params: LoginParams): AuthenticatedSession = signIn(params.email, params.password)

    suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole = PawMedicRole.USER,
        fullName: String? = null,
        phone: String? = null
    ): SignUpResult

    suspend fun signUp(params: RegisterParams): SignUpResult

    suspend fun verifyEmailOtp(email: String, code: String): AuthenticatedSession

    suspend fun resendEmailOtp(email: String)

    suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String? = null): AuthenticatedSession

    suspend fun signOut()
}
