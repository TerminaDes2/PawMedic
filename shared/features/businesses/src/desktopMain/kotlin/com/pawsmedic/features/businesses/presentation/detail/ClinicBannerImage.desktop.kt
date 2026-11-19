package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.imageio.ImageIO

@Composable
actual fun ClinicBannerImage(imageUrl: String?, modifier: Modifier) {
    val url = imageUrl.orEmpty()
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(url) { mutableStateOf(url.isNotBlank()) }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            bitmap = null
            loading = false
            return@LaunchedEffect
        }
        loading = true
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val uri = URI(url)
                require(uri.scheme == "https")
                uri.toURL().openConnection().apply {
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
        modifier = modifier.background(PawMedicSemanticColors.current.softAccent),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        when {
            current != null -> Image(current, contentDescription = "Clinic Logo", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
            loading -> CircularProgressIndicator(modifier = Modifier.size(32.dp), color = PawMedicColors.Teal600, strokeWidth = 2.dp)
            else -> Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = PawMedicSemanticColors.current.accent,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}
