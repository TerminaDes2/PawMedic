package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto

/**
 * Platform adapters can implement this with Supabase Auth. It deliberately
 * accepts configuration rather than embedding a URL, anon key, or secret.
 */
interface SupabaseAuthDataSource {
    suspend fun currentSession(): AuthSessionDto?
    suspend fun signIn(email: String, password: String): AuthSessionDto
    suspend fun signOut()
}
