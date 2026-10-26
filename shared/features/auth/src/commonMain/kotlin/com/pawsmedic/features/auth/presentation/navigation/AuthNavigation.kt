package com.pawsmedic.features.auth.presentation.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pawsmedic.features.pets.data.datasource.SupabasePetDataSource
import com.pawsmedic.features.pets.data.repository.DefaultPetRepository
import com.pawsmedic.features.auth.presentation.login.LoginScreen
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.otp.OtpVerificationScreen
import com.pawsmedic.features.auth.presentation.register.RegisterScreen
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashScreen
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import com.pawsmedic.features.auth.presentation.welcome.WelcomeScreen
import com.pawsmedic.features.pets.presentation.navigation.PetNavHost
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.collect

sealed interface AuthScreenState {
    data object Splash : AuthScreenState
    data object Welcome : AuthScreenState
    data object Login : AuthScreenState
    data object Register : AuthScreenState
    data object OtpVerification : AuthScreenState
    data class Authenticated(val userId: String, val email: String?) : AuthScreenState
}

@Composable
fun AuthNavHost(
    splashViewModel: SplashViewModel,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    supabaseClient: SupabaseClient,
    onGoogleLoginClick: (() -> Unit)? = null,
    onGoogleRegisterClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Splash) }

    LaunchedEffect(registerViewModel) {
        registerViewModel.uiState.collect { state ->
            if (state.requiresOtpVerification) {
                currentScreen = AuthScreenState.OtpVerification
                registerViewModel.onOtpNavigationHandled()
            }
        }
    }

    Crossfade(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AuthScreenState.Splash -> {
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToWelcome = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToHome = { userId, email ->
                        currentScreen = AuthScreenState.Authenticated(userId, email)
                    }
                )
            }
            AuthScreenState.Welcome -> {
                WelcomeScreen(
                    onNavigateToLogin = { currentScreen = AuthScreenState.Login },
                    onNavigateToRegister = { currentScreen = AuthScreenState.Register }
                )
            }
            AuthScreenState.Login -> {
                LoginScreen(
                    viewModel = loginViewModel,
                    onBackClick = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToRegister = { currentScreen = AuthScreenState.Register },
                    onLoginSuccess = { userId ->
                        currentScreen = AuthScreenState.Authenticated(userId, loginViewModel.uiState.value.userEmail)
                    },
                    onGoogleSignInClick = onGoogleLoginClick
                )
            }
            AuthScreenState.Register -> {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onBackClick = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToLogin = { currentScreen = AuthScreenState.Login },
                    onRegisterSuccess = { userId ->
                        currentScreen = AuthScreenState.Authenticated(userId, registerViewModel.uiState.value.userEmail)
                    },
                    onGoogleSignInClick = onGoogleRegisterClick
                )
            }
            AuthScreenState.OtpVerification -> {
                OtpVerificationScreen(
                    viewModel = registerViewModel,
                    onBackClick = { currentScreen = AuthScreenState.Register },
                    onVerificationSuccess = { userId ->
                        currentScreen = AuthScreenState.Authenticated(userId, registerViewModel.uiState.value.userEmail)
                    }
                )
            }
            is AuthScreenState.Authenticated -> {
                AuthenticatedHomeScreen(
                    userId = screen.userId,
                    email = screen.email,
                    supabaseClient = supabaseClient,
                    onSignOut = {
                        loginViewModel.resetAfterSignOut()
                        registerViewModel.resetAfterSignOut()
                        currentScreen = AuthScreenState.Welcome
                    }
                )
            }
        }
    }
}

@Composable
fun AuthenticatedHomeScreen(
    userId: String,
    email: String?,
    supabaseClient: SupabaseClient,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val petRepository = remember(supabaseClient) {
        DefaultPetRepository(SupabasePetDataSource(supabaseClient))
    }

    PetNavHost(
        repository = petRepository,
        ownerId = userId,
        userName = email?.substringBefore("@")?.takeIf(String::isNotBlank) ?: "usuario",
        supabaseClient = supabaseClient,
        onSignOut = {
            supabaseClient.auth.signOut()
            onSignOut()
        },
        modifier = modifier
    )
}
