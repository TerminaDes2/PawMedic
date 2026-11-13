package com.pawsmedic.desktop.veterinary.ui

import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage

internal interface CameraDevice {
    fun open()
    fun frame(): BufferedImage?
    fun close()
}

internal fun defaultCamera(): CameraDevice? {
    val webcam = Webcam.getDefault() ?: return null
    return object : CameraDevice {
        override fun open() {
            check(webcam.open()) { "No se pudo abrir la cámara." }
        }

        override fun frame(): BufferedImage? = webcam.image

        override fun close() {
            check(webcam.close()) { "No se pudo liberar la cámara." }
        }
    }
}

internal suspend fun runCameraPreview(
    createCamera: () -> CameraDevice?,
    onFrame: (BufferedImage) -> Unit
): Boolean {
    var camera: CameraDevice? = null
    try {
        withContext(Dispatchers.IO) {
            camera = createCamera()
            camera?.open()
        }
        val device = camera ?: return false
        while (currentCoroutineContext().isActive) {
            val image = withContext(Dispatchers.IO) { device.frame() }
            if (image != null) onFrame(image)
            delay(100)
        }
        return true
    } finally {
        withContext(NonCancellable + Dispatchers.IO) {
            try {
                camera?.close()
            } catch (e: Exception) {
                System.err.println("No se pudo liberar la cámara: ${e.javaClass.simpleName}")
            }
        }
    }
}
