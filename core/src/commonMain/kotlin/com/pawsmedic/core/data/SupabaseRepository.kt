package com.pawsmedic.core.data

import com.pawsmedic.core.model.AuthCredentials
import com.pawsmedic.core.model.ProfileRole
import com.pawsmedic.core.model.UserProfile
import com.pawsmedic.core.model.UserSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * Configuration is intentionally supplied by the host application. This
 * adapter is the boundary for a Supabase Auth/REST client and remains usable
 * in previews and tests without a network or embedded credentials.
 */
data class SupabaseConfig(
    val url: String = "https://fmcsdyupvmdjvlbzjgml.supabase.co",
    val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZtY3NkeXVwdm1kanZsYnpqZ21sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAyOTE0MDMsImV4cCI6MjEwNTg2NzQwM30.y4dIbAKuGdFujQNos3HXGH-TYbtCMMEFw083SbGwkVc"
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}

class SupabaseAuthRepository(
private val supabaseClient: SupabaseClient, // Aquí inyectas el cliente real
private val config: SupabaseConfig
) : AuthRepository {

    override suspend fun signIn(credentials: AuthCredentials): Result<UserSession> {
        if (!config.isConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured for this build"))
        }
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }

        return try {
            // 1. Llamada real a la API de Supabase para iniciar sesión
            supabaseClient.auth.signInWith(Email) {
                email = credentials.email
                password = credentials.password
            }

            // 2. Obtener la sesión que Supabase guardó internamente tras el login
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw IllegalStateException("Error al obtener la sesión después del login")

            // 3. Mapearlo a tu modelo de dominio
            Result.success(
                UserSession(
                    userId = session.user?.id ?: "",
                    accessToken = session.accessToken,
                    expiresAtEpochSeconds = session.expiresAt.epochSeconds
                )
            )
        } catch (e: Exception) {
            // Si el correo no existe o la contraseña está mal, cae aquí
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override suspend fun currentSession(): UserSession? {
        val session = supabaseClient.auth.currentSessionOrNull() ?: return null
        return UserSession(
            userId = session.user?.id ?: "",
            accessToken = session.accessToken,
            expiresAtEpochSeconds = session.expiresAt.epochSeconds
        )
    }
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
