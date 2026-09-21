package com.pawsmedic.features.auth.domain

import com.pawsmedic.core.model.UserSession

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Loading : AuthState
    data class SignedIn(val session: UserSession) : AuthState
    data class Error(val message: String) : AuthState
}
