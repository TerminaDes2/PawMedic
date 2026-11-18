package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
actual fun ProductImage(imageUrl: String?, size: Dp, modifier: Modifier) {
    val url = imageUrl.orEmpty()
    if (url.isBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(14.dp))
                .background(PawMedicColors.Gray50),
            contentAlignment = Alignment.Center
        ) {
            ProductImageFallback()
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
            contentDescription = "Imagen del producto",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(RoundedCornerShape(14.dp))
        )
    }
}
