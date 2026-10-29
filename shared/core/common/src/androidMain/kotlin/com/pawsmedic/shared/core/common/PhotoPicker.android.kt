package com.pawsmedic.shared.core.common

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@Composable
actual fun rememberPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String,
    folderName: String,
    onPhotoPicked: (String) -> Unit,
    onUploadError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                uploadImageToSupabase(
                    context = context,
                    supabaseClient = supabaseClient,
                    imageUri = selectedUri,
                    bucketName = bucketName,
                    folderName = folderName
                ).onSuccess(onPhotoPicked)
                    .onFailure { error ->
                        onUploadError(error.message ?: "No se pudo subir la imagen")
                    }
            }
        }
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
