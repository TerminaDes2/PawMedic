package com.pawsmedic.features.auth.presentation

import com.pawsmedic.features.auth.domain.model.PawMedicRole

enum class AppRoute { USER_HOME, VETERINARY_DASHBOARD, SUPERADMIN_DASHBOARD, SIGN_IN }

fun AuthUiState.route(): AppRoute = when (this) {
    is AuthUiState.Authenticated -> when (session.role) {
        PawMedicRole.USER -> AppRoute.USER_HOME
        PawMedicRole.VETERINARY_BUSINESS -> AppRoute.VETERINARY_DASHBOARD
        PawMedicRole.SUPERADMIN -> AppRoute.SUPERADMIN_DASHBOARD
    }
    AuthUiState.Loading, AuthUiState.SignedOut, is AuthUiState.Error -> AppRoute.SIGN_IN
}
