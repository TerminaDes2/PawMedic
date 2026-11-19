package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.imageio.ImageIO

@Composable
actual fun ProductImage(imageUrl: String?, size: Dp, modifier: Modifier) {
    var bitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(imageUrl) { mutableStateOf(!imageUrl.isNullOrBlank()) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) {
            bitmap = null
            loading = false
            return@LaunchedEffect
        }
        loading = true
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val uri = URI(imageUrl)
                require(uri.scheme == "https")
                URI(imageUrl).toURL().openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }.getInputStream().use { input ->
                    ImageIO.read(input)?.toComposeImageBitmap()
                }
            }.getOrNull()
        }
        loading = false
    }

    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(14.dp)).background(PawMedicColors.Gray50),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        when {
            current != null -> Image(current, contentDescription = "Imagen del producto", contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(14.dp)))
            loading -> CircularProgressIndicator(modifier = Modifier.size((size.value * 0.28f).dp), color = PawMedicColors.Teal600, strokeWidth = 2.dp)
            else -> Icon(Icons.Default.Inventory2, null, tint = PawMedicColors.Teal600)
        }
    }
}
