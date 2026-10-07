package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (String) -> Unit
): () -> Unit {
    return {
        // Desktop mock photo picker
    }
}
