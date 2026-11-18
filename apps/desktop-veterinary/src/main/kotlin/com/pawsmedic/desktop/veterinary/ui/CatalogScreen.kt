package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.admin.translation.translateBatchLocally

import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID
import java.net.URI
import javax.imageio.ImageIO

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
    @Serializable(with = CatalogIdSerializer::class)
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    val sku: String? = null,
    val name: String,
    val description: String = "",
    @SerialName("type_id") val typeId: Int? = null,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int,
    val active: Boolean,
    val published: Boolean,
    @SerialName("image_url") val imageUrl: String = ""
)

@Serializable
internal data class ProductCategory(val id: Int, val name: String)

internal fun productRequirementsMet(categoryId: Int?, categories: List<ProductCategory>, hasImage: Boolean): Boolean =
    categoryId != null && categories.any { it.id == categoryId } && hasImage

private object CatalogIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CatalogId", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): String =
        if (decoder is JsonDecoder) decoder.decodeJsonElement().jsonPrimitive.content else decoder.decodeString()
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

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
    @SerialName("type_id") val typeId: Int,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("stock_quantity") val stockQuantity: Int,
    @SerialName("image_url") val imageUrl: String,
    val active: Boolean = true,
    val published: Boolean = false
)

private val LocalCatalogTranslations = staticCompositionLocalOf { mutableStateMapOf<String, String>() }

@Composable
internal fun CatalogTranslationProvider(sessionKey: String, content: @Composable () -> Unit) {
    val translations = remember(sessionKey) { mutableStateMapOf<String, String>() }
    CompositionLocalProvider(LocalCatalogTranslations provides translations, content = content)
}

@Composable
fun CatalogScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    var selectedTab by remember { mutableStateOf("Servicios") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var detailProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var detailImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var selectedPhoto by remember { mutableStateOf<SelectedPhoto?>(null) }
    var services by remember { mutableStateOf<List<CatalogService>>(emptyList()) }
    var products by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }
    var categoriesError by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val translations = LocalCatalogTranslations.current
    var translationError by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var showOriginal by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }
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

    val catalogTexts = remember(services, products, categories, selectedTab) {
        if (selectedTab == "Servicios") services.flatMap { listOf(it.name, it.description) }
        else products.flatMap { listOf(it.name, it.description) } + categories.map { it.name }
    }
    LaunchedEffect(english, catalogTexts, translationAttempt) {
        if (!english) {
            translating = false
            translationError = false
            return@LaunchedEffect
        }
        val untranslated = catalogTexts.filter { it.isNotBlank() && !translations.containsKey(it) }.distinct()
        val pending = untranslated.filter { it.length <= 4000 }
        translationError = pending.size != untranslated.size
        if (pending.isNotEmpty()) {
            translating = true
            try {
                for (batch in pending.chunked(50)) {
                    val results = translateBatchLocally(batch)
                    batch.zip(results).forEach { (original, translated) -> translations[original] = translated }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                translationError = true
            } finally {
                translating = false
            }
        }
    }
    fun displayText(original: String): String =
        if (english && !showOriginal) translations[original] ?: original else original

    suspend fun loadCatalog() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error(tr("No hay una sesión de Supabase activa. Inicia sesión nuevamente.", "There is no active Supabase session. Please sign in again."))
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) error(tr("El catálogo solo está disponible para cuentas veterinarias.", "The catalog is only available to veterinary accounts."))
            val clinicId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
            tenantId = clinicId
            services = supabase.postgrest["services"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<CatalogService>()
            products = supabase.postgrest["products"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<CatalogProduct>()
            try {
                categories = supabase.postgrest["product_types"].select().decodeList<ProductCategory>().sortedBy { it.name }
                categoriesError = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                categories = emptyList()
                categoriesError = true
                System.err.println("No se pudieron cargar las categorías de productos: ${e.javaClass.simpleName}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = tr("No se pudo cargar el catálogo. Inténtalo de nuevo.", "Could not load the catalog. Please try again.")
        } finally {
            isLoading = false
        }
    }

    suspend fun saveNewItem() {
        val clinicId = tenantId ?: run { errorMessage = tr("No hay una clínica autorizada.", "No clinic is authorized."); return }
        val amount = price.trim().replace(",", ".").toBigDecimalOrNull() ?: run {
            errorMessage = tr("Ingresa un precio válido no negativo.", "Enter a valid non-negative price."); return
        }
        val priceCents = try {
            require(amount.signum() >= 0)
            amount.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).intValueExact()
        } catch (e: ArithmeticException) {
            errorMessage = tr("El precio excede el límite permitido.", "The price exceeds the allowed limit."); return
        } catch (e: IllegalArgumentException) {
            errorMessage = tr("El precio no puede ser negativo.", "The price cannot be negative."); return
        }
        val cleanName = name.trim()
        if (cleanName.isBlank()) { errorMessage = tr("El nombre es obligatorio.", "Name is required."); return }
        val minutes = duration.toIntOrNull()?.takeIf { it > 0 }
        val cleanSku = sku.trim()
        val quantity = stock.toIntOrNull()?.takeIf { it >= 0 }
        val categoryId = selectedCategoryId
        if (selectedTab == "Servicios" && minutes == null) {
            errorMessage = tr("La duración debe ser un entero mayor que cero.", "Duration must be a positive whole number."); return
        }
        if (selectedTab != "Servicios") {
            if (cleanSku.isBlank()) { errorMessage = tr("El SKU es obligatorio para los productos.", "SKU is required for products."); return }
            if (quantity == null) { errorMessage = tr("La existencia debe ser un entero no negativo.", "Stock must be a non-negative whole number."); return }
            if (!productRequirementsMet(categoryId, categories, selectedPhoto != null || !editingProduct?.imageUrl.isNullOrBlank())) {
                errorMessage = tr("Selecciona una categoría disponible y una imagen para el producto.", "Select an available category and an image for the product."); return
            }
        }
        isSaving = true
        errorMessage = null
        try {
            if (selectedTab == "Servicios") {
                supabase.postgrest["services"].insert(
                    NewCatalogService(clinicId, cleanName, description.trim(), checkNotNull(minutes), priceCents)
                )
            } else {
                checkNotNull(categoryId)
                checkNotNull(quantity)
                val imageUrl = selectedPhoto?.let { photo ->
                    val bucket = supabase.storage.from("product-images")
                    val key = "$clinicId/${UUID.randomUUID()}.${photo.extension}"
                    bucket.upload(key, photo.bytes)
                    bucket.publicUrl(key)
                } ?: editingProduct?.imageUrl.orEmpty()
                val existing = editingProduct
                if (existing == null) {
                    supabase.postgrest["products"].insert(
                        NewCatalogProduct(clinicId, cleanSku, cleanName, description.trim(), categoryId, priceCents, quantity, imageUrl)
                    )
                } else {
                    val updated = supabase.postgrest["products"].update({
                        set("sku", cleanSku)
                        set("name", cleanName)
                        set("description", description.trim())
                        set("type_id", categoryId)
                        set("price_cents", priceCents)
                        set("stock_quantity", quantity)
                        set("image_url", imageUrl)
                    }) {
                        filter { eq("id", existing.id); eq("tenant_id", clinicId) }
                        select()
                    }.decodeList<JsonObject>()
                    check(updated.size == 1) { tr("No se pudo actualizar el producto.", "Could not update the product.") }
                }
            }
            showCreateDialog = false
            name = ""
            sku = ""
            description = ""
            price = ""
            duration = "30"
            stock = "0"
            selectedPhoto = null
            selectedCategoryId = null
            editingProduct = null
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = tr("No se pudo guardar el elemento. Inténtalo de nuevo.", "Could not save the item. Please try again.")
        } finally {
            isSaving = false
        }
    }

    suspend fun toggleActive(id: String, current: Boolean, table: String) {
        val clinicId = tenantId ?: return
        if (isSaving) return
        isSaving = true
        errorMessage = null
        try {
            val updated = supabase.postgrest[table].update({ set("active", !current) }) {
                filter {
                    eq("id", id)
                    eq("tenant_id", clinicId)
                }
                select()
            }.decodeList<JsonObject>()
            if (updated.isEmpty()) error(tr("No se actualizó ningún elemento. Actualiza el catálogo e inténtalo de nuevo.", "No item was updated. Refresh the catalog and try again."))
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = tr("No se pudo actualizar el estado del elemento.", "Could not update the item status.")
        } finally {
            isSaving = false
        }
    }

    suspend fun selectAllServices(active: Boolean) {
        val clinicId = tenantId ?: return
        if (isSaving) return
        isSaving = true
        errorMessage = null
        try {
            val updated = supabase.postgrest["services"].update({ set("active", active) }) {
                filter { eq("tenant_id", clinicId) }
                select()
            }.decodeList<CatalogService>()
            check(updated.size == services.size) {
                tr("No se pudieron actualizar todos los servicios. Actualiza el catálogo y vuelve a intentarlo.", "Could not update all services. Refresh the catalog and try again.")
            }
            services = updated.sortedBy { it.name }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = tr("No se pudieron actualizar los servicios.", "Could not update the services.")
        } finally {
            isSaving = false
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
            if (updated.isEmpty()) error(tr("No se actualizó el producto. Actualiza el catálogo e inténtalo de nuevo.", "The product was not updated. Refresh the catalog and try again."))
            loadCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = tr("No se pudo actualizar la publicación del producto.", "Could not update the product publication status.")
        }
    }

    LaunchedEffect(Unit) { loadCatalog() }
    LaunchedEffect(detailProduct?.id, detailProduct?.imageUrl) {
        detailImage = null
        imageError = null
        val product = detailProduct ?: return@LaunchedEffect
        if (product.imageUrl.isBlank()) return@LaunchedEffect
        try {
            val prefix = supabase.storage.from("product-images").publicUrl("${product.tenantId}/")
            require(product.imageUrl.startsWith(prefix)) { "La imagen no pertenece al catálogo de la clínica." }
            detailImage = withContext(Dispatchers.IO) {
                val uri = URI(product.imageUrl)
                require(uri.scheme == "https") { "La imagen debe usar HTTPS." }
                val connection = uri.toURL().openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val bytes = connection.getInputStream().use { it.readNBytes(5_000_001) }
                require(bytes.size <= 5_000_000) { "La imagen supera 5 MB." }
                val stream = ImageIO.createImageInputStream(bytes.inputStream())
                    ?: error("No se pudo leer la imagen.")
                stream.use {
                    val readers = ImageIO.getImageReaders(it)
                    require(readers.hasNext()) { "Imagen no válida." }
                    val reader = readers.next()
                    try {
                        reader.input = it
                        require(reader.getWidth(0) <= 4096 && reader.getHeight(0) <= 4096) {
                            "La imagen supera el tamaño permitido."
                        }
                        reader.read(0).toComposeImageBitmap()
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            imageError = tr("No se pudo mostrar la imagen del producto.", "Could not display the product image.")
        }
    }

    VetAppLayout(
        currentScreen = "Catalog",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Catálogo de Servicios y Productos",
        userEmail = userEmail
    ) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(CardWhite).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(tr("Catálogo de la clínica", "Clinic catalog"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(tr("Servicios y productos guardados en Supabase para la clínica de la sesión actual.", "Services and products for the current clinic."), fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { coroutineScope.launch { loadCatalog() } }, enabled = !isLoading) { Text(tr("Actualizar", "Refresh")) }
                    Button(onClick = {
                        editingProduct = null
                        selectedPhoto = null
                        selectedCategoryId = null
                        name = ""; sku = ""; description = ""; price = ""; stock = "0"; duration = "30"
                        errorMessage = null
                        showCreateDialog = true
                    }, enabled = tenantId != null && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text(tr("Nuevo", "New"))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (english) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Automatic translation; check the original.", color = TextSecondary, fontSize = 12.sp)
                    TextButton(onClick = { showOriginal = !showOriginal }) {
                        Text(if (showOriginal) "Show translation" else "Show original")
                    }
                    if (translating) Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                }
                if (translationError) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Some text could not be translated; the original is shown.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        TextButton(onClick = { translationAttempt++ }) { Text("Retry") }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Servicios", "Productos").forEach { tab ->
                    FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(if (english) if (tab == "Servicios") "Services" else "Products" else tab) })
                }
            }
            if (selectedTab == "Servicios") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = services.isNotEmpty() && services.all { it.active },
                        onCheckedChange = { selected -> coroutineScope.launch { selectAllServices(selected) } },
                        enabled = services.isNotEmpty() && !isLoading && !isSaving
                    )
                    Column {
                        Text(tr("Seleccionar todos", "Select all"), fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(tr("Solo los servicios seleccionados estarán disponibles para agendar en la app móvil.", "Only selected services can be booked in the mobile app."), fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp)).border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                if (selectedTab == "Servicios") {
                    Text(tr("SERVICIO / DESCRIPCIÓN", "SERVICE / DESCRIPTION"), Modifier.weight(0.42f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(tr("DURACIÓN", "DURATION"), Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(tr("PRECIO", "PRICE"), Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                } else {
                    Text(tr("SKU / PRODUCTO", "SKU / PRODUCT"), Modifier.weight(0.38f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(tr("EXISTENCIAS", "STOCK"), Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(tr("PRECIO", "PRICE"), Modifier.weight(0.16f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(tr("PUBLICADO", "PUBLISHED"), Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, textAlign = TextAlign.Center)
                }
                Text(if (selectedTab == "Servicios") tr("SELECCIONADO", "SELECTED") else tr("ESTADO", "STATUS"), Modifier.weight(0.14f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, textAlign = if (selectedTab == "Servicios") TextAlign.Start else TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            when {
                isLoading -> Text(tr("Cargando catálogo…", "Loading catalog…"), color = TextSecondary, modifier = Modifier.padding(12.dp))
                selectedTab == "Servicios" && services.isEmpty() -> Text(tr("No hay servicios registrados.", "No services registered."), color = TextSecondary, modifier = Modifier.padding(12.dp))
                selectedTab == "Productos" && products.isEmpty() -> Text(tr("No hay productos registrados.", "No products registered."), color = TextSecondary, modifier = Modifier.padding(12.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                if (selectedTab == "Servicios") items(services, key = { it.id }) { service ->
                    Row(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(7.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(0.42f)) {
                            Text(displayText(service.name), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            if (service.description.isNotBlank()) Text(displayText(service.description), fontSize = 11.sp, color = TextSecondary)
                        }
                        Text("${service.durationMinutes} min", Modifier.weight(0.16f), fontSize = 12.sp, color = TextSecondary)
                        Text(formatPrice(service.priceCents), Modifier.weight(0.16f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Row(Modifier.weight(0.14f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = service.active,
                                onCheckedChange = { coroutineScope.launch { toggleActive(service.id, service.active, "services") } },
                                enabled = !isLoading && !isSaving
                            )
                        }
                    }
                }
                else items(products, key = { it.id }) { product ->
                    Row(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(7.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(0.38f)) {
                            Text(product.sku.orEmpty(), fontSize = 10.sp, color = TextMuted)
                            Text(displayText(product.name), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            TextButton(onClick = { detailProduct = product }) { Text(tr("Ver detalle", "View details")) }
                        }
                        Text(product.stockQuantity.toString(), Modifier.weight(0.16f), fontSize = 12.sp, color = TextSecondary)
                        Text(formatPrice(product.priceCents), Modifier.weight(0.16f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Box(Modifier.weight(0.12f), contentAlignment = Alignment.Center) {
                            TextButton(
                                onClick = { coroutineScope.launch { togglePublished(product) } },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (product.published) tr("Sí", "Yes") else "No", fontSize = 12.sp, color = if (product.published) MaterialTheme.colorScheme.primary else TextMuted)
                            }
                        }
                        Box(Modifier.weight(0.14f), contentAlignment = Alignment.Center) {
                            TextButton(
                                onClick = { coroutineScope.launch { toggleActive(product.id, product.active, "products") } },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (product.active) tr("Activo", "Active") else tr("Inactivo", "Inactive"), color = if (product.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        detailProduct?.let { product ->
            AlertDialog(
                onDismissRequest = { detailProduct = null },
                title = { Text(displayText(product.name)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SKU: ${product.sku}")
                        Text("${tr("Categoría", "Category")}: ${categories.find { it.id == product.typeId }?.name?.let(::displayText) ?: tr("Sin categoría", "No category")}")
                        Text("${tr("Descripción", "Description")}: ${displayText(product.description).ifBlank { "—" }}")
                        if (english) {
                            Text("Automatic translation; check the original.", color = TextSecondary, fontSize = 11.sp)
                            TextButton(onClick = { showOriginal = !showOriginal }) {
                                Text(if (showOriginal) "Show translation" else "Show original")
                            }
                        }
                        Text("${tr("Precio", "Price")}: ${formatPrice(product.priceCents)}")
                        Text("${tr("Existencias", "Stock")}: ${product.stockQuantity}")
                        Text(if (product.active) tr("Disponible", "Available") else tr("No disponible", "Unavailable"))
                        Text(if (product.published) tr("Publicado", "Published") else tr("No publicado", "Unpublished"))
                        if (product.imageUrl.isBlank()) Text(tr("Sin imagen", "No image"))
                        detailImage?.let { Image(it, contentDescription = tr("Imagen del producto", "Product image"), modifier = Modifier.size(120.dp)) }
                        imageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        editingProduct = product
                        name = product.name; sku = product.sku.orEmpty(); description = product.description
                        selectedCategoryId = product.typeId
                        price = java.math.BigDecimal(product.priceCents).movePointLeft(2).toPlainString()
                        stock = product.stockQuantity.toString()
                        selectedPhoto = null
                        errorMessage = null
                        detailProduct = null
                        showCreateDialog = true
                    }) { Text(tr("Editar", "Edit")) }
                },
                dismissButton = { TextButton(onClick = { detailProduct = null }) { Text(tr("Cerrar", "Close")) } }
            )
        }

        if (showCreateDialog) Dialog(onDismissRequest = { if (!isSaving) showCreateDialog = false }) {
            Card(Modifier.width(480.dp).wrapContentHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.heightIn(max = 700.dp).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (editingProduct != null) tr("Editar producto", "Edit product") else if (selectedTab == "Servicios") tr("Nuevo servicio", "New service") else tr("Nuevo producto", "New product"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { if (!isSaving) showCreateDialog = false }) { Icon(Icons.Default.Close, contentDescription = tr("Cerrar", "Close")) }
                    }
                    if (selectedTab == "Productos") OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(name, { name = it }, label = { Text(tr("Nombre", "Name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text(tr("Descripción", "Description")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(price, { price = it }, label = { Text(tr("Precio MXN", "Price MXN")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (selectedTab == "Servicios") OutlinedTextField(duration, { duration = it }, label = { Text(tr("Duración (minutos)", "Duration (minutes)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    else {
                        OutlinedTextField(stock, { stock = it }, label = { Text(tr("Existencias", "Stock")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = categories.find { it.id == selectedCategoryId }?.name?.let(::displayText).orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isSaving && categories.isNotEmpty(),
                                singleLine = true,
                                label = { Text(tr("Categoría", "Category")) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Box(Modifier.matchParentSize().clickable(enabled = !isSaving && categories.isNotEmpty()) { categoryMenuExpanded = true })
                            DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                                categories.forEach { category ->
                                    DropdownMenuItem(text = { Text(displayText(category.name)) }, onClick = {
                                        selectedCategoryId = category.id
                                        categoryMenuExpanded = false
                                    })
                                }
                            }
                        }
                        if (categories.isEmpty()) {
                            Text(if (categoriesError) tr("No se pudieron cargar las categorías. Actualiza el catálogo.", "Could not load categories. Refresh the catalog.")
                            else tr("No hay categorías disponibles en Supabase.", "No categories are available in Supabase."),
                                color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = {
                            val file = chooseProfilePhoto(english)
                            if (file != null) coroutineScope.launch {
                                try {
                                    selectedPhoto = readProfilePhoto(file)
                                    errorMessage = null
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    errorMessage = tr("No se pudo leer la imagen. Selecciona un archivo JPG o PNG válido.", "Could not read the image. Select a valid JPG or PNG file.")
                                }
                            }
                        }, enabled = !isSaving) { Text(tr("Seleccionar imagen obligatoria (JPG o PNG)", "Choose required image (JPG or PNG)")) }
                        selectedPhoto?.let { Image(it.preview, contentDescription = tr("Imagen seleccionada", "Selected image"), modifier = Modifier.size(72.dp)) }
                        if (selectedPhoto == null && !editingProduct?.imageUrl.isNullOrBlank()) {
                            Text(tr("Se conservará la imagen actual.", "The current image will be kept."), fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(tr("Los productos nuevos quedan sin publicar. El estado de publicación puede cambiarse desde el listado.", "New products are unpublished. Change their publication status in the list."), fontSize = 11.sp, color = TextSecondary)
                    }
                    errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showCreateDialog = false }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text(tr("Cancelar", "Cancel")) }
                        Button(onClick = { coroutineScope.launch { saveNewItem() } }, enabled = !isSaving, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)) { Text(if (isSaving) tr("Guardando…", "Saving…") else tr("Guardar", "Save"), color = Color.White) }
                    }
                }
            }
        }
    }
}

private fun formatPrice(cents: Int): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')} MXN"
