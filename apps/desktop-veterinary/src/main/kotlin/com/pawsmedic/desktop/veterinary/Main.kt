package com.pawsmedic.desktop.veterinary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.pawsmedic.desktop.veterinary.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    var showExitDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Login") }
    var activeUserEmail by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Restauración automática de sesión guardada mediante SessionManager al iniciar
    LaunchedEffect(Unit) {
        try {
            val savedEmail = SessionManager.getSavedEmail()
            if (!savedEmail.isNullOrBlank() && SessionManager.isRememberMeEnabled()) {
                activeUserEmail = savedEmail
                val isSuperAdmin = savedEmail.equals("super@gmail.com", ignoreCase = true) ||
                                   savedEmail.equals("admin@pawsmedic.com", ignoreCase = true) ||
                                   savedEmail.lowercase().contains("admin") ||
                                   savedEmail.lowercase().contains("super")
                currentScreen = if (isSuperAdmin) "Admin" else "Agenda"
            } else {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null && session.user?.email != null) {
                    val email = session.user!!.email!!
                    activeUserEmail = email
                    val isSuperAdmin = email.equals("super@gmail.com", ignoreCase = true) ||
                                       email.equals("admin@pawsmedic.com", ignoreCase = true) ||
                                       email.lowercase().contains("admin") ||
                                       email.lowercase().contains("super")
                    currentScreen = if (isSuperAdmin) "Admin" else "Agenda"
                }
            }
        } catch (e: Exception) {
            // Continuar en Login
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            SessionManager.clearSession()
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
                // Ignorar
            }
            activeUserEmail = ""
            currentScreen = "Login"
        }
    }

    Window(
        onCloseRequest = {
            if (currentScreen != "Login") {
                showExitDialog = true
            } else {
                exitApplication()
            }
        },
        title = "PawsMedic - Sistema Veterinario",
        state = windowState
    ) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    "Login" -> LoginScreen(
                        onNavigateToVetMain = { email ->
                            activeUserEmail = email
                            currentScreen = "Agenda"
                        },
                        onNavigateToAdminMain = { email ->
                            activeUserEmail = email
                            currentScreen = "Admin"
                        },
                        onNavigateToRegister = { currentScreen = "Register" }
                    )
                    "Register" -> RegisterScreen(
                        onNavigateToVetMain = { currentScreen = "Agenda" },
                        onNavigateToLogin = { currentScreen = "Login" }
                    )
                    "Agenda" -> AgendaScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Expedientes" -> ExpedientesScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Catalog" -> CatalogScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Admin" -> SuperadminDashboardScreen(
                        userEmail = activeUserEmail,
                        onLogout = { handleLogout() }
                    )
                }

                // DIÁLOGO DE CONFIRMACIÓN DE SALIDA CON SESIÓN ACTIVA
                if (showExitDialog) {
                    Dialog(onDismissRequest = { showExitDialog = false }) {
                        Card(
                            modifier = Modifier
                                .width(540.dp)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EmeraldLightBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ClinicalEmerald,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Text(
                                    text = "Aviso: Sesión Activa",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "No cerraste sesión. Cuando vuelvas a abrir el programa tu sesión estará activa.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showExitDialog = false },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Regresar", fontSize = 12.sp, color = TextPrimary)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                SessionManager.clearSession()
                                                try {
                                                    supabase.auth.signOut()
                                                } catch (e: Exception) {
                                                    // Ignorar
                                                }
                                                showExitDialog = false
                                                exitApplication()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Text("Cerrar sesión y salir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            showExitDialog = false
                                            exitApplication()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                    ) {
                                        Text("Continuar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
