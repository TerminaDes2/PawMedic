package com.pawsmedic.desktop.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pawsmedic.desktop.admin.ui.AdminLoginScreen
import com.pawsmedic.desktop.admin.ui.BusinessApplicationsAdminScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma de Administración Central - Superadmin",
        state = windowState
    ) {
        MaterialTheme {
            AdminApplication()
        }
    }
}

private sealed interface AdminScreen {
    data object Loading : AdminScreen
    data object Login : AdminScreen
    data object Applications : AdminScreen
}

@Serializable
private data class AdminProfileRole(
    @SerialName("role") val role: String
)

@Composable
private fun AdminApplication() {
    val supabaseClient = remember { createDesktopSupabaseClient() }
    val coroutineScope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<AdminScreen>(AdminScreen.Loading) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun requireSuperadmin(client: SupabaseClient) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay una sesión autenticada.")
        val profile = client
            .from("profiles")
            .select(columns = Columns.list("role")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<AdminProfileRole>()

        if (profile.role != "SUPERADMIN") {
            throw IllegalAccessException("Esta cuenta no tiene permisos de Superadmin.")
        }
    }

    LaunchedEffect(supabaseClient) {
        if (supabaseClient.auth.currentSessionOrNull() == null) {
            screen = AdminScreen.Login
        } else {
            try {
                requireSuperadmin(supabaseClient)
                screen = AdminScreen.Applications
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                supabaseClient.auth.signOut()
                errorMessage = error.message ?: "No se pudo validar la cuenta."
                screen = AdminScreen.Login
            }
        }
    }

    when (screen) {
        AdminScreen.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        AdminScreen.Login -> AdminLoginScreen(
            errorMessage = errorMessage,
            onLogin = { email, password ->
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                try {
                    requireSuperadmin(supabaseClient)
                    errorMessage = null
                    screen = AdminScreen.Applications
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    supabaseClient.auth.signOut()
                    throw error
                }
            }
        )
        AdminScreen.Applications -> BusinessApplicationsAdminScreen(
            supabaseClient = supabaseClient,
            onLogout = {
                coroutineScope.launch {
                    try {
                        supabaseClient.auth.signOut()
                        errorMessage = null
                        screen = AdminScreen.Login
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "No se pudo cerrar la sesión."
                    }
                }
            }
        )
    }
}
