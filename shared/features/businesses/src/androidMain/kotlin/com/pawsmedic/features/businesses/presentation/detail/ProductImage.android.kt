package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
internal actual fun ProductImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Imagen del producto",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
