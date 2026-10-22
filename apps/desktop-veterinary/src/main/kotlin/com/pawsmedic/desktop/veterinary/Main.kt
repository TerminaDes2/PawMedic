package com.pawsmedic.desktop.veterinary

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pawsmedic.desktop.admin.ui.BusinessApplicationsAdminScreen
import com.pawsmedic.desktop.veterinary.ui.AgendaScreen
import com.pawsmedic.desktop.veterinary.ui.CatalogScreen
import com.pawsmedic.desktop.veterinary.ui.ExpedientesScreen
import com.pawsmedic.desktop.veterinary.ui.auth.BusinessApplicationStatusScreen
import com.pawsmedic.desktop.veterinary.ui.auth.LoginScreen
import com.pawsmedic.desktop.veterinary.ui.auth.RegisterScreen
import com.pawsmedic.features.products.data.datasource.SupabaseProductDataSource
import com.pawsmedic.features.products.data.repository.DefaultProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface DesktopScreen {
    data object Loading : DesktopScreen
    data object Login : DesktopScreen
    data object Register : DesktopScreen
    data class Pending(val userId: String, val application: BusinessApplicationRecord) : DesktopScreen
    data class Rejected(val userId: String, val application: BusinessApplicationRecord) : DesktopScreen
    data object Agenda : DesktopScreen
    data object Expedientes : DesktopScreen
    data object Catalog : DesktopScreen
    data object AdminApplications : DesktopScreen
}

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    Window(
        onCloseRequest = ::exitApplication,
        title = "PawsMedic - Sistema Veterinario",
        state = windowState
    ) {
        MaterialTheme {
            VeterinaryDesktopApp()
        }
    }
}

@Composable
private fun VeterinaryDesktopApp() {
    val supabaseClient: SupabaseClient = remember { supabase }
    val authRepository = remember(supabaseClient) {
        DesktopAuthRepository(supabaseClient)
    }
    val productRepository = remember(supabaseClient) {
        DefaultProductRepository(SupabaseProductDataSource(supabaseClient))
    }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<DesktopScreen>(DesktopScreen.Loading) }
    var loginNotice by remember { mutableStateOf<String?>(null) }
    var pendingRefreshError by remember { mutableStateOf<String?>(null) }
    var isRefreshingPending by remember { mutableStateOf(false) }
    var activeTenantId by remember { mutableStateOf<String?>(null) }

    fun navigateForAccess(access: DesktopAccess) {
        pendingRefreshError = null
        currentScreen = when (access) {
            DesktopAccess.Administrator -> DesktopScreen.AdminApplications
            is DesktopAccess.ApprovedVeterinary -> {
                activeTenantId = access.tenantId
                DesktopScreen.Agenda
            }
            is DesktopAccess.PendingVeterinary ->
                DesktopScreen.Pending(access.userId, access.application)
            is DesktopAccess.RejectedVeterinary ->
                DesktopScreen.Rejected(access.userId, access.application)
        }
    }

    fun signOut() {
        coroutineScope.launch {
            try {
                authRepository.signOut()
                loginNotice = null
                currentScreen = DesktopScreen.Login
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                loginNotice = "No se pudo cerrar la sesión: " +
                    (error.message ?: "error de conexión")
            }
        }
    }

    LaunchedEffect(authRepository) {
        if (supabaseClient.auth.currentSessionOrNull() == null) {
            currentScreen = DesktopScreen.Login
        } else {
            try {
                navigateForAccess(authRepository.restoreAccess())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                authRepository.signOut()
                loginNotice = error.message ?: "No se pudo verificar la sesión."
                currentScreen = DesktopScreen.Login
            }
        }
    }

    when (val screen = currentScreen) {
        DesktopScreen.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        DesktopScreen.Login -> LoginScreen(
            onLogin = { email, password ->
                try {
                    val access = authRepository.signIn(email, password)
                    loginNotice = null
                    navigateForAccess(access)
                } catch (error: Exception) {
                    loginNotice = error.message ?: "No se pudo iniciar sesión."
                }
            },
            onNavigateToRegister = {
                loginNotice = null
                currentScreen = DesktopScreen.Register
            },
            loginNotice = loginNotice,
            onNavigateToAdminMain = {
                currentScreen = DesktopScreen.AdminApplications
            }
        )

        DesktopScreen.Register -> RegisterScreen(
            onNavigateToVetMain = {
                currentScreen = DesktopScreen.Agenda
            },
            onNavigateToLogin = {
                currentScreen = DesktopScreen.Login
            }
        )

        is DesktopScreen.Pending -> BusinessApplicationStatusScreen(
            application = screen.application,
            isRejected = false,
            isRefreshing = isRefreshingPending,
            errorMessage = pendingRefreshError,
            onRefresh = {
                if (!isRefreshingPending) {
                    coroutineScope.launch {
                        isRefreshingPending = true
                        pendingRefreshError = null
                        try {
                            navigateForAccess(
                                authRepository.refreshAccess(screen.userId)
                            )
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            pendingRefreshError =
                                error.message ?: "No se pudo consultar la solicitud."
                        } finally {
                            isRefreshingPending = false
                        }
                    }
                }
            },
            onSignOut = ::signOut
        )

        is DesktopScreen.Rejected -> BusinessApplicationStatusScreen(
            application = screen.application,
            isRejected = true,
            isRefreshing = false,
            errorMessage = null,
            onRefresh = {},
            onSignOut = ::signOut
        )

        DesktopScreen.Agenda -> AgendaScreen(
            onNavigate = { destination ->
                currentScreen = when (destination) {
                    "Expedientes" -> DesktopScreen.Expedientes
                    "Catalog" -> DesktopScreen.Catalog
                    else -> DesktopScreen.Agenda
                }
            },
            onLogout = ::signOut
        )

        DesktopScreen.Expedientes -> ExpedientesScreen(
            onNavigate = { destination ->
                currentScreen = when (destination) {
                    "Agenda" -> DesktopScreen.Agenda
                    "Catalog" -> DesktopScreen.Catalog
                    else -> DesktopScreen.Expedientes
                }
            },
            onLogout = ::signOut
        )

        DesktopScreen.Catalog -> CatalogScreen(
            repository = productRepository,
            tenantId = activeTenantId
                ?: error("No hay una clínica asociada a la sesión."),
            onNavigate = { destination ->
                currentScreen = when (destination) {
                    "Agenda" -> DesktopScreen.Agenda
                    "Expedientes" -> DesktopScreen.Expedientes
                    else -> DesktopScreen.Catalog
                }
            },
            onLogout = ::signOut
        )

        DesktopScreen.AdminApplications -> BusinessApplicationsAdminScreen(
            supabaseClient = supabaseClient,
            onLogout = ::signOut
        )
    }
}
