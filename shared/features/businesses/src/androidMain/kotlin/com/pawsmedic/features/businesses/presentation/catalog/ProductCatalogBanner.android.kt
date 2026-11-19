package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pawsmedic.shared.features.businesses.R

@Composable
actual fun ProductCatalogBanner(modifier: Modifier) {
    Image(
        painter = painterResource(R.drawable.products_banner),
        contentDescription = "Productos para mascotas",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
