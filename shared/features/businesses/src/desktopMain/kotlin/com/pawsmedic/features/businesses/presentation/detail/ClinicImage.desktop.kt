package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.imageio.ImageIO

@Composable
internal actual fun ClinicImage(url: String, modifier: Modifier) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val uri = URI(url)
                require(uri.scheme == "https")
                uri.toURL().openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }.getInputStream().use { ImageIO.read(it)?.toComposeImageBitmap() }
            }.getOrNull()
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Foto de la veterinaria",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
