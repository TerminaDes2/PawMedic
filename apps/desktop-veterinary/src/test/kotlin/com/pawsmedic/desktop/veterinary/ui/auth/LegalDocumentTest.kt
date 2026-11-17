package com.pawsmedic.desktop.veterinary.ui.auth

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LegalDocumentTest {
    @Test
    fun `bundled document contains both complete sections and corrected claims`() {
        val document = loadLegalDocument()
        val introduction = document.introduction.joinToString("\n")
        val terms = document.terms.joinToString("\n")
        val privacy = document.privacy.joinToString("\n")
        assertContains(introduction, "Documento pendiente de revisión")
        assertContains(terms, "PARTE I.")
        assertContains(terms, "permite exportar reportes operativos a PDF")
        assertContains(privacy, "PARTE II.")
        assertContains(privacy, "Argos Translate")
        assertContains(privacy, "Fecha de última actualización")
        assertTrue("**Microsoft Azure:**" !in privacy)
    }

    @Test
    fun `invalid documents fail explicitly`() {
        assertFailsWith<IllegalArgumentException> { parseLegalDocument("## PARTE I. Términos") }
        assertFailsWith<IllegalArgumentException> { parseLegalDocument("## PARTE II. Privacidad\ntexto\n## PARTE I. Términos") }
    }
}
