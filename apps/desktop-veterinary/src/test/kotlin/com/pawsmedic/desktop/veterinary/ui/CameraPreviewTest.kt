package com.pawsmedic.desktop.veterinary.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CameraPreviewTest {
    @Test
    fun `frames return to caller context and cancellation closes camera`() = runBlocking {
        val dispatcher = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "camera-test-ui")
        }.asCoroutineDispatcher()
        try {
            val received = CompletableDeferred<String>()
            var openThread = ""
            var closed = false
            val camera = object : CameraDevice {
                override fun open() { openThread = Thread.currentThread().name }
                override fun frame() = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
                override fun close() { closed = true }
            }
            withContext(dispatcher) {
                val job = launch {
                    runCameraPreview({ camera }) { received.complete(Thread.currentThread().name) }
                }
                assertTrue(withTimeout(5000) { received.await() }.startsWith("camera-test-ui"))
                job.cancelAndJoin()
            }
            assertNotEquals("camera-test-ui", openThread)
            assertTrue(closed)
        } finally {
            dispatcher.close()
        }
    }

    @Test
    fun `camera is released when opening fails`() = runBlocking {
        var closed = false
        val camera = object : CameraDevice {
            override fun open() { throw IllegalStateException("camera unavailable") }
            override fun frame(): BufferedImage? = null
            override fun close() { closed = true }
        }
        assertFailsWith<IllegalStateException> { runCameraPreview({ camera }) {} }
        assertTrue(closed)
    }

    @Test
    fun `no camera returns false`() = runBlocking {
        assertFalse(runCameraPreview({ null }) {})
    }
}
