package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage

@Composable
internal fun ProfilePhotoPicker(
    enabled: Boolean,
    onPhotoSelected: (SelectedPhoto) -> Unit,
    onError: (String) -> Unit,
    onBusyChange: (Boolean) -> Unit
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<SelectedPhoto?>(null) }
    var cameraOpen by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }

    fun process(block: suspend () -> SelectedPhoto) {
        working = true
        onBusyChange(true)
        scope.launch {
            try {
                pending = block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when (e.message) {
                    "Selecciona una imagen JPG o PNG." -> tr("Selecciona una imagen JPG o PNG.", "Select a JPG or PNG image.")
                    "Selecciona una imagen JPG o PNG válida." -> tr("Selecciona una imagen JPG o PNG válida.", "Select a valid JPG or PNG image.")
                    "La foto debe tener un tamaño máximo de 5 MB.", "La imagen supera el tamaño máximo de 5 MB." -> tr("La foto debe tener un tamaño máximo de 5 MB.", "The photo must be no larger than 5 MB.")
                    "La extensión de la foto no coincide con su contenido." -> tr("La extensión de la foto no coincide con su contenido.", "The photo extension does not match its contents.")
                    "La imagen no debe superar 4096 × 4096 píxeles." -> tr("La imagen no debe superar 4096 × 4096 píxeles.", "The image must not exceed 4096 × 4096 pixels.")
                    else -> tr("No se pudo preparar la foto.", "Could not prepare the photo.")
                }
                onError(message)
            } finally {
                working = false
                onBusyChange(false)
            }
        }
    }

    Column {
        OutlinedButton(enabled = enabled && !working, onClick = {
            chooseProfilePhoto(english)?.let { file -> process { readProfilePhoto(file) } }
        }) { Text(tr("Seleccionar foto", "Select photo")) }
        OutlinedButton(enabled = enabled && !working, onClick = { cameraOpen = true }) {
            Text(tr("Tomar foto con cámara", "Take photo with camera"))
        }
    }

    if (cameraOpen) {
        CameraDialog(
            onDismiss = { cameraOpen = false },
            onCapture = { image ->
                cameraOpen = false
                process { withContext(Dispatchers.IO) { selectedPhotoFromImage(image) } }
            }
        )
    }
    pending?.let { photo ->
        PhotoCropDialog(
            photo = photo,
            onDismiss = { pending = null },
            onApply = { cropped -> pending = null; onPhotoSelected(cropped) }
        )
    }
}

@Composable
private fun CameraDialog(onDismiss: () -> Unit, onCapture: (BufferedImage) -> Unit) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    var frame by remember { mutableStateOf<BufferedImage?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        frame = null
        message = null
        try {
            if (!runCameraPreview(::defaultCamera) { frame = it }) {
                message = tr("No se encontró una cámara. Conéctala y vuelve a intentar.", "No camera was found. Connect one and try again.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = tr("No se pudo abrir la cámara. Comprueba los permisos y que no esté en uso.", "Could not open the camera. Check permissions and that it is not in use.")
            frame = null
        } catch (e: LinkageError) {
            message = tr("La cámara no es compatible con este equipo.", "The camera is not supported on this device.")
            frame = null
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(Modifier.widthIn(max = 520.dp).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Tomar foto", "Take photo"), style = MaterialTheme.typography.titleLarge)
                val current = frame
                if (current != null) {
                    androidx.compose.foundation.Image(current.toComposeImageBitmap(), tr("Vista de la cámara", "Camera preview"),
                        modifier = Modifier.fillMaxWidth().height(300.dp))
                } else {
                    Box(Modifier.fillMaxWidth().height(180.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text(message ?: tr("Iniciando cámara…", "Starting camera…"))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text(tr("Cancelar", "Cancel")) }
                    if (message != null) {
                        OutlinedButton(onClick = { attempt++ }) { Text(tr("Reintentar", "Retry")) }
                    }
                    Button(enabled = current != null, onClick = { current?.let(onCapture) }) { Text(tr("Capturar", "Capture")) }
                }
            }
        }
    }
}

@Composable
private fun PhotoCropDialog(photo: SelectedPhoto, onDismiss: () -> Unit, onApply: (SelectedPhoto) -> Unit) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    var zoom by remember(photo) { mutableFloatStateOf(1f) }
    var horizontal by remember(photo) { mutableFloatStateOf(0.5f) }
    var vertical by remember(photo) { mutableFloatStateOf(0.5f) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val crop = photoCrop(photo.preview.width, photo.preview.height, zoom, horizontal, vertical)
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(Modifier.widthIn(max = 430.dp).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("Ajustar foto", "Adjust photo"), style = MaterialTheme.typography.titleLarge)
                Text(tr("Arrastra la imagen para centrarla y ajusta el zoom.", "Drag the image to center it and adjust the zoom."))
                Canvas(
                    Modifier.align(Alignment.CenterHorizontally).size(240.dp).clip(CircleShape)
                        .background(Color.LightGray).pointerInput(photo, zoom) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                val extraX = photo.preview.width - crop.size
                                val extraY = photo.preview.height - crop.size
                                if (extraX > 0) horizontal = (horizontal - drag.x / size.width * crop.size / extraX).coerceIn(0f, 1f)
                                if (extraY > 0) vertical = (vertical - drag.y / size.height * crop.size / extraY).coerceIn(0f, 1f)
                            }
                        }
                ) {
                    drawImage(photo.preview, srcOffset = IntOffset(crop.x, crop.y),
                        srcSize = IntSize(crop.size, crop.size),
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()))
                }
                Text("Zoom")
                Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..3f, enabled = !saving)
                Text(tr("Posición horizontal", "Horizontal position"))
                Slider(value = horizontal, onValueChange = { horizontal = it }, enabled = !saving)
                Text(tr("Posición vertical", "Vertical position"))
                Slider(value = vertical, onValueChange = { vertical = it }, enabled = !saving)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(enabled = !saving, onClick = onDismiss) { Text(tr("Cancelar", "Cancel")) }
                    Button(enabled = !saving, onClick = {
                        saving = true
                        scope.launch {
                            try {
                                onApply(cropProfilePhoto(photo, zoom, horizontal, vertical))
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                error = tr("No se pudo ajustar la foto. Inténtalo de nuevo.", "Could not adjust the photo. Please try again.")
                            } finally { saving = false }
                        }
                    }) { Text(tr("Usar foto", "Use photo")) }
                }
            }
        }
    }
}
