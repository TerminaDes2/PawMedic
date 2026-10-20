package com.pawsmedic.features.auth.presentation.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.presentation.login.LoginScreen
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.register.RegisterScreen
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashScreen
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import com.pawsmedic.features.auth.presentation.welcome.WelcomeScreen
import com.pawsmedic.features.pets.data.datasource.SupabasePetDataSource
import com.pawsmedic.features.pets.data.repository.DefaultPetRepository
import com.pawsmedic.features.pets.presentation.navigation.PetNavHost
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

sealed interface AuthScreenState {
    data object Splash : AuthScreenState
    data object Welcome : AuthScreenState
    data object Login : AuthScreenState
    data object Register : AuthScreenState
    data class CheckingAccess(val userId: String) : AuthScreenState
    data class Authenticated(val userId: String) : AuthScreenState
    data class AccessDenied(
        val message: String,
        val userIdToRetry: String? = null
    ) : AuthScreenState
}

@Serializable
private data class ProfileRoleRecord(val role: String)

@Composable
fun AuthNavHost(
    splashViewModel: SplashViewModel,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    supabaseClient: SupabaseClient,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Splash) }
    val coroutineScope = rememberCoroutineScope()

    Crossfade(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AuthScreenState.Splash -> {
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToWelcome = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToHome = { userId ->
                        currentScreen = AuthScreenState.CheckingAccess(userId)
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
                    onLoginSuccess = { userId, _ ->
                        currentScreen = AuthScreenState.CheckingAccess(userId)
                    }
                )
            }
            AuthScreenState.Register -> {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onBackClick = { currentScreen = AuthScreenState.Welcome },
                    onNavigateToLogin = { currentScreen = AuthScreenState.Login },
                    onRegisterSuccess = { userId, _ ->
                        currentScreen = AuthScreenState.CheckingAccess(userId)
                    }
                )
            }
            is AuthScreenState.CheckingAccess -> {
                LaunchedEffect(screen.userId) {
                    val role = try {
                        readProfileRole(supabaseClient, screen.userId)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        currentScreen = AuthScreenState.AccessDenied(
                            message = "No se pudo verificar el rol de esta cuenta: " +
                                (error.message ?: "error de conexión"),
                            userIdToRetry = screen.userId
                        )
                        return@LaunchedEffect
                    }

                    currentScreen = if (role == PawMedicRole.USER.name) {
                        AuthScreenState.Authenticated(screen.userId)
                    } else {
                        AuthScreenState.AccessDenied(
                            message = "Esta cuenta no tiene permiso para usar la aplicación móvil."
                        )
                    }
                }
                AccessCheckingScreen()
            }
            is AuthScreenState.AccessDenied -> {
                AccessDeniedScreen(
                    message = screen.message,
                    canRetry = screen.userIdToRetry != null,
                    onRetry = {
                        screen.userIdToRetry?.let { userId ->
                            currentScreen = AuthScreenState.CheckingAccess(userId)
                        }
                    },
                    onSignOut = {
                        coroutineScope.launch {
                            try {
                                supabaseClient.auth.signOut()
                                currentScreen = AuthScreenState.Welcome
                            } catch (error: Exception) {
                                currentScreen = screen.copy(
                                    message = "No se pudo cerrar la sesión: " +
                                        (error.message ?: "error de conexión")
                                )
                            }
                        }
                    }
                )
            }
            is AuthScreenState.Authenticated -> {
                LaunchedEffect(screen.userId) {
                    while (true) {
                        delay(15_000)
                        val role = try {
                            readProfileRole(supabaseClient, screen.userId)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            currentScreen = AuthScreenState.AccessDenied(
                                message = "No se pudo volver a verificar el rol. " +
                                    "Por seguridad, el acceso quedó bloqueado.",
                                userIdToRetry = screen.userId
                            )
                            return@LaunchedEffect
                        }

                        if (role != PawMedicRole.USER.name) {
                            currentScreen = AuthScreenState.AccessDenied(
                                message = "El rol de esta cuenta ya no permite usar " +
                                    "la aplicación móvil."
                            )
                            return@LaunchedEffect
                        }
                    }
                }
                AuthenticatedHomeScreen(
                    userId = screen.userId,
                    supabaseClient = supabaseClient,
                    onSignOut = { currentScreen = AuthScreenState.Welcome }
                )
            }
        }
    }
}

private suspend fun readProfileRole(
    supabaseClient: SupabaseClient,
    userId: String
): String =
    supabaseClient
        .from("profiles")
        .select(columns = Columns.list("role")) {
            filter { eq("id", userId) }
        }
        .decodeSingle<ProfileRoleRecord>()
        .role

@Composable
private fun AccessCheckingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = PawMedicColors.Teal600)
        Text(
            text = "Verificando acceso...",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun AccessDeniedScreen(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message)
        if (canRetry) {
            PawMedicPrimaryButton(
                text = "Reintentar verificación",
                onClick = onRetry,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        PawMedicPrimaryButton(
            text = "Cerrar sesión",
            onClick = onSignOut,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun AuthenticatedHomeScreen(
    userId: String,
    supabaseClient: SupabaseClient,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val petDataSource = remember(supabaseClient) { SupabasePetDataSource(supabaseClient) }
    val petRepository = remember(petDataSource) { DefaultPetRepository(petDataSource) }

    val currentUser = supabaseClient.auth.currentUserOrNull()

    val displayName = currentUser?.userMetadata
        ?.get("full_name")
        ?.toString()
        ?.replace("\"", "")
        ?.takeIf { it.isNotBlank() }
        ?: currentUser?.userMetadata
            ?.get("nombre")
            ?.toString()
            ?.replace("\"", "")
            ?.takeIf { it.isNotBlank() }
        ?: currentUser?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
        ?: "Usuario"

    PetNavHost(
        repository = petRepository,
        supabaseClient = supabaseClient,
        userName = displayName,
        modifier = modifier
    )
}