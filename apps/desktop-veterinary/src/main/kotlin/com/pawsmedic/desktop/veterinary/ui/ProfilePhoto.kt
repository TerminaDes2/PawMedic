package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayInputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

internal data class SelectedPhoto(val bytes: ByteArray, val extension: String, val preview: ImageBitmap)

internal data class PhotoCrop(val x: Int, val y: Int, val size: Int)

internal fun photoCrop(width: Int, height: Int, zoom: Float, horizontal: Float, vertical: Float): PhotoCrop {
    require(width > 0 && height > 0)
    val size = (minOf(width, height) / zoom.coerceIn(1f, 3f)).toInt().coerceAtLeast(1)
    val x = ((width - size) * horizontal.coerceIn(0f, 1f)).toInt()
    val y = ((height - size) * vertical.coerceIn(0f, 1f)).toInt()
    return PhotoCrop(x, y, size)
}

internal suspend fun cropProfilePhoto(photo: SelectedPhoto, zoom: Float, horizontal: Float, vertical: Float): SelectedPhoto =
    withContext(Dispatchers.IO) {
        val source = ImageIO.read(ByteArrayInputStream(photo.bytes)) ?: error("No se pudo leer la imagen.")
        val crop = photoCrop(source.width, source.height, zoom, horizontal, vertical)
        val result = BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB)
        val graphics = result.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.drawImage(source, 0, 0, 512, 512, crop.x, crop.y,
                crop.x + crop.size, crop.y + crop.size, null)
        } finally {
            graphics.dispose()
        }
        selectedPhotoFromImage(result)
    }

internal fun selectedPhotoFromImage(image: BufferedImage): SelectedPhoto {
    val output = ByteArrayOutputStream()
    check(ImageIO.write(image, "png", output)) { "No se pudo preparar la foto." }
    val bytes = output.toByteArray()
    require(bytes.size <= 5_000_000) { "La imagen supera el tamaño máximo de 5 MB." }
    return SelectedPhoto(bytes, "png", image.toComposeImageBitmap())
}

internal fun chooseProfilePhoto(): File? {
    val dialog = FileDialog(null as Frame?, "Seleccionar foto de perfil", FileDialog.LOAD)
    return try {
        dialog.isVisible = true
        val name = dialog.file
        val directory = dialog.directory
        if (name != null && directory != null) File(directory, name) else null
    } finally {
        dialog.dispose()
    }
}

internal suspend fun readProfilePhoto(file: File): SelectedPhoto = withContext(Dispatchers.IO) {
    val extension = file.extension.lowercase()
    require(extension in setOf("jpg", "jpeg", "png")) { "Selecciona una imagen JPG o PNG." }
    require(file.isFile && file.length() in 1..5_000_000) { "La foto debe tener un tamaño máximo de 5 MB." }
    val bytes = file.readBytes()
    require(bytes.size in 1..5_000_000) { "La foto debe tener un tamaño máximo de 5 MB." }
    val stream = ImageIO.createImageInputStream(ByteArrayInputStream(bytes))
        ?: error("No se pudo leer la imagen.")
    val image = stream.use {
        val readers = ImageIO.getImageReaders(it)
        require(readers.hasNext()) { "Selecciona una imagen JPG o PNG válida." }
        val reader = readers.next()
        try {
            reader.input = it
            val format = reader.formatName.lowercase()
            require(format in setOf("jpg", "jpeg", "png")) { "Selecciona una imagen JPG o PNG válida." }
            require((extension == "png") == (format == "png")) {
                "La extensión de la foto no coincide con su contenido."
            }
            require(reader.getWidth(0) <= 4096 && reader.getHeight(0) <= 4096) {
                "La imagen no debe superar 4096 × 4096 píxeles."
            }
            reader.read(0) ?: error("No se pudo leer la imagen.")
        } finally {
            reader.dispose()
        }
    }
    SelectedPhoto(bytes, extension, image.toComposeImageBitmap())
}
