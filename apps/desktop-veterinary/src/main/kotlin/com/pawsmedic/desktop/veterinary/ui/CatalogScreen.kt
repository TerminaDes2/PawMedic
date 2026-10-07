package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.veterinary.ui.theme.*

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.ui.window.singleWindowApplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class CatalogItem(
    val sku: String,
    val name: String,
    val category: String,
    val price: String,
    val duration: String,
    var status: String = "Activo"
)

@Composable
fun CatalogScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showNewServiceModal by remember { mutableStateOf(false) }

    var newSku by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Consultas") }
    var newPrice by remember { mutableStateOf("") }
    var newDuration by remember { mutableStateOf("30 min") }

    val catalogItems = remember {
        mutableStateListOf(
            CatalogItem("SRV-001", "Consulta General Veterinaria", "Consultas", "$450.00 MXN", "30 min", "Activo"),
            CatalogItem("SRV-002", "Vacunación Sextuple Canina", "Vacunas", "$600.00 MXN", "15 min", "Activo"),
            CatalogItem("SRV-003", "Limpieza Dental Ultrasonido", "Cirugías", "$1,800.00 MXN", "90 min", "Activo"),
            CatalogItem("SRV-004", "Profilaxis & Cirugía Mayor", "Cirugías", "$3,500.00 MXN", "120 min", "Activo"),
            CatalogItem("SRV-005", "Baño Medicado & Corte Higiénico", "Estética", "$350.00 MXN", "45 min", "Activo"),
            CatalogItem("SRV-006", "Hemograma Completo + Bioquímica", "Laboratorio", "$850.00 MXN", "24 hrs", "Activo"),
            CatalogItem("SRV-007", "Radiografía Digital (2 Vistas)", "Diagnóstico", "$950.00 MXN", "30 min", "Activo")
        )
    }

    VetAppLayout(
        currentScreen = "Catalog",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Catálogo de Servicios y Tarifas",
        userEmail = userEmail
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Listado de Servicios Vigentes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Catálogo público y precios autorizados para consulta y agendamiento.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen)
                            .clickable { showNewServiceModal = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Nuevo Servicio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Categorías
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Todos", "Consultas", "Cirugías", "Vacunas", "Estética", "Laboratorio", "Diagnóstico").forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) EmeraldGreen else OffWhiteBg)
                                .border(1.dp, if (isSel) EmeraldGreen else BorderLight, RoundedCornerShape(6.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Encabezados Tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OffWhiteBg)
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SKU", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.15f))
                    Text("NOMBRE DEL SERVICIO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.35f))
                    Text("CATEGORÍA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.15f))
                    Text("PRECIO BASE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.15f))
                    Text("DURACIÓN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.10f))
                    Text("ESTADO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.10f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = catalogItems.filter {
                    selectedCategory == "Todos" || it.category == selectedCategory
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.sku, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMuted, modifier = Modifier.weight(0.15f))
                            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.35f))
                            Text(item.category, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(0.15f))
                            Text(item.price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.weight(0.15f))
                            Text(item.duration, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(0.10f))
                            Box(
                                modifier = Modifier
                                    .weight(0.10f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (item.status == "Activo") Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .clickable {
                                        val idx = catalogItems.indexOf(item)
                                        if (idx != -1) {
                                            catalogItems[idx] = item.copy(status = if (item.status == "Activo") "Inactivo" else "Activo")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (item.status == "Activo") Color(0xFF166534) else Color(0xFF991B1B))
                            }
                        }
                    }
                }
            }
        }

        // MODAL DE AGREGAR NUEVO SERVICIO
        if (showNewServiceModal) {
            Dialog(onDismissRequest = { showNewServiceModal = false }) {
                Card(
                    modifier = Modifier
                        .width(480.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Agregar Nuevo Servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            IconButton(onClick = { showNewServiceModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                            }
                        }

                        HorizontalDivider(color = BorderLight)

                        OutlinedTextField(
                            value = newSku,
                            onValueChange = { newSku = it },
                            label = { Text("Código / SKU") },
                            placeholder = { Text("ej. SRV-008") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nombre del Servicio") },
                            placeholder = { Text("ej. Ultrasonido Abdominal") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = { newPrice = it },
                            label = { Text("Precio Base ($ MXN)") },
                            placeholder = { Text("ej. $750.00 MXN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showNewServiceModal = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    if (newName.isNotBlank() && newPrice.isNotBlank()) {
                                        catalogItems.add(
                                            CatalogItem(
                                                sku = newSku.ifBlank { "SRV-00${catalogItems.size + 1}" },
                                                name = newName.trim(),
                                                category = newCategory,
                                                price = if (newPrice.contains("$")) newPrice.trim() else "$$newPrice MXN",
                                                duration = newDuration,
                                                status = "Activo"
                                            )
                                        )
                                        showNewServiceModal = false
                                        newSku = ""
                                        newName = ""
                                        newPrice = ""
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                            ) {
                                Text("Guardar Servicio", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CatalogScreenPreview() {
    CatalogScreen(onNavigate = {})
}

fun main() = singleWindowApplication(title = "Preview - Catálogo de Servicios") {
    CatalogScreen(onNavigate = {})
}
