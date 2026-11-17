package com.pawsmedic.desktop.veterinary.ui

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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal data class PdfReportAppointment(val startsAt: String, val petName: String, val serviceName: String)
internal data class PdfReportService(val name: String, val total: Int)
internal data class PdfReport(
    val start: String,
    val end: String,
    val timeZone: String,
    val total: Int,
    val completed: Int,
    val appointments: List<PdfReportAppointment>,
    val services: List<PdfReportService>
)

internal fun choosePdfFile(english: Boolean = false): File? {
    val dialog = FileDialog(null as Frame?, if (english) "Save PDF report" else "Guardar reporte PDF", FileDialog.SAVE)
    dialog.file = if (english) "operational-report.pdf" else "reporte-operativo.pdf"
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

internal fun writeReportPdf(file: File, report: PdfReport, english: Boolean) {
    val fontFile = File(System.getenv("WINDIR") ?: "C:\\Windows", "Fonts\\arial.ttf")
    val boldFile = File(System.getenv("WINDIR") ?: "C:\\Windows", "Fonts\\arialbd.ttf")
    check(fontFile.isFile) { "No se encontró la fuente Arial necesaria para generar el PDF." }
    check(boldFile.isFile) { "No se encontró la fuente Arial negrita necesaria para generar el PDF." }
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
                draw("PawMedic  |  ${if (english) "Operational report" else "Reporte operativo"}", left, 24f, 8f, muted)
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
                draw(if (english) "VETERINARY OPERATIONS" else "GESTIÓN VETERINARIA",
                    left + 14f, pageHeight - 55f, 8f, Color(203, 213, 225))
                y = pageHeight - 101f
            }
            fun ensure(height: Float) {
                if (y - height < 55f) newPage()
            }
            fun wrap(text: String, maxWidth: Float, size: Float): List<String> {
                val normalized = safeText(text)
                val buffer = StringBuilder()
                val lines = mutableListOf<String>()
                fun finish() {
                    lines.add(buffer.toString())
                    buffer.clear()
                }
                normalized.codePoints().forEach { cp ->
                    val char = String(Character.toChars(cp))
                    if (buffer.isNotEmpty() && font.getStringWidth(buffer.toString() + char) * size / 1000f > maxWidth) {
                        finish()
                    }
                    buffer.append(char)
                }
                if (buffer.isNotEmpty() || lines.isEmpty()) finish()
                return lines
            }
            fun section(title: String) {
                ensure(55f)
                y -= 14f
                draw(title, left, y, 13f, ink, true)
                y -= 17f
            }
            fun header(labels: List<Pair<String, Float>>) {
                ensure(28f)
                rect(left, y - 22f, width, 22f, light)
                labels.forEach { (label, x) -> draw(label, x, y - 15f, 8f, muted, true) }
                y -= 22f
            }
            fun row(columns: List<Triple<String, Float, Float>>, labels: List<Pair<String, Float>>) {
                val lines = columns.map { wrap(it.first, it.third, 9.5f) }
                var index = 0
                val count = lines.maxOf { it.size }
                while (index < count) {
                    if (y - 24f < 55f) {
                        newPage()
                        header(labels)
                    }
                    val available = ((y - 55f - 10f) / 14f).toInt().coerceAtLeast(1)
                    val take = minOf(count - index, available)
                    val height = take * 14f + 10f
                    columns.forEachIndexed { column, (_, x, _) ->
                        repeat(take) { offset ->
                            val text = lines[column].getOrNull(index + offset)
                            if (text != null) draw(text, x, y - 15f - offset * 14f, 9.5f)
                        }
                    }
                    y -= height
                    rect(left, y, width, 0.5f, border)
                    index += take
                }
            }
            try {
                newPage()
                draw(if (english) "Operational report" else "Reporte operativo", left, y, 20f, ink, true)
                y -= 23f
                draw("${if (english) "Period" else "Periodo"}: ${report.start} - ${report.end}", left, y, 10f, muted)
                y -= 16f
                wrap("${if (english) "Time zone" else "Zona horaria"}: ${report.timeZone}", width, 10f).forEach {
                    draw(it, left, y, 10f, muted)
                    y -= 14f
                }
                y -= 6f
                val cardWidth = (width - 12f) / 2f
                listOf(
                    (if (english) "Total appointments" else "Total de citas") to report.total,
                    (if (english) "Completed appointments" else "Citas atendidas") to report.completed
                ).forEachIndexed { index, (label, number) ->
                    val x = left + index * (cardWidth + 12f)
                    rect(x, y - 56f, cardWidth, 56f, light)
                    draw(label, x + 12f, y - 20f, 9f, muted)
                    draw(number.toString(), x + 12f, y - 43f, 18f, teal, true)
                }
                y -= 65f
                section(if (english) "Completed appointments" else "Citas atendidas")
                val appointmentLabels = listOf(
                    (if (english) "DATE AND TIME" else "FECHA Y HORA") to (left + 8f),
                    (if (english) "PET" else "MASCOTA") to (left + 160f),
                    (if (english) "SERVICE" else "SERVICIO") to (left + 300f)
                )
                header(appointmentLabels)
                if (report.appointments.isEmpty()) {
                    row(listOf(Triple(if (english) "No completed appointments." else "No hay citas atendidas.", left + 8f, width - 16f)), appointmentLabels)
                }
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                val zone = ZoneId.of(report.timeZone)
                report.appointments.forEach {
                    row(listOf(
                        Triple(Instant.parse(it.startsAt).atZone(zone).format(formatter), left + 8f, 140f),
                        Triple(it.petName, left + 160f, 130f),
                        Triple(it.serviceName, left + 300f, width - 308f)
                    ), appointmentLabels)
                }
                section(if (english) "Services provided" else "Servicios prestados")
                val serviceLabels = listOf((if (english) "SERVICE" else "SERVICIO") to (left + 8f),
                    "TOTAL" to (left + width - 60f))
                header(serviceLabels)
                if (report.services.isEmpty()) {
                    row(listOf(Triple(if (english) "No services provided." else "No hay servicios prestados.", left + 8f, width - 16f)), serviceLabels)
                }
                report.services.forEach {
                    row(listOf(Triple(it.name, left + 8f, width - 80f),
                        Triple(it.total.toString(), left + width - 60f, 50f)), serviceLabels)
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
