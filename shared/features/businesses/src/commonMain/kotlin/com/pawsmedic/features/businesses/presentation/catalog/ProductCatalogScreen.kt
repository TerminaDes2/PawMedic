package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.businesses.data.datasource.PublicProduct
import com.pawsmedic.features.businesses.data.datasource.PublicProductsDataSource
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

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
    publicProductsDataSource: PublicProductsDataSource,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    var products by remember(tenantId) { mutableStateOf<List<PublicProduct>>(emptyList()) }
    var isLoading by remember(tenantId) { mutableStateOf(true) }
    var errorMessage by remember(tenantId) { mutableStateOf<String?>(null) }
    var query by remember(tenantId) { mutableStateOf("") }
    var selectedCategory by remember(tenantId) { mutableStateOf("Todos") }

    LaunchedEffect(tenantId, publicProductsDataSource) {
        isLoading = true
        errorMessage = null
        try {
            products = publicProductsDataSource.list(tenantId)
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = error.message ?: language.text("No se pudo cargar el catálogo.", "Could not load the catalog.")
        } finally {
            isLoading = false
        }
    }

    val categories = productCategories
    val filtered = products.filter { product ->
        val matchesQuery = query.isBlank() ||
            product.name.contains(query, ignoreCase = true) ||
            product.description.contains(query, ignoreCase = true)
        val matchesCategory = selectedCategory == "Todos" ||
            product.typeName.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    Column(modifier = modifier.fillMaxSize().background(PawMedicSemanticColors.current.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(170.dp)
                        .background(PawMedicSemanticColors.current.accent)
                ) {
                    ProductCatalogBanner(modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PawMedicColors.White.copy(alpha = 0.16f))
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = language.text("Regresar", "Back"), tint = PawMedicColors.White)
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 20.dp)
                    ) {
                        Text(language.text("Catálogo de productos", "Product catalog"), color = PawMedicColors.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(clinicName, color = PawMedicColors.White, fontSize = 14.sp)
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(language.text("Buscar productos...", "Search products...")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PawMedicSemanticColors.current.accent,
                        unfocusedBorderColor = PawMedicSemanticColors.current.separator,
                        focusedContainerColor = PawMedicSemanticColors.current.surface,
                        unfocusedContainerColor = PawMedicSemanticColors.current.surface
                    )
                )

                Text(
                    language.text("Categorías", "Categories"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.text,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.border,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Text(
                    language.text("Productos publicados", "Published products"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            item {
                when {
                    isLoading -> Text(language.text("Cargando catálogo...", "Loading catalog..."), modifier = Modifier.padding(horizontal = 20.dp), color = PawMedicSemanticColors.current.secondaryText)
                    errorMessage != null -> Text(errorMessage.orEmpty(), modifier = Modifier.padding(horizontal = 20.dp), color = PawMedicColors.Error)
                    filtered.isEmpty() -> Text(language.text("No hay productos publicados que coincidan.", "No published products match."), modifier = Modifier.padding(horizontal = 20.dp), color = PawMedicSemanticColors.current.secondaryText)
                }
            }

            items(filtered, key = { it.id }) { product ->
                ProductCatalogCard(product, Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun ProductCatalogCard(product: PublicProduct, modifier: Modifier = Modifier) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(92.dp).clip(RoundedCornerShape(14.dp))
                .background(PawMedicSemanticColors.current.softAccent),
            contentAlignment = Alignment.Center
        ) { ProductImage(product.imageUrl, 92.dp) }

        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
            Text(
                product.typeName?.takeIf(String::isNotBlank)
                    ?: language.text("Sin categoría", "No category"),
                fontSize = 12.sp,
                color = PawMedicSemanticColors.current.accent,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (product.description.isNotBlank()) {
                Text(product.description, fontSize = 12.sp, color = PawMedicSemanticColors.current.secondaryText, modifier = Modifier.padding(top = 4.dp), maxLines = 2)
            }
            Text(
                "$${product.priceCents / 100}.${(product.priceCents % 100).toString().padStart(2, '0')} MXN",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
