package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

@Composable
actual fun ClinicBannerImage(imageUrl: String?, modifier: Modifier) {
    val url = imageUrl.orEmpty()
    val context = LocalContext.current
    println("CLINIC_BANNER_IMAGE_URL: '$url'")
    if (url.isBlank()) {
        Box(
            modifier = modifier.background(PawMedicSemanticColors.current.softAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = PawMedicSemanticColors.current.accent,
                modifier = Modifier.size(56.dp)
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .listener(
                    onSuccess = { _, _ -> println("CLINIC_BANNER_COIL_SUCCESS: Loaded $url") },
                    onError = { _, result -> System.err.println("CLINIC_BANNER_COIL_ERROR: Failed to load $url. Reason: ${result.throwable.javaClass.simpleName}: ${result.throwable.message}") }
                )
                .build(),
            contentDescription = "Clinic Logo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
