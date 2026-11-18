package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
expect fun ProductImage(
    imageUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
)

@Composable
internal fun ProductImageFallback() {
    Icon(
        imageVector = Icons.Default.Inventory2,
        contentDescription = null,
        tint = PawMedicColors.Teal600
    )
}
