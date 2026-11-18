package com.pawsmedic.desktop.admin.ui

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.awt.Color
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal data class AdminPdfEvent(val date: String, val event: String, val clinic: String, val actor: String)

internal data class AdminPdfReport(
    val start: String,
    val end: String,
    val generatedBy: String,
    val counters: List<Pair<String, Int>>,
    val events: List<AdminPdfEvent>
)

internal fun chooseAdminPdfFile(english: Boolean): File? {
    val dialog = FileDialog(null as Frame?, if (english) "Save PDF report" else "Guardar reporte PDF", FileDialog.SAVE)
    dialog.file = if (english) "administrative-report.pdf" else "reporte-administrativo.pdf"
    return try {
        dialog.isVisible = true
        val directory = dialog.directory
        val name = dialog.file
        if (directory == null || name == null) null
        else File(directory, if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf")
    } finally {
        dialog.dispose()
    }
}

internal fun writeAdminReportPdf(file: File, report: AdminPdfReport, english: Boolean) {
    val fontsDir = File(System.getenv("WINDIR") ?: "C:\\Windows", "Fonts")
    val fontFile = File(fontsDir, "arial.ttf")
    val boldFile = File(fontsDir, "arialbd.ttf")
    check(fontFile.isFile && boldFile.isFile) { "No se encontró la fuente Arial necesaria para generar el PDF." }
    val target = file.toPath().toAbsolutePath()
    val temp = Files.createTempFile(target.parent, ".pawmedic-report-", ".pdf")
    try {
        PDDocument().use { document ->
            val font = PDType0Font.load(document, fontFile)
            val bold = PDType0Font.load(document, boldFile)
            var stream: PDPageContentStream? = null
            var y = 0f
            var pageNumber = 0
            val pageWidth = PDRectangle.A4.width
            val pageHeight = PDRectangle.A4.height
            val left = 44f
            val width = pageWidth - left * 2
            val ink = Color(30, 41, 59)
            val muted = Color(100, 116, 139)
            val teal = Color(13, 148, 136)
            val light = Color(241, 245, 249)
            val border = Color(226, 232, 240)

            fun rect(x: Float, bottom: Float, w: Float, h: Float, color: Color) {
                stream!!.setNonStrokingColor(color)
                stream!!.addRect(x, bottom, w, h)
                stream!!.fill()
            }
            fun safeText(text: String): String = buildString {
                text.replace(Regex("[\\r\\n\\t]+"), " ").codePoints().forEach { cp ->
                    if (font.hasGlyph(cp) && bold.hasGlyph(cp)) appendCodePoint(cp) else append('?')
                }
            }
            fun draw(text: String, x: Float, baseline: Float, size: Float, color: Color = ink, emphasis: Boolean = false) {
                stream!!.beginText()
                stream!!.setNonStrokingColor(color)
                stream!!.setFont(if (emphasis) bold else font, size)
                stream!!.newLineAtOffset(x, baseline)
                stream!!.showText(safeText(text))
                stream!!.endText()
            }
            fun footer() {
                if (stream == null) return
                rect(left, 37f, width, 0.6f, border)
                draw("PawMedic  |  ${if (english) "Administrative report" else "Reporte administrativo"}", left, 24f, 8f, muted)
                draw("${if (english) "Page" else "Página"} $pageNumber", pageWidth - 90f, 24f, 8f, muted)
            }
            fun newPage() {
                footer()
                stream?.close()
                val page = PDPage(PDRectangle.A4)
                document.addPage(page)
                stream = PDPageContentStream(document, page)
                pageNumber++
                rect(0f, pageHeight - 76f, pageWidth, 76f, Color(15, 23, 42))
                rect(left, pageHeight - 48f, 5f, 23f, teal)
                draw("PawMedic", left + 14f, pageHeight - 38f, 17f, Color.WHITE, true)
                draw(if (english) "PLATFORM ADMINISTRATION" else "ADMINISTRACIÓN DE LA PLATAFORMA",
                    left + 14f, pageHeight - 55f, 8f, Color(203, 213, 225))
                y = pageHeight - 101f
            }
            fun wrap(text: String, maxWidth: Float, size: Float): List<String> {
                val buffer = StringBuilder()
                val lines = mutableListOf<String>()
                safeText(text).codePoints().forEach { cp ->
                    val char = String(Character.toChars(cp))
                    if (buffer.isNotEmpty() && font.getStringWidth(buffer.toString() + char) * size / 1000f > maxWidth) {
                        lines.add(buffer.toString())
                        buffer.clear()
                    }
                    buffer.append(char)
                }
                if (buffer.isNotEmpty() || lines.isEmpty()) lines.add(buffer.toString())
                return lines
            }
            fun header(labels: List<Pair<String, Float>>) {
                if (y - 28f < 55f) newPage()
                rect(left, y - 22f, width, 22f, light)
                labels.forEach { (label, x) -> draw(label, x, y - 15f, 8f, muted, true) }
                y -= 22f
            }
            fun row(columns: List<Triple<String, Float, Float>>, labels: List<Pair<String, Float>>) {
                val lines = columns.map { wrap(it.first, it.third, 9.5f) }
                val count = lines.maxOf { it.size }
                var index = 0
                while (index < count) {
                    if (y - 24f < 55f) {
                        newPage()
                        header(labels)
                    }
                    val available = ((y - 55f - 10f) / 14f).toInt().coerceAtLeast(1)
                    val take = minOf(count - index, available)
                    columns.forEachIndexed { column, (_, x, _) ->
                        repeat(take) { offset ->
                            lines[column].getOrNull(index + offset)?.let {
                                draw(it, x, y - 15f - offset * 14f, 9.5f)
                            }
                        }
                    }
                    y -= take * 14f + 10f
                    rect(left, y, width, 0.5f, border)
                    index += take
                }
            }

            try {
                newPage()
                draw(if (english) "Administrative report" else "Reporte administrativo", left, y, 20f, ink, true)
                y -= 23f
                draw("${if (english) "Period" else "Periodo"}: ${report.start} - ${report.end}", left, y, 10f, muted)
                y -= 14f
                draw("${if (english) "Generated by" else "Generado por"}: ${report.generatedBy}", left, y, 10f, muted)
                y -= 20f

                val perRow = 3
                val gap = 10f
                val cardWidth = (width - gap * (perRow - 1)) / perRow
                report.counters.chunked(perRow).forEach { chunk ->
                    if (y - 60f < 55f) newPage()
                    chunk.forEachIndexed { index, (label, number) ->
                        val x = left + index * (cardWidth + gap)
                        rect(x, y - 52f, cardWidth, 52f, light)
                        draw(label, x + 10f, y - 18f, 8.5f, muted)
                        draw(number.toString(), x + 10f, y - 41f, 17f, teal, true)
                    }
                    y -= 62f
                }

                if (y - 55f < 55f) newPage()
                y -= 8f
                draw(if (english) "Activity detail" else "Detalle de actividad", left, y, 13f, ink, true)
                y -= 17f
                val labels = listOf(
                    (if (english) "DATE AND TIME" else "FECHA Y HORA") to (left + 8f),
                    (if (english) "EVENT" else "EVENTO") to (left + 108f),
                    (if (english) "CLINIC" else "VETERINARIA") to (left + 238f),
                    (if (english) "BY" else "POR") to (left + 398f)
                )
                header(labels)
                if (report.events.isEmpty()) {
                    row(listOf(Triple(if (english) "No activity in this period." else "No hay actividad en el periodo.", left + 8f, width - 16f)), labels)
                }
                report.events.forEach {
                    row(listOf(
                        Triple(it.date, left + 8f, 95f),
                        Triple(it.event, left + 108f, 125f),
                        Triple(it.clinic, left + 238f, 155f),
                        Triple(it.actor, left + 398f, width - 406f)
                    ), labels)
                }
            } finally {
                footer()
                stream?.close()
            }
            document.save(temp.toFile())
        }
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(temp)
    }
}
