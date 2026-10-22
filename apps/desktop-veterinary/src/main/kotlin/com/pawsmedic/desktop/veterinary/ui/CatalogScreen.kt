package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.model.formattedPrice
import com.pawsmedic.features.products.domain.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    repository: ProductRepository,
    tenantId: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var products by remember(tenantId) { mutableStateOf<List<Product>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            isLoading = true
            message = null
            try {
                products = repository.getProductsByTenant(tenantId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                message = error.message ?: "No se pudo cargar el catálogo."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(tenantId) {
        reload()
    }

    VetAppLayout(
        currentScreen = "Catalog",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Catálogo de productos"
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Agregar producto")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    placeholder = { Text("250.50") },
                    singleLine = true
                )
                Button(
                    enabled = !isSaving && name.isNotBlank(),
                    onClick = {
                        val priceCents = parsePriceCents(price)
                        if (priceCents == null) {
                            message = "El precio debe ser un número válido, por ejemplo 250.50."
                        } else {
                            scope.launch {
                                isSaving = true
                                message = null
                                try {
                                    repository.addProduct(
                                        Product(
                                            tenantId = tenantId,
                                            name = name.trim(),
                                            description = description.trim(),
                                            priceCents = priceCents
                                        ),
                                        tenantId
                                    )
                                    name = ""
                                    description = ""
                                    price = ""
                                    products = repository.getProductsByTenant(tenantId)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    message = error.message ?: "No se pudo guardar el producto."
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSaving) "Guardando..." else "Agregar")
                }
            }

            message?.let { Text(it) }
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> CircularProgressIndicator()
                products.isEmpty() -> Text("No hay productos registrados.")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id ?: "${it.tenantId}-${it.name}" }) { product ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(product.name)
                            Text(product.description.orEmpty())
                            Text(product.formattedPrice())
                        }
                    }
                }
            }
        }
    }
}

private fun parsePriceCents(value: String): Int? {
    val normalized = value.trim().replace(',', '.')
    val parts = normalized.split('.')
    if (parts.size > 2 || parts.any { it.isEmpty() && parts.size == 1 }) return null
    val whole = parts.firstOrNull()?.toLongOrNull() ?: return null
    val fraction = when (parts.getOrNull(1)?.length ?: 0) {
        0 -> 0
        1 -> parts[1].toIntOrNull()?.times(10) ?: return null
        2 -> parts[1].toIntOrNull() ?: return null
        else -> return null
    }
    val cents = whole * 100 + fraction
    return if (cents <= Int.MAX_VALUE) cents.toInt() else null
}
