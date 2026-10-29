package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient

@Composable
actual fun rememberPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String,
    folderName: String,
    onPhotoPicked: (String) -> Unit,
    onUploadError: (String) -> Unit
): () -> Unit {
    return {
        // Desktop mock photo picker
    }
}

@Composable
actual fun rememberDeferredPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String,
    folderName: String,
    onPhotoPicked: (String) -> Unit
): DeferredPhotoPicker {
    return DeferredPhotoPicker(
        pick = {},
        upload = {
            Result.failure<String>(UnsupportedOperationException("La selección de imágenes no está disponible en escritorio"))
        }
    )
}
