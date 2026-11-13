package com.pawsmedic.desktop.veterinary.ui

import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.io.ByteArrayInputStream

class ProfilePhotoTest {
    @Test
    fun `crop boundaries track position and zoom`() {
        assertEquals(PhotoCrop(0, 0, 100), photoCrop(200, 100, 1f, 0f, 0f))
        assertEquals(PhotoCrop(100, 0, 100), photoCrop(200, 100, 1f, 1f, 1f))
        assertEquals(PhotoCrop(150, 50, 50), photoCrop(200, 100, 2f, 1f, 1f))
        assertEquals(PhotoCrop(0, 0, 33), photoCrop(200, 100, 8f, -1f, -1f))
    }

    @Test
    fun `applied crop is the image that will be uploaded`() = runBlocking {
        val image = BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = java.awt.Color.RED
            graphics.fillRect(0, 0, 100, 100)
            graphics.color = java.awt.Color.BLUE
            graphics.fillRect(100, 0, 100, 100)
        } finally { graphics.dispose() }
        val cropped = cropProfilePhoto(selectedPhotoFromImage(image), 1f, 1f, 0.5f)
        val saved = ImageIO.read(ByteArrayInputStream(cropped.bytes))
        assertEquals(512, saved.width)
        assertEquals(java.awt.Color.BLUE.rgb, saved.getRGB(256, 256))
        assertEquals(512, cropped.preview.width)
    }

    @Test
    fun `reads a valid png for preview`() = runBlocking {
        val file = Files.createTempFile("profile-photo-", ".png").toFile()
        try {
            ImageIO.write(BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", file)
            val photo = readProfilePhoto(file)
            assertEquals(4, photo.preview.width)
            assertEquals("png", photo.extension)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `rejects invalid files and mismatched extensions`() = runBlocking {
        val file = Files.createTempFile("profile-photo-", ".jpg").toFile()
        try {
            file.writeText("not an image")
            assertFailsWith<IllegalArgumentException> { readProfilePhoto(file) }
            ImageIO.write(BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", file)
            assertFailsWith<IllegalArgumentException> { readProfilePhoto(file) }
            Unit
        } finally {
            file.delete()
        }
    }
}
