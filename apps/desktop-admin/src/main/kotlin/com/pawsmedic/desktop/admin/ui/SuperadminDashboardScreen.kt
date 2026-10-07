package com.pawsmedic.desktop.admin.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.admin.model.BusinessApplicationDto
import com.pawsmedic.desktop.admin.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

val AdminOffWhiteBg = Color(0xFFF8FAFC)
val AdminDarkSlate = Color(0xFF0F172A)
val AdminBorderLight = Color(0xFFE2E8F0)
val AdminSelectedBg = Color(0xFFCCFBF1)
val AdminSelectedText = Color(0xFF0D9488)
val AdminEmeraldGreen = Color(0xFF0D9488)
val AdminTextPrimary = Color(0xFF1E293B)
val AdminTextSecondary = Color(0xFF64748B)
val AdminTextMuted = Color(0xFF94A3B8)

data class RegistrationRequest(
    val id: String,
    val name: String,
    val rep: String,
    val rfc: String,
    val date: String,
    var status: String,
    val email: String = "contacto@clinica.com",
    val phone: String = "+52 55 1234 5678",
    val address: String = "Av. Principal #123, Col. Centro",
    val licenseNumber: String = "CÉD-981204-VET"
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
    userEmail: String = "",
    onLogout: () -> Unit = {}
) {
    var selectedModule by remember { mutableStateOf(0) } // 0: Monitoreo, 1: Solicitudes, 2: Auditoría
    var searchQuery by remember { mutableStateOf("") }

    val requests = remember {
        mutableStateListOf(
            RegistrationRequest("SOL-2026-089", "Clínica Veterinaria San Miguel", "MVZ Carlos Eduardo Ramos", "VET891204KL2", "27/09/2026", "Pendiente", "contacto@sanmiguelvet.com", "+52 55 8765 4321", "Av. San Miguel #45, Col. Jardines", "CÉD-891204-MVZ"),
            RegistrationRequest("SOL-2026-090", "VetCare Pets & Specialty", "Dra. Patricia Solís", "VCP1203099X1", "28/09/2026", "Pendiente", "p.solis@vetcarepets.com", "+52 55 9812 3456", "Calle Del Bosque #102, Lomas", "CÉD-120309-MVZ"),
            RegistrationRequest("SOL-2026-091", "Hospital Veterinario PetSalud", "MVZ Miguel Ángel Torres", "HVP9001153A0", "28/09/2026", "Pendiente", "m.torres@petsalud.org", "+52 55 4567 8901", "Blvd. Los Olivos #78, Centro", "CÉD-900115-MVZ"),
            RegistrationRequest("SOL-2026-092", "Centro Veterinario Las Lomas", "Dra. Sofía Valenzuela", "CVL18042211B", "28/09/2026", "Pendiente", "s.valenzuela@laslomasvet.com", "+52 55 2345 6789", "Av. Las Lomas #310, Poniente", "CÉD-180422-MVZ"),
            RegistrationRequest("SOL-2026-093", "AgroVet & Músculo Animal", "MVZ Fernando Castro", "AVM1105189R4", "28/09/2026", "Pendiente", "f.castro@agrovet.mx", "+52 55 3456 7890", "Carretera Nacional Km 12", "CÉD-110518-MVZ")
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
                // Header Marca Superadmin (Paleta Esmeralda Clínico)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AdminEmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paws",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminTextPrimary
                        )
                        Text(
                            text = "Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminEmeraldGreen
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
                                tint = if (isSelected) AdminSelectedText else AdminTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AdminSelectedText else AdminTextSecondary
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
                            1 -> "Solicitudes de Registro de Veterinarias y Cuentas"
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
                        val displayName = if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "admin"
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
                                        .background(AdminSelectedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = AdminSelectedText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminTextPrimary
                                    )
                                    Text(
                                        text = userEmail.ifBlank { "Control Central" },
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
                        Text(sub, fontSize = 11.sp, color = AdminSelectedText)
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
                                                .background(AdminEmeraldGreen)
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
                                            .background(AdminSelectedBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("En Línea", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminSelectedText)
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

// MÓDULO 2: SOLICITUDES DIVIDIDAS EN SUB-PESTAÑAS (VETERINARIAS VS SUPERADMIN)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolicitudesModule(requests: MutableList<RegistrationRequest>) {
    var subTab by remember { mutableStateOf(0) } // 0: Veterinarias, 1: Superadmin
    var selectedRequestForDetail by remember { mutableStateOf<RegistrationRequest?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val superadminRequests = remember {
        mutableStateListOf(
            RegistrationRequest("SOL-ADM-001", "N/A - Control Central", "Lic. Roberto Gómez", "ADMIN-001", "29/09/2026", "Pendiente", "roberto.gomez@pawsmedic.com", "+52 55 1122 3344", "Oficina Central Central", "CÉD-ADM-01"),
            RegistrationRequest("SOL-ADM-002", "N/A - Control Central", "Ing. Daniela Morales", "ADMIN-002", "30/09/2026", "Pendiente", "daniela.morales@pawsmedic.com", "+52 55 9988 7766", "Oficina Central Norte", "CÉD-ADM-02")
        )
    }

    val currentList = if (subTab == 0) requests else superadminRequests

    // Consulta en tiempo real a Supabase Postgrest 'business_applications'
    LaunchedEffect(Unit) {
        try {
            val fetchedApps = supabase.postgrest["business_applications"]
                .select()
                .decodeList<BusinessApplicationDto>()

            if (fetchedApps.isNotEmpty()) {
                requests.clear()
                fetchedApps.forEach { dto ->
                    requests.add(
                        RegistrationRequest(
                            id = dto.id ?: "SOL-2026-001",
                            name = dto.businessName.ifBlank { "Clínica Veterinaria" },
                            rep = dto.applicantName.ifBlank { "MVZ Responsable" },
                            rfc = dto.taxId.ifBlank { "VET123456789" },
                            date = "Hoy",
                            status = if (dto.status.contains("APROB", ignoreCase = true)) "Aprobado"
                                     else if (dto.status.contains("RECHAZ", ignoreCase = true)) "Rechazado"
                                     else "Pendiente",
                            email = dto.email.ifBlank { "contacto@clinica.com" },
                            phone = dto.phone.ifBlank { "+52 55 1234 5678" },
                            address = dto.address.ifBlank { "Av. Principal #123" }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Se conserva la lista inicial
        }
    }

    fun updateStatus(req: RegistrationRequest, newStatus: String) {
        val idx = currentList.indexOf(req)
        if (idx != -1) currentList[idx] = req.copy(status = newStatus)

        coroutineScope.launch {
            try {
                val dbStatus = if (newStatus == "Aprobado") "APROBADA" else "RECHAZADA"
                supabase.postgrest["business_applications"]
                    .update(mapOf("status" to dbStatus)) {
                        filter {
                            eq("id", req.id)
                        }
                    }
            } catch (e: Exception) {
                // Actualizado localmente
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Solicitudes de Registro Pendientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Text("Aprueba o rechaza cuentas de Veterinarias y Operadores Superadmin.", fontSize = 12.sp, color = AdminTextSecondary)
                }

                // SUB-PESTAÑAS: VETERINARIAS VS SUPERADMIN
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AdminOffWhiteBg)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (subTab == 0) AdminEmeraldGreen else Color.Transparent)
                            .clickable { subTab = 0 }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Veterinarias", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (subTab == 0) Color.White else AdminTextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (subTab == 1) AdminEmeraldGreen else Color.Transparent)
                            .clickable { subTab = 1 }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Cuentas Superadmin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (subTab == 1) Color.White else AdminTextSecondary)
                    }
                }
            }

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
                Text(if (subTab == 0) "NOMBRE VETERINARIA" else "OFICINA CENTRAL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.28f))
                Text("SOLICITANTE / MVZ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.22f))
                Text("RFC / CÉDULA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.15f))
                Text("FECHA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))
                Text("ACCIONES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.13f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList) { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                            .combinedClickable(
                                onClick = {},
                                onDoubleClick = { selectedRequestForDetail = req }
                            )
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
                                        .background(AdminEmeraldGreen)
                                        .clickable { updateStatus(req, "Aprobado") },
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
                                        .clickable { updateStatus(req, "Rechazado") },
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
                                        .background(if (req.status == "Aprobado") AdminSelectedBg else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = req.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (req.status == "Aprobado") AdminSelectedText else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO MODAL DE DETALLE DE LA SOLICITUD
    if (selectedRequestForDetail != null) {
        val req = selectedRequestForDetail!!
        Dialog(onDismissRequest = { selectedRequestForDetail = null }) {
            Card(
                modifier = Modifier
                    .width(660.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header del Diálogo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(AdminSelectedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (subTab == 0) Icons.Default.Store else Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = AdminEmeraldGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = req.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminTextPrimary
                                )
                                Text(
                                    text = "Folio: ${req.id} · Fecha de Solicitud: ${req.date}",
                                    fontSize = 12.sp,
                                    color = AdminTextSecondary
                                )
                            }
                        }

                        // Badge de Estado
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (req.status) {
                                        "Aprobado" -> AdminSelectedBg
                                        "Rechazado" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = req.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (req.status) {
                                    "Aprobado" -> AdminSelectedText
                                    "Rechazado" -> Color(0xFF991B1B)
                                    else -> Color(0xFFD97706)
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = AdminBorderLight)

                    // Detalles en 2 columnas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Columna 1: Datos Institucionales
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailItem(label = "RUC / ID Fiscal", value = req.rfc, icon = Icons.Default.Badge)
                            DetailItem(label = "Dirección Registrada", value = req.address, icon = Icons.Default.LocationOn)
                            DetailItem(label = "Correo Administrador", value = req.email, icon = Icons.Default.Email)
                        }

                        // Columna 2: Datos Responsable Médico / Operador
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailItem(label = "Solicitante Responsable", value = req.rep, icon = Icons.Default.Person)
                            DetailItem(label = "Cédula / Licencia", value = req.licenseNumber, icon = Icons.Default.Verified)
                            DetailItem(label = "Teléfono de Contacto", value = req.phone, icon = Icons.Default.Phone)
                        }
                    }

                    HorizontalDivider(color = AdminBorderLight)

                    // Acciones del modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedRequestForDetail = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cerrar", fontSize = 13.sp, color = AdminTextPrimary)
                        }

                        if (req.status == "Pendiente") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        updateStatus(req, "Rechazado")
                                        selectedRequestForDetail = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Text("Rechazar Solicitud", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        updateStatus(req, "Aprobado")
                                        selectedRequestForDetail = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen)
                                ) {
                                    Text("Aprobar Habilitación", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AdminOffWhiteBg)
                .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AdminTextMuted, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(text = label, fontSize = 11.sp, color = AdminTextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
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

fun main() = singleWindowApplication(title = "Preview - Superadmin Dashboard") {
    SuperadminDashboardScreen()
}
