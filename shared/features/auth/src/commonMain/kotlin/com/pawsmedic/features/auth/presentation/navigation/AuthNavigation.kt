package com.pawsmedic.features.auth.presentation.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pawsmedic.features.auth.presentation.login.LoginScreen
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.register.RegisterScreen
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashScreen
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import com.pawsmedic.features.auth.presentation.welcome.WelcomeScreen
import com.pawsmedic.features.pets.data.datasource.InMemoryPetDataSource
import com.pawsmedic.features.pets.data.repository.DefaultPetRepository
import com.pawsmedic.features.pets.presentation.navigation.PetNavHost

sealed interface AuthScreenState {
    data object Splash : AuthScreenState
    data object Welcome : AuthScreenState
    data object Login : AuthScreenState
    data object Register : AuthScreenState
    data class Authenticated(val userId: String) : AuthScreenState
}

@Composable
fun AuthNavHost(
    splashViewModel: SplashViewModel,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Splash) }

    Crossfade(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AuthScreenState.Splash -> {
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToWelcome = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToHome = { userId -> currentScreen = AuthScreenState.Authenticated(userId) }
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
                    onLoginSuccess = { userId -> currentScreen = AuthScreenState.Authenticated(userId) }
                )
            }
            AuthScreenState.Register -> {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onBackClick = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToLogin = { currentScreen = AuthScreenState.Login },
                    onRegisterSuccess = { userId -> currentScreen = AuthScreenState.Authenticated(userId) }
                )
            }
            is AuthScreenState.Authenticated -> {
                AuthenticatedHomeScreen(
                    userId = screen.userId,
                    onSignOut = { currentScreen = AuthScreenState.Welcome }
                )
            }
        }
    }
}

@Composable
fun AuthenticatedHomeScreen(
    userId: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val petDataSource = remember { InMemoryPetDataSource() }
    val petRepository = remember { DefaultPetRepository(petDataSource) }

    PetNavHost(
        repository = petRepository,
        modifier = modifier
    )
}
