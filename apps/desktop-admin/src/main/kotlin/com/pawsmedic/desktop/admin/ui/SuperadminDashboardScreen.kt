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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val licenseNumber: String = ""
)

@Serializable
private data class AdminAuditLogDto(
    @SerialName("actor_id") val actorId: String? = null,
    @SerialName("action") val action: String,
    @SerialName("entity_type") val entityType: String,
    @SerialName("entity_id") val entityId: String? = null,
    @SerialName("created_at") val createdAt: String
)

data class AuditLog(
    val timestamp: String,
    val actorId: String,
    val action: String,
    val entityType: String,
    val entityId: String
)

@Composable
fun SuperadminDashboardScreen(
    userEmail: String = "",
    onLogout: () -> Unit = {},
    supabaseClient: SupabaseClient = supabase
) {
    var selectedModule by remember { mutableStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }

    val requests = remember { mutableStateListOf<RegistrationRequest>() }
    val auditLogs = remember { mutableStateListOf<AuditLog>() }
    var auditLoadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(supabaseClient) {
        try {
            val rows = try {
                supabaseClient.postgrest["audit_logs"].select().decodeList<AdminAuditLogDto>()
            } catch (_: Exception) {
                try { listOf(supabaseClient.postgrest["audit_logs"].select().decodeSingle<AdminAuditLogDto>()) } catch (_: Exception) { emptyList() }
            }
            auditLogs.clear()
            auditLogs.addAll(rows.map { item ->
                AuditLog(item.createdAt, item.actorId ?: "sistema", item.action, item.entityType, item.entityId ?: "—")
            })
        } catch (e: Exception) {
            auditLoadError = e.message ?: "No se pudo cargar la auditoría."
        }
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
                            0 -> "Supervisión de veterinarias"
                            1 -> "Solicitudes de incorporación veterinaria"
                            else -> "Auditoría administrativa"
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
                                            text = "Buscar veterinaria, solicitante o RFC...",
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
                        0 -> MonitoreoModule(supabaseClient)
                        1 -> SolicitudesModule(requests, supabaseClient, searchQuery)
                        2 -> AuditoriaModule(auditLogs, auditLoadError)
                    }
                }
            }
        }
    }
}

// MÓDULO 1: MONITOREO
@Composable
fun MonitoreoModule(supabaseClient: SupabaseClient) {
    val businesses = remember { mutableStateListOf<BusinessApplicationDto>() }
    var isLoading by remember { mutableStateOf(true) }
    var busyBusinessId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(supabaseClient) {
        try {
            businesses.clear()
            val list = try {
                supabaseClient.postgrest["businesses"].select().decodeList<BusinessApplicationDto>()
            } catch (_: Exception) {
                try { listOf(supabaseClient.postgrest["businesses"].select().decodeSingle<BusinessApplicationDto>()) } catch (_: Exception) { emptyList() }
            }
            businesses.addAll(
                list.filter { it.status == "APPROVED" || it.status == "SUSPENDED" }
            )
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudieron cargar las veterinarias."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val activeCount = businesses.count { it.status == "APPROVED" }
        val suspendedCount = businesses.count { it.status == "SUSPENDED" }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(
                "Veterinarias activas" to activeCount,
                "Veterinarias suspendidas" to suspendedCount
            ).forEach { (label, count) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AdminOffWhiteBg)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(label, fontSize = 13.sp, color = AdminTextSecondary)
                    Text(count.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                }
            }
        }

        Text("Supervisión de veterinarias", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
        errorMessage?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
        if (isLoading) CircularProgressIndicator(color = AdminEmeraldGreen)
        if (!isLoading && businesses.isEmpty() && errorMessage == null) {
            Text("No hay veterinarias aprobadas para mostrar.", color = AdminTextSecondary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(businesses) { business ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdminOffWhiteBg)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(business.publicName, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                        Text("Estado: ${business.status}", fontSize = 12.sp, color = AdminTextSecondary)
                    }
                    val nextStatus = if (business.status == "APPROVED") "SUSPENDED" else "APPROVED"
                    Button(
                        enabled = busyBusinessId == null,
                        onClick = {
                            coroutineScope.launch {
                                busyBusinessId = business.id
                                errorMessage = null
                                try {
                                    supabaseClient.postgrest.rpc(
                                        "set_business_status",
                                        buildJsonObject {
                                            put("p_business_id", business.id)
                                            put("p_status", nextStatus)
                                        }
                                    )
                                    val index = businesses.indexOf(business)
                                    if (index >= 0) businesses[index] = business.copy(status = nextStatus)
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "No se pudo cambiar el estado."
                                } finally {
                                    busyBusinessId = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen)
                    ) {
                        Text(if (nextStatus == "SUSPENDED") "Suspender" else "Reactivar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolicitudesModule(
    requests: MutableList<RegistrationRequest>,
    supabaseClient: SupabaseClient,
    searchQuery: String
) {
    var selectedRequestForDetail by remember { mutableStateOf<RegistrationRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val visibleRequests = requests.filter { request ->
        searchQuery.isBlank() || listOf(
            request.id, request.name, request.rep, request.rfc, request.email
        ).any { it.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(supabaseClient) {
        try {
            val fetchedApps = try {
                supabaseClient.postgrest["businesses"].select().decodeList<BusinessApplicationDto>()
            } catch (_: Exception) {
                try { listOf(supabaseClient.postgrest["businesses"].select().decodeSingle<BusinessApplicationDto>()) } catch (_: Exception) { emptyList() }
            }
            requests.clear()
            requests.addAll(fetchedApps.map { dto ->
                RegistrationRequest(
                    id = dto.id,
                    name = dto.publicName,
                    rep = dto.applicantName,
                    rfc = dto.taxId,
                    date = dto.createdAt?.substringBefore("T") ?: "—",
                    status = when (dto.status) {
                        "APPROVED" -> "Aprobado"
                        "REJECTED" -> "Rechazado"
                        "SUSPENDED" -> "Suspendido"
                        else -> "Pendiente"
                    },
                    email = dto.email,
                    phone = dto.phone,
                    address = dto.address,
                    licenseNumber = dto.licenseNumber
                )
            })
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudieron cargar las solicitudes."
        } finally {
            isLoading = false
        }
    }

    fun updateStatus(req: RegistrationRequest, newStatus: String) {
        coroutineScope.launch {
            isActionLoading = true
            errorMessage = null
            try {
                val parameters = buildJsonObject {
                    put("p_business_id", req.id)
                    if (newStatus == "Rechazado") put("p_reason", "")
                }
                val function = if (newStatus == "Aprobado") {
                    "approve_business_application"
                } else {
                    "reject_business_application"
                }
                supabaseClient.postgrest.rpc(function, parameters)
                val idx = requests.indexOf(req)
                if (idx >= 0) requests[idx] = req.copy(status = newStatus)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo guardar la decisión."
            } finally {
                isActionLoading = false
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
                    Text("Revisa los datos y decide la incorporación de cada clínica.", fontSize = 12.sp, color = AdminTextSecondary)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val fetchedApps = try {
                                    supabaseClient.postgrest["businesses"].select().decodeList<BusinessApplicationDto>()
                                } catch (_: Exception) {
                                    try { listOf(supabaseClient.postgrest["businesses"].select().decodeSingle<BusinessApplicationDto>()) } catch (_: Exception) { emptyList() }
                                }
                                requests.clear()
                                requests.addAll(fetchedApps.map { dto ->
                                    RegistrationRequest(
                                        id = dto.id,
                                        name = dto.publicName,
                                        rep = dto.applicantName,
                                        rfc = dto.taxId,
                                        date = dto.createdAt?.substringBefore("T") ?: "—",
                                        status = when (dto.status) {
                                            "APPROVED" -> "Aprobado"
                                            "REJECTED" -> "Rechazado"
                                            "SUSPENDED" -> "Suspendido"
                                            else -> "Pendiente"
                                        },
                                        email = dto.email,
                                        phone = dto.phone,
                                        address = dto.address,
                                        licenseNumber = dto.licenseNumber
                                    )
                                })
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "No se pudieron cargar las solicitudes."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refrescar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = AdminEmeraldGreen)
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
                Text("NOMBRE VETERINARIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.28f))
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
                if (!isLoading && visibleRequests.isEmpty() && errorMessage == null) {
                    item {
                        Text(
                            "No hay solicitudes de veterinarias para mostrar.",
                            color = AdminTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                items(visibleRequests) { req ->
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
                                        .clickable(enabled = !isActionLoading) { updateStatus(req, "Aprobado") },
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
                                        .clickable(enabled = !isActionLoading) { updateStatus(req, "Rechazado") },
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
                                    imageVector = Icons.Default.Store,
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
fun AuditoriaModule(logs: List<AuditLog>, loadError: String? = null) {
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
            loadError?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
            if (logs.isEmpty() && loadError == null) {
                Text("No hay eventos de auditoría para mostrar.", fontSize = 13.sp, color = AdminTextSecondary)
            }

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
                Text("FECHA & HORA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.20f))
                Text("ACTOR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.22f))
                Text("ACCIÓN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.25f))
                Text("ENTIDAD", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.16f))
                Text("ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.17f))
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
                        Text(log.timestamp, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.20f))
                        Text(log.actorId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, modifier = Modifier.weight(0.22f))
                        Text(log.action, fontSize = 12.sp, color = AdminTextSecondary, modifier = Modifier.weight(0.25f))
                        Text(log.entityType, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.16f))
                        Text(log.entityId, fontSize = 11.sp, color = AdminTextMuted, modifier = Modifier.weight(0.17f))
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
