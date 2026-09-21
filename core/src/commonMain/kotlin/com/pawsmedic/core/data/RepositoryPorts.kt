package com.pawsmedic.core.data

import com.pawsmedic.core.model.AuthCredentials
import com.pawsmedic.core.model.UserProfile
import com.pawsmedic.core.model.UserSession

interface AuthRepository {
    suspend fun signIn(credentials: AuthCredentials): Result<UserSession>
    suspend fun signOut()
    suspend fun currentSession(): UserSession?
}

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<UserProfile>
}
