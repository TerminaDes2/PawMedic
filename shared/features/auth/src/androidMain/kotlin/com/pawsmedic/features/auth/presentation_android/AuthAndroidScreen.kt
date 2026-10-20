package com.pawsmedic.features.auth.presentation_android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.presentation.AuthUiState
import com.pawsmedic.features.auth.presentation.login.LoginScreen
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.register.RegisterScreen
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel

@Composable
fun AuthAndroidScreen(
    state: AuthUiState,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    onAuthSuccess: (userId: String, role: PawMedicRole) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var isRegistering by rememberSaveable { mutableStateOf(false) }

    if (isRegistering) {
        RegisterScreen(
            viewModel = registerViewModel,
            onBackClick = { isRegistering = false },
            onNavigateToLogin = { isRegistering = false },
            onRegisterSuccess = { userId, role ->
                onAuthSuccess(userId, role)
            },
            modifier = modifier
        )
    } else {
        LoginScreen(
            viewModel = loginViewModel,
            onBackClick = onBackClick,
            onNavigateToRegister = { isRegistering = true },
            onLoginSuccess = { userId, role ->
                onAuthSuccess(userId, role ?: PawMedicRole.USER)
            },
            modifier = modifier
        )
    }
}