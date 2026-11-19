package com.pawsmedic.desktop.veterinary.ui.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthMessageTest {
    @Test
    fun `sign in error follows the currently selected language`() {
        val message = signInError("Por favor ingrese correo electrónico y contraseña")
        assertEquals("Enter your email and password.", message.text(true))
        assertEquals("Por favor ingrese correo electrónico y contraseña.", message.text(false))
        assertEquals("Enter your email and password.", message.text(true))
    }

    @Test
    fun `errors from photo picker remain bilingual regardless of language at failure`() {
        val english = photoErrorMessage("Select a JPG or PNG image.")
        val spanish = photoErrorMessage("Selecciona una imagen JPG o PNG.")
        assertEquals(english, spanish)
        assertEquals("Selecciona una imagen JPG o PNG.", english.text(false))
        assertEquals("Select a JPG or PNG image.", spanish.text(true))
    }

    @Test
    fun `dynamic messages keep their values while switching language`() {
        val message = AuthMessage("Código enviado a test@example.com.", "Code sent to test@example.com.")
        assertEquals("Código enviado a test@example.com.", message.text(false))
        assertEquals("Code sent to test@example.com.", message.text(true))
    }
}
