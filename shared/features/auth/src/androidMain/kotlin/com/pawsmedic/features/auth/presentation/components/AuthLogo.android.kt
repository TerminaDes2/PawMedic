package com.pawsmedic.features.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pawsmedic.shared.core.designsystem.components.PawMedicLogoIcon

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
actual fun AuthLogo(modifier: Modifier) {
    val context = LocalContext.current
    val resourceId = remember(context) {
        context.resources.getIdentifier(
            "logo_avd_anim",
            "drawable",
            context.packageName
        )
    }

    if (resourceId != 0) {
        val animatedVector = AnimatedImageVector.animatedVectorResource(resourceId)
        var atEnd by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { atEnd = true }
        Image(
            painter = rememberAnimatedVectorPainter(animatedVector, atEnd),
            contentDescription = "PawMedic",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        PawMedicLogoIcon(modifier = modifier)
    }
}
