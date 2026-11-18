package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
actual fun ProductCatalogBanner(modifier: Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.background(PawMedicColors.Teal600)
    )
}
