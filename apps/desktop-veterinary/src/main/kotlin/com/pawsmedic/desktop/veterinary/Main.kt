package com.pawsmedic.desktop.veterinary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pawsmedic.desktop.admin.ui.SuperadminDashboardScreen
import com.pawsmedic.desktop.veterinary.ui.AgendaScreen
import com.pawsmedic.desktop.veterinary.ui.CatalogScreen
import com.pawsmedic.desktop.veterinary.ui.ExpedientesScreen
import com.pawsmedic.desktop.veterinary.ui.auth.LoginScreen
import com.pawsmedic.desktop.veterinary.ui.auth.RegisterScreen

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        onCloseRequest = ::exitApplication,
        title = "PawsMedic - Sistema Veterinario",
        state = windowState
    ) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf("Login") }

            when (currentScreen) {
                "Login" -> LoginScreen(
                    onNavigateToVetMain = { currentScreen = "Agenda" },
                    onNavigateToAdminMain = { currentScreen = "Admin" },
                    onNavigateToRegister = { currentScreen = "Register" }
                )
                "Register" -> RegisterScreen(
                    onNavigateToVetMain = { currentScreen = "Agenda" },
                    onNavigateToLogin = { currentScreen = "Login" }
                )
                "Agenda" -> AgendaScreen(
                    onNavigate = { currentScreen = it },
                    onLogout = { currentScreen = "Login" }
                )
                "Expedientes" -> ExpedientesScreen(
                    onNavigate = { currentScreen = it },
                    onLogout = { currentScreen = "Login" }
                )
                "Catalog" -> CatalogScreen(
                    onNavigate = { currentScreen = it },
                    onLogout = { currentScreen = "Login" }
                )
                "Admin" -> SuperadminDashboardScreen(
                    onLogout = { currentScreen = "Login" }
                )
            }
        }
    }
}
