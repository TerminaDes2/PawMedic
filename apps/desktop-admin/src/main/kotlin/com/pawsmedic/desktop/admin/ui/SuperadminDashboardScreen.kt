package com.pawsmedic.desktop.admin.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AdminOffWhiteBg = Color(0xFFF8F9FA)
val AdminDarkSlate = Color(0xFF0F172A)
val AdminBorderLight = Color(0xFFE2E8F0)
val AdminSelectedBg = Color(0xFFF1F5F9)
val AdminTextPrimary = Color(0xFF0F172A)
val AdminTextSecondary = Color(0xFF64748B)
val AdminTextMuted = Color(0xFF94A3B8)

data class RegistrationRequest(
    val id: String,
    val name: String,
    val rep: String,
    val rfc: String,
    val date: String,
    var status: String
)

data class AuditLog(
    val timestamp: String,
    val user: String,
    val action: String,
    val module: String,
    val ip: String,
    val riskLevel: String
)

@Composable
fun SuperadminDashboardScreen(
    onLogout: () -> Unit = {}
) {
    var selectedModule by remember { mutableStateOf(0) } // 0: Monitoreo, 1: Solicitudes, 2: Auditoría
    var searchQuery by remember { mutableStateOf("") }

    val requests = remember {
        mutableStateListOf(
            RegistrationRequest("SOL-2026-089", "Clínica Veterinaria San Miguel", "MVZ Carlos Eduardo Ramos", "VET891204KL2", "27/09/2026", "Pendiente"),
            RegistrationRequest("SOL-2026-090", "VetCare Pets & Specialty", "Dra. Patricia Solís", "VCP1203099X1", "28/09/2026", "Pendiente"),
            RegistrationRequest("SOL-2026-091", "Hospital Veterinario PetSalud", "MVZ Miguel Ángel Torres", "HVP9001153A0", "28/09/2026", "Pendiente"),
            RegistrationRequest("SOL-2026-092", "Centro Veterinario Las Lomas", "Dra. Sofía Valenzuela", "CVL18042211B", "28/09/2026", "Pendiente"),
            RegistrationRequest("SOL-2026-093", "AgroVet & Músculo Animal", "MVZ Fernando Castro", "AVM1105189R4", "28/09/2026", "Pendiente")
        )
    }

    val auditLogs = remember {
        listOf(
            AuditLog("28/09/2026 18:42", "root@pawsmedic.com", "Inicio de sesión Superadmin", "Auth", "192.168.1.10", "Bajo"),
            AuditLog("28/09/2026 17:15", "carlos.ramos@sanmiguel.com", "Registro de clínica completado", "Solicitudes", "201.140.22.8", "Bajo"),
            AuditLog("28/09/2026 15:30", "sistema_autorun", "Verificación periódica de licencias", "Licencias", "localhost", "Bajo"),
            AuditLog("28/09/2026 12:10", "unknown_user", "Intento fallido de autenticación admin", "Security", "187.190.45.12", "Alto"),
            AuditLog("27/09/2026 22:05", "root@pawsmedic.com", "Suspensión preventiva de VetExpress", "Cuentas", "192.168.1.10", "Medio")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminOffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // SIDEBAR IZQUIERDO (260.dp)
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .border(1.dp, AdminBorderLight)
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                // Header Marca Superadmin
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AdminDarkSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "PawsMedic",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminTextPrimary
                        )
                        Text(
                            text = "SISTEMA SUPERADMIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // LOS 3 MÓDULOS DE ADMINISTRACIÓN
                val modules = listOf(
                    Triple(0, "Monitoreo", Icons.Default.Monitor),
                    Triple(1, "Solicitudes", Icons.Default.Assignment),
                    Triple(2, "Auditoría", Icons.Default.Security)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    modules.forEach { (index, label, icon) ->
                        val isSelected = selectedModule == index

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AdminSelectedBg else Color.Transparent)
                                .clickable { selectedModule = index }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) AdminTextPrimary else AdminTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AdminTextPrimary else AdminTextSecondary
                            )
                        }
                    }
                }
            }

            // CONTENIDO DERECHO
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.White)
                        .border(1.dp, AdminBorderLight)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (selectedModule) {
                            0 -> "Monitoreo del Sistema & Servidores"
                            1 -> "Solicitudes de Registro de Veterinarias"
                            else -> "Auditoría, Licencias & Seguridad"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Buscador
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AdminOffWhiteBg)
                                .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AdminTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Buscar clínica, folio, IP...",
                                            fontSize = 13.sp,
                                            color = AdminTextMuted,
                                            maxLines = 1
                                        )
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 13.sp,
                                            color = AdminTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Profile Root & Cerrar Sesión
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
                                        .background(AdminDarkSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "SA",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Sesión Activa: Root",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminTextPrimary
                                    )
                                    Text(
                                        text = "Control Central",
                                        fontSize = 11.sp,
                                        color = AdminTextMuted
                                    )
                                }
                            }

                            // Botón Cerrar Sesión
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminOffWhiteBg)
                                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
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

                // ÁREA DE CONTENIDO SEGÚN MÓDULO SELECCIONADO
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    when (selectedModule) {
                        0 -> MonitoreoModule()
                        1 -> SolicitudesModule(requests)
                        2 -> AuditoriaModule(auditLogs)
                    }
                }
            }
        }
    }
}

// MÓDULO 1: MONITOREO
@Composable
fun MonitoreoModule() {
    Column(modifier = Modifier.fillMaxSize()) {
        // TARJETAS DE MÉTRICAS (4 Columnas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val metrics = listOf(
                Triple("Total Veterinarias", "142", "+12 este mes"),
                Triple("Usuarios Activos", "1,280", "Médicos & Asistentes"),
                Triple("Solicitudes Pendientes", "18", "Requieren revisión"),
                Triple("Salud del Sistema", "99.9%", "En línea & Operativo")
            )

            metrics.forEach { (title, valStr, sub) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(valStr, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                        Text(sub, fontSize = 11.sp, color = AdminTextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de Servidores / Microservicios
            Box(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Salud de Microservicios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    val services = listOf(
                        "Base de Datos Principal" to "Operativo (12ms)",
                        "Servicio de Autenticación" to "Operativo (0 errores)",
                        "Servidor de Almacenamiento" to "Operativo (2.4 TB libre)",
                        "Motor de Notificaciones Push" to "Operativo",
                        "API Gateway Central" to "Operativo (99.99%)"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        services.forEach { (sName, status) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminOffWhiteBg)
                                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF16A34A))
                                        )
                                        Text(sName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                                    }
                                    Text(status, fontSize = 11.sp, color = AdminTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Registro de Sesiones Activas
            Box(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Sesiones de Veterinarias en Tiempo Real", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeClinics = listOf(
                        "PawsMedic Clínica Principal" to "Plan Enterprise · 14 usuarios",
                        "VetCare Pets & Specialty" to "Plan Pro · 6 usuarios",
                        "Hospital Veterinario PetSalud" to "Plan Pro · 8 usuarios",
                        "Centro Veterinario Las Lomas" to "Plan Basic · 2 usuarios"
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(activeClinics) { (cName, detail) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminOffWhiteBg)
                                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                                        Text(detail, fontSize = 12.sp, color = AdminTextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("En Línea", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
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

// MÓDULO 2: SOLICITUDES
@Composable
fun SolicitudesModule(requests: MutableList<RegistrationRequest>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Solicitudes de Registro Pendientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
            Text("Aprueba o rechaza el alta de nuevas clínicas veterinarias en la plataforma.", fontSize = 12.sp, color = AdminTextSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            // Encabezados Tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AdminOffWhiteBg)
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FOLIO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.12f))
                Text("NOMBRE VETERINARIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.28f))
                Text("REPRESENTANTE / MVZ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.22f))
                Text("RFC / CÉDULA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.15f))
                Text("FECHA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))
                Text("ACCIONES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.13f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(requests) { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(req.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, modifier = Modifier.weight(0.12f))
                        Text(req.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, modifier = Modifier.weight(0.28f))
                        Text(req.rep, fontSize = 12.sp, color = AdminTextSecondary, modifier = Modifier.weight(0.22f))
                        Text(req.rfc, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.15f))
                        Text(req.date, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))

                        Row(
                            modifier = Modifier.weight(0.13f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (req.status == "Pendiente") {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AdminDarkSlate)
                                        .clickable {
                                            val idx = requests.indexOf(req)
                                            if (idx != -1) requests[idx] = req.copy(status = "Aprobado")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Aprobar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                                        .clickable {
                                            val idx = requests.indexOf(req)
                                            if (idx != -1) requests[idx] = req.copy(status = "Rechazado")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Rechazar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (req.status == "Aprobado") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = req.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (req.status == "Aprobado") Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// MÓDULO 3: AUDITORÍA
@Composable
fun AuditoriaModule(logs: List<AuditLog>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Registro de Auditoría & Seguridad", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
            Text("Historial inmutable de eventos del sistema y cambios de configuración.", fontSize = 12.sp, color = AdminTextSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            // Encabezado Tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AdminOffWhiteBg)
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FECHA & HORA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.18f))
                Text("USUARIO / SISTEMA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.22f))
                Text("ACCIÓN REALIZADA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.30f))
                Text("MÓDULO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.12f))
                Text("IP ORIGEN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))
                Text("RIESGO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.08f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(log.timestamp, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.18f))
                        Text(log.user, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, modifier = Modifier.weight(0.22f))
                        Text(log.action, fontSize = 12.sp, color = AdminTextSecondary, modifier = Modifier.weight(0.30f))
                        Text(log.module, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.12f))
                        Text(log.ip, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))

                        Box(
                            modifier = Modifier
                                .weight(0.08f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (log.riskLevel == "Alto") Color(0xFFFEE2E2) else AdminOffWhiteBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = log.riskLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (log.riskLevel == "Alto") Color(0xFF991B1B) else AdminTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SuperadminDashboardScreenPreview() {
    SuperadminDashboardScreen()
}
