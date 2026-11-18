package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.model.formattedPrice
import com.pawsmedic.features.products.domain.repository.ProductRepository
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

private val productCategories = listOf(
    "Todos",
    "Alimentos",
    "Medicamentos",
    "Estética e higiene",
    "Accesorios",
    "Material veterinario"
)

@Composable
fun ProductCatalogScreen(
    clinicName: String,
    tenantId: String,
    productRepository: ProductRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var products by remember(tenantId) { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember(tenantId) { mutableStateOf(true) }
    var errorMessage by remember(tenantId) { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    LaunchedEffect(tenantId, productRepository) {
        isLoading = true
        errorMessage = null
        try {
            products = productRepository.getProductsByTenant(tenantId)
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = error.message ?: "No se pudo cargar el catálogo."
        } finally {
            isLoading = false
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesQuery = query.isBlank() ||
            product.name.contains(query, ignoreCase = true) ||
            product.description.orEmpty().contains(query, ignoreCase = true)
        val matchesCategory = selectedCategory == "Todos" ||
            product.typeName.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(PawMedicColors.Teal600)
                    ) {
                        ProductCatalogBanner(
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = PawMedicColors.White,
                            modifier = Modifier
                                .padding(20.dp)
                                .size(28.dp)
                                .clickable(onClick = onBackClick)
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 24.dp, bottom = 22.dp, end = 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = PawMedicColors.White,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Catálogo de productos",
                                color = PawMedicColors.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = clinicName,
                                color = PawMedicColors.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar productos...", color = PawMedicColors.Gray400) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = PawMedicColors.Gray500)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PawMedicColors.Teal600,
                            unfocusedBorderColor = PawMedicColors.Gray300,
                            focusedContainerColor = PawMedicColors.White,
                            unfocusedContainerColor = PawMedicColors.White
                        )
                    )
                    Text(
                        text = "Categorías",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        productCategories.forEach { category ->
                            val selected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) PawMedicColors.Teal50 else PawMedicColors.White)
                                    .border(
                                        1.dp,
                                        if (selected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (selected) PawMedicColors.Teal700 else PawMedicColors.Gray700,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Text(
                        text = "Productos",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                when {
                    isLoading -> Text("Cargando catálogo...", color = PawMedicColors.Gray600, modifier = Modifier.padding(horizontal = 20.dp))
                    errorMessage != null -> Text(errorMessage.orEmpty(), color = PawMedicColors.Error, modifier = Modifier.padding(horizontal = 20.dp))
                    filteredProducts.isEmpty() -> Text("No hay productos que coincidan con tu búsqueda.", color = PawMedicColors.Gray600, modifier = Modifier.padding(horizontal = 20.dp))
                    else -> Spacer(Modifier.height(0.dp))
                }
            }

            items(filteredProducts, key = { it.id ?: it.name }) { product ->
                ProductCatalogCard(product, Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun ProductCatalogCard(product: Product, modifier: Modifier = Modifier) {
    val inStock = product.stockQuantity > 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PawMedicColors.Gray50),
            contentAlignment = Alignment.Center
        ) {
            ProductImage(product.imageUrl, 92.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
            Text(product.typeName ?: "Sin categoría", fontSize = 12.sp, color = PawMedicColors.Teal700, modifier = Modifier.padding(top = 4.dp))
            product.description?.takeIf(String::isNotBlank)?.let {
                Text(it, fontSize = 12.sp, color = PawMedicColors.Gray600, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
            }
            Text(product.formattedPrice(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600, modifier = Modifier.padding(top = 6.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (inStock) Color(0xFFE7F7EA) else Color(0xFFFEE2E2))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (inStock) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E8B46), modifier = Modifier.size(16.dp))
            Text(if (inStock) "En stock" else "Agotado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (inStock) Color(0xFF2E8B46) else Color(0xFFB91C1C), modifier = Modifier.padding(start = 4.dp))
        }
    }
}
