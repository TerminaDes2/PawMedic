package com.pawsmedic.core.data

import com.pawsmedic.core.model.AuthCredentials
import com.pawsmedic.core.model.ProfileRole
import com.pawsmedic.core.model.UserProfile
import com.pawsmedic.core.model.UserSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

/**
 * Configuración de conexión para el cliente de Supabase.
 * Los valores son inyectados desde el entorno (ej. BuildConfig en Android o System.getenv en Desktop).
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}

/**
 * Repositorio de Autenticación que interactúa con Supabase Auth.
 * Maneja inicio/cierre de sesión y recuperación del token de sesión JWT.
 */
class SupabaseAuthRepository(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun signIn(credentials: AuthCredentials): Result<UserSession> {
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email y contraseña son obligatorios"))
        }

        return try {
            // 1. Iniciar sesión con email y contraseña mediante Supabase Auth
            supabase.auth.signInWith(Email) {
                email = credentials.email
                password = credentials.password
            }

            // 2. Obtener la sesión activa generada por Supabase
            val session = supabase.auth.currentSessionOrNull()
                ?: throw IllegalStateException("No se pudo obtener la sesión tras iniciar sesión")

            // 3. Retornar el objeto de sesión con el token JWT y el UUID del usuario
            Result.success(
                UserSession(
                    userId = session.user?.id ?: "",
                    accessToken = session.accessToken,
                    expiresAtEpochSeconds = session.expiresAt.epochSeconds
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override suspend fun currentSession(): UserSession? {
        val session = supabase.auth.currentSessionOrNull() ?: return null
        return UserSession(
            userId = session.user?.id ?: "",
            accessToken = session.accessToken,
            expiresAtEpochSeconds = session.expiresAt.epochSeconds
        )
    }
}

/**
 * Repositorio para consultar y gestionar perfiles de usuario en la tabla 'profiles' de Supabase.
 * Utiliza Postgrest y opera respetando la seguridad RLS (Row Level Security) basada en el UUID del usuario.
 */
class SupabaseProfileRepository(
    private val supabase: SupabaseClient
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Result<UserProfile> {
        return try {
            // Consultar a la tabla 'profiles' en Supabase pasando el UUID como filtro
            val profile = supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>() // Deserializa automáticamente a la data class Kotlin
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Implementación simulada de Autenticación para pruebas unitarias o desarrollo sin red.
 */
class InMemoryAuthRepository : AuthRepository {
    private var session: UserSession? = null

    override suspend fun signIn(credentials: AuthCredentials): Result<UserSession> {
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email y contraseña son obligatorios"))
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

/**
 * Implementación simulada de Perfil para pruebas unitarias.
 */
class InMemoryProfileRepository : ProfileRepository {
    override suspend fun getProfile(userId: String): Result<UserProfile> =
        Result.success(
            UserProfile(
                id = userId,
                nombre = "Test",
                apellidos = "User",
                role = ProfileRole.USER
            )
        )
}
