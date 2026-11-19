package com.pawsmedic.desktop.veterinary.ui

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class ReportPdfTest {
    @Test
    fun exportsAllRowsAcrossPages() {
        val file = Files.createTempFile("report-test-", ".pdf").toFile()
        try {
            val report = PdfReport(
                "2026-01-01", "2026-01-31", "America/Mexico_City", 80, 80,
                (1..80).map { PdfReportAppointment("2026-01-10T18:00:00Z", "Mascota $it", "Consulta") },
                listOf(PdfReportService("Consulta", 80))
            )
            writeReportPdf(file, report, false)
            PDDocument.load(file).use { document ->
                val text = PDFTextStripper().getText(document)
                assertTrue(document.numberOfPages > 1)
                assertTrue(text.contains("Mascota 80"))
                assertTrue(text.contains("Servicios prestados"))
                assertTrue(text.contains("Consulta"))
                assertTrue(text.contains("80"))
                assertTrue(text.contains("10/01/2026 12:00"))
                assertTrue(text.contains("PawMedic"))
                assertTrue(text.contains("Página 2"))
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun exportsLongNamesAndEmptyReports() {
        val file = Files.createTempFile("report-test-", ".pdf").toFile()
        try {
            val name = "Clínica " + "A".repeat(600)
            val report = PdfReport(
                "2026-01-01", "2026-01-31", "America/Mexico_City", 1, 1,
                listOf(PdfReportAppointment("2026-01-10T18:00:00Z", name, "Consulta")),
                emptyList()
            )
            writeReportPdf(file, report, false)
            PDDocument.load(file).use { document ->
                val text = PDFTextStripper().getText(document)
                assertTrue(text.contains("Clínica"))
                assertTrue(text.count { it == 'A' } >= 600)
                assertTrue(text.contains("No hay servicios prestados."))
            }
        } finally {
            file.delete()
        }
    }
}
