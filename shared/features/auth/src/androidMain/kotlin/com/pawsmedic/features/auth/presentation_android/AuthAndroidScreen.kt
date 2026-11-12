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
import com.pawsmedic.features.auth.presentation.register.RegistrationSuccessScreen
import io.github.jan.supabase.SupabaseClient

@Composable
fun AuthAndroidScreen(
    state: AuthUiState,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    onAuthSuccess: (userId: String, role: PawMedicRole) -> Unit,
    supabaseClient: SupabaseClient? = null,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var registeredUserId by rememberSaveable { mutableStateOf<String?>(null) }

    if (registeredUserId != null) {
        RegistrationSuccessScreen(
            onContinue = { onAuthSuccess(registeredUserId!!, PawMedicRole.USER) },
            modifier = modifier
        )
    } else if (isRegistering) {
        RegisterScreen(
            viewModel = registerViewModel,
            onBackClick = { isRegistering = false },
            onNavigateToLogin = { isRegistering = false },
            onRegisterSuccess = { userId ->
                registeredUserId = userId
            },
            modifier = modifier
        )
    } else {
        LoginScreen(
            viewModel = loginViewModel,
            onBackClick = onBackClick,
            onNavigateToRegister = { isRegistering = true },
            onLoginSuccess = { userId ->
                onAuthSuccess(userId, PawMedicRole.USER)
            },
            modifier = modifier
        )
    }
}