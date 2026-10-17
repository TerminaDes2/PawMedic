package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
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

@Serializable
private data class CatalogService(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    val name: String,
    val description: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("price_cents") val priceCents: Int,
    val active: Boolean
)

@Serializable
private data class CatalogProduct(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    val sku: String,
    val name: String,
    val description: String = "",
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int,
    val active: Boolean,
    val published: Boolean
)

@Serializable
private data class NewCatalogService(
    @SerialName("tenant_id") val tenantId: String,
    val name: String,
    val description: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("price_cents") val priceCents: Int,
    val active: Boolean = true
)

@Serializable
private data class NewCatalogProduct(
    @SerialName("tenant_id") val tenantId: String,
    val sku: String,
    val name: String,
    val description: String,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int,
    val active: Boolean = true,
    val published: Boolean = false
)

@Composable
fun CatalogScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Servicios") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var services by remember { mutableStateOf<List<CatalogService>>(emptyList()) }
    var products by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var stock by remember { mutableStateOf("0") }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadCatalog() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error("No hay una sesión de Supabase activa. Inicia sesión nuevamente.")
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) error("El catálogo solo está disponible para cuentas veterinarias.")
            val clinicId = profile.tenantId ?: error("El perfil no tiene una clínica asignada.")
            tenantId = clinicId
            services = supabase.postgrest["services"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<CatalogService>()
            products = supabase.postgrest["products"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<CatalogProduct>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudo cargar el catálogo desde Supabase."
        } finally {
            isLoading = false
        }
    }

    suspend fun saveNewItem() {
        val clinicId = tenantId ?: run { errorMessage = "No hay una clínica autorizada."; return }
        val priceCents = ((price.trim().replace(",", ".").toBigDecimalOrNull() ?: run {
            errorMessage = "Ingresa un precio válido no negativo."; return
        }) * java.math.BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).toInt()
        if (priceCents < 0) { errorMessage = "El precio no puede ser negativo."; return }
        val cleanName = name.trim()
        if (cleanName.isBlank()) { errorMessage = "El nombre es obligatorio."; return }
        isSaving = true
        errorMessage = null
        try {
            if (selectedTab == "Servicios") {
                val minutes = duration.toIntOrNull()?.takeIf { it > 0 }
                    ?: error("La duración debe ser un entero mayor que cero.")
                supabase.postgrest["services"].insert(
                    NewCatalogService(clinicId, cleanName, description.trim(), minutes, priceCents)
                )
            } else {
                val cleanSku = sku.trim()
                if (cleanSku.isBlank()) error("El SKU es obligatorio para los productos.")
                val quantity = stock.toIntOrNull()?.takeIf { it >= 0 }
                    ?: error("La existencia debe ser un entero no negativo.")
                supabase.postgrest["products"].insert(
                    NewCatalogProduct(clinicId, cleanSku, cleanName, description.trim(), priceCents, quantity)
                )
            }
            showCreateDialog = false
            name = ""
            sku = ""
            description = ""
            price = ""
            duration = "30"
            stock = "0"
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudo guardar el elemento en Supabase."
        } finally {
            isSaving = false
        }
    }

    suspend fun toggleActive(id: String, current: Boolean, table: String) {
        val clinicId = tenantId ?: return
        errorMessage = null
        try {
            val updated = supabase.postgrest[table].update({ set("active", !current) }) {
                filter {
                    eq("id", id)
                    eq("tenant_id", clinicId)
                }
                select()
            }.decodeList<JsonObject>()
            if (updated.isEmpty()) error("No se actualizó ningún elemento. Actualiza el catálogo e inténtalo de nuevo.")
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudo actualizar el estado del elemento."
        }
    }

    suspend fun togglePublished(product: CatalogProduct) {
        val clinicId = tenantId ?: return
        errorMessage = null
        try {
            val updated = supabase.postgrest["products"].update({ set("published", !product.published) }) {
                filter {
                    eq("id", product.id)
                    eq("tenant_id", clinicId)
                }
                select()
            }.decodeList<JsonObject>()
            if (updated.isEmpty()) error("No se actualizó el producto. Actualiza el catálogo e inténtalo de nuevo.")
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudo actualizar la publicación del producto."
        }
    }

    LaunchedEffect(Unit) { loadCatalog() }

    VetAppLayout(
        currentScreen = "Catalog",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Catálogo de Servicios y Productos",
        userEmail = userEmail
    ) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Catálogo de la clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Servicios y productos guardados en Supabase para la clínica de la sesión actual.", fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { coroutineScope.launch { loadCatalog() } }, enabled = !isLoading) { Text("Actualizar") }
                    Button(onClick = { showCreateDialog = true }, enabled = tenantId != null && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Nuevo")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Servicios", "Productos").forEach { tab ->
                    FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(tab) })
                }
            }
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp)).border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                if (selectedTab == "Servicios") {
                    Text("SERVICIO / DESCRIPCIÓN", Modifier.weight(0.42f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("DURACIÓN", Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("PRECIO", Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                } else {
                    Text("SKU / PRODUCTO", Modifier.weight(0.38f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("EXISTENCIAS", Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("PRECIO", Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("PUBLICADO", Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                Text("ESTADO", Modifier.weight(0.14f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            when {
                isLoading -> Text("Cargando catálogo…", color = TextSecondary, modifier = Modifier.padding(12.dp))
                selectedTab == "Servicios" && services.isEmpty() -> Text("No hay servicios registrados.", color = TextSecondary, modifier = Modifier.padding(12.dp))
                selectedTab == "Productos" && products.isEmpty() -> Text("No hay productos registrados.", color = TextSecondary, modifier = Modifier.padding(12.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                if (selectedTab == "Servicios") items(services, key = { it.id }) { service ->
                    Row(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(7.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(0.42f)) { Text(service.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary); if (service.description.isNotBlank()) Text(service.description, fontSize = 11.sp, color = TextSecondary) }
                        Text("${service.durationMinutes} min", Modifier.weight(0.16f), fontSize = 12.sp, color = TextSecondary)
                        Text(formatPrice(service.priceCents), Modifier.weight(0.16f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        TextButton(onClick = { coroutineScope.launch { toggleActive(service.id, service.active, "services") } }) { Text(if (service.active) "Activo" else "Inactivo", color = if (service.active) Color(0xFF166534) else Color(0xFF991B1B)) }
                    }
                }
                else items(products, key = { it.id }) { product ->
                    Row(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(7.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(0.38f)) { Text(product.sku, fontSize = 10.sp, color = TextMuted); Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary) }
                        Text(product.stockQuantity.toString(), Modifier.weight(0.16f), fontSize = 12.sp, color = TextSecondary)
                        Text(formatPrice(product.priceCents), Modifier.weight(0.16f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        TextButton(onClick = { coroutineScope.launch { togglePublished(product) } }) {
                            Text(if (product.published) "Sí" else "No", fontSize = 12.sp, color = if (product.published) Color(0xFF166534) else TextMuted)
                        }
                        TextButton(onClick = { coroutineScope.launch { toggleActive(product.id, product.active, "products") } }) { Text(if (product.active) "Activo" else "Inactivo", color = if (product.active) Color(0xFF166534) else Color(0xFF991B1B)) }
                    }
                }
            }
        }

        if (showCreateDialog) Dialog(onDismissRequest = { if (!isSaving) showCreateDialog = false }) {
            Card(Modifier.width(480.dp).wrapContentHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Nuevo ${if (selectedTab == "Servicios") "servicio" else "producto"}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { if (!isSaving) showCreateDialog = false }) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                    }
                    if (selectedTab == "Productos") OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(price, { price = it }, label = { Text("Precio MXN") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (selectedTab == "Servicios") OutlinedTextField(duration, { duration = it }, label = { Text("Duración (minutos)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    else {
                        OutlinedTextField(stock, { stock = it }, label = { Text("Existencias") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Text("Los productos nuevos quedan sin publicar. El estado de publicación puede cambiarse desde el listado.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showCreateDialog = false }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = { coroutineScope.launch { saveNewItem() } }, enabled = !isSaving, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)) { Text(if (isSaving) "Guardando…" else "Guardar", color = Color.White) }
                    }
                }
            }
        }
    }
}

private fun formatPrice(cents: Int): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')} MXN"
