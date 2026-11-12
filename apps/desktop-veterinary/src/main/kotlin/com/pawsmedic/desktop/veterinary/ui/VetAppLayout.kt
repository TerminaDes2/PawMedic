package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.desktop.veterinary.ui.theme.*

@Composable
fun VetAppLayout(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    title: String,
    userEmail: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    var showAdminModal by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // SIDEBAR IZQUIERDO (260.dp)
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(CardWhite)
                    .border(1.dp, BorderLight)
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                // Header Marca PawsMedic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClinicalEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paws",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Medic",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ÚNICOS 3 ÍTEMS DE NAVEGACIÓN
                val menuItems = listOf(
                    Triple("Agenda", "Agenda y Citas", Icons.Default.CalendarMonth),
                    Triple("Expedientes", "Expedientes Clínicos", Icons.Default.FolderShared),
                    Triple("Catalog", "Catálogo de Servicios", Icons.Default.ShoppingCart)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    menuItems.forEach { (id, label, icon) ->
                        val isSelected = currentScreen == id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldLightBg else Color.Transparent)
                                .clickable { onNavigate(id) }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) ClinicalEmerald else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ClinicalEmerald else TextSecondary
                            )
                        }
                    }
                }
            }

            // CONTENIDO PRINCIPAL DERECHO
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(CardWhite)
                        .border(1.dp, BorderLight)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Buscador
                        AlignedSearchBox(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Buscar paciente, dueño, folio...",
                            modifier = Modifier.width(300.dp)
                        )

                        // Perfil Usuario (Muestra icono y nombre antes de @)
                        val displayName = if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "Dr. Valdez"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLightBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ClinicalEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = userEmail.ifBlank { "Clínica Principal" },
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Botón Cerrar Sesión
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OffWhiteBg)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                    .clickable { onLogout() }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Cerrar Sesión",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }

                // CONTENIDO DE PANTALLA
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }

        // MODAL DE AUTENTICACIÓN ADMIN
        if (showAdminModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(480.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ClinicalEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Autenticación Admin",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingrese credenciales de administración para acceder.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("Usuario") },
                            placeholder = { Text("admin@pawsmedic.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            isError = loginError
                        )

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            isError = loginError
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAdminModal = false
                                    adminUsername = ""
                                    adminPassword = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    if (adminUsername.isNotEmpty() && adminPassword.isNotEmpty()) {
                                        showAdminModal = false
                                    } else {
                                        loginError = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                            ) {
                                Text("Iniciar Sesión", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlignedSearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OffWhiteBg)
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
