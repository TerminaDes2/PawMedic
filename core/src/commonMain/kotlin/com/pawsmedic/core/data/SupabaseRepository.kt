package com.pawsmedic.core.data

import com.pawsmedic.core.model.AuthCredentials
import com.pawsmedic.core.model.ProfileRole
import com.pawsmedic.core.model.UserProfile
import com.pawsmedic.core.model.UserSession

/**
 * Configuration is intentionally supplied by the host application. This
 * adapter is the boundary for a Supabase Auth/REST client and remains usable
 * in previews and tests without a network or embedded credentials.
 */
data class SupabaseConfig(
    val url: String = "",
    val anonKey: String = ""
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}

class SupabaseAuthRepository(
    private val config: SupabaseConfig
) : AuthRepository {
    private var session: UserSession? = null

    override suspend fun signIn(credentials: AuthCredentials): Result<UserSession> {
        if (!config.isConfigured) {
            return Result.failure(
                IllegalStateException("Supabase is not configured for this build")
            )
        }
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }
        return Result.failure(
            UnsupportedOperationException(
                "Wire this port to Supabase Auth; no live credentials are bundled"
            )
        )
    }

    override suspend fun signOut() {
        session = null
    }

    override suspend fun currentSession(): UserSession? = session
}

class InMemoryAuthRepository : AuthRepository {
    private var session: UserSession? = null

    override suspend fun signIn(credentials: AuthCredentials): Result<UserSession> {
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }
        val next = UserSession(
            userId = credentials.email.lowercase(),
            accessToken = "test-session",
            expiresAtEpochSeconds = Long.MAX_VALUE
        )
        session = next
        return Result.success(next)
    }

    override suspend fun signOut() {
        session = null
    }

    override suspend fun currentSession(): UserSession? = session
}

class InMemoryProfileRepository : ProfileRepository {
    override suspend fun getProfile(userId: String): Result<UserProfile> =
        Result.success(
            UserProfile(
                id = userId,
                email = userId,
                displayName = userId.substringBefore('@'),
                role = ProfileRole.USER
            )
        )
}
