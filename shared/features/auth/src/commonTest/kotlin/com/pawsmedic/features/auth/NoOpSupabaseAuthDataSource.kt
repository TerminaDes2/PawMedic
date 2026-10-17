package com.pawsmedic.features.auth

import com.pawsmedic.features.auth.data.datasource.SignUpResultDto
import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSource
import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams

class NoOpSupabaseAuthDataSource : SupabaseAuthDataSource {
    override suspend fun currentSession(): AuthSessionDto? = null

    override suspend fun signIn(email: String, password: String): AuthSessionDto =
        error("Authentication is not available in this test data source")

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?,
        phone: String?
    ): SignUpResultDto = error("Authentication is not available in this test data source")

    override suspend fun signUp(params: RegisterParams): SignUpResultDto =
        error("Authentication is not available in this test data source")

    override suspend fun verifyEmailOtp(email: String, code: String): AuthSessionDto =
        error("Authentication is not available in this test data source")

    override suspend fun resendEmailOtp(email: String) {
        error("Authentication is not available in this test data source")
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String?): AuthSessionDto =
        error("Authentication is not available in this test data source")

    override suspend fun signOut() {
        error("Authentication is not available in this test data source")
    }
}
