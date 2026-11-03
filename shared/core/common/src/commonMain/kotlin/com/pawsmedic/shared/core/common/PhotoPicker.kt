package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient

data class DeferredPhotoPicker(
    val pick: () -> Unit,
    val upload: suspend () -> Result<String>
)

@Composable
expect fun rememberPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String = "pets",
    folderName: String = "uploads",
    onPhotoPicked: (String) -> Unit,
    onUploadError: (String) -> Unit = {}
): () -> Unit

@Composable
expect fun rememberDeferredPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String = "pets",
    folderName: String = "uploads",
    onPhotoPicked: (String) -> Unit
): DeferredPhotoPicker
