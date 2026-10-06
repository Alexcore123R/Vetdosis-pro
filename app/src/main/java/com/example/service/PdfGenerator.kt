package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateAndSharePrescriptionPdf(
        context: Context,
        clinicName: String,
        doctorName: String,
        doctorLicense: String, // Cédula Profesional
        clinicAddress: String,
        clinicPhone: String,
        patientName: String,
        speciesName: String,
        breed: String,
        age: String,
        weightKg: Double,
        ownerName: String,
        prescriptions: List<String>, // items list
        indications: String
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (72 dpi approx)
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val primaryColor = Color.rgb(13, 92, 88)
        val darkGray = Color.rgb(40, 40, 40)
        val lightGray = Color.rgb(230, 235, 235)
        val accentGold = Color.rgb(197, 155, 39)

        val paint = Paint().apply { isAntiAlias = true }

        // Top Header Banner
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 105f, paint)

        // Accent strip
        paint.color = accentGold
        canvas.drawRect(0f, 105f, 595f, 110f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        canvas.drawText(clinicName.ifBlank { "CLÍNICA VETERINARIA VETDOSIS PRO" }, 40f, 42f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dr./MVZ: $doctorName  |  Cédula Prof: $doctorLicense", 40f, 65f, paint)
        canvas.drawText("Dirección: $clinicAddress  |  Tel: $clinicPhone", 40f, 85f, paint)

        var currentY = 145f

        // Document Title Box
        paint.color = primaryColor
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RECETA MÉDICA VETERINARIA OFICIAL", 40f, currentY, paint)

        paint.color = darkGray
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "MX")).format(Date())
        canvas.drawText("Fecha de emisión: $dateStr", 340f, currentY, paint)

        currentY += 20f

        // Patient Box Background
        paint.color = lightGray
        canvas.drawRoundRect(40f, currentY, 555f, currentY + 75f, 8f, 8f, paint)

        // Patient Box Details
        paint.color = darkGray
        paint.textSize = 11f
        val startY = currentY + 22f
        canvas.drawText("Paciente: $patientName", 55f, startY, paint)
        canvas.drawText("Especie: $speciesName", 220f, startY, paint)
        canvas.drawText("Raza: ${breed.ifBlank { "Mestizo" }}", 380f, startY, paint)

        canvas.drawText("Peso: $weightKg kg", 55f, startY + 24f, paint)
        canvas.drawText("Edad: ${age.ifBlank { "Adulto" }}", 220f, startY + 24f, paint)
        canvas.drawText("Propietario: ${ownerName.ifBlank { "Particular" }}", 380f, startY + 24f, paint)

        currentY += 105f

        // Prescription Content (Rx)
        paint.color = primaryColor
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Rp /", 40f, currentY, paint)

        currentY += 25f
        paint.color = darkGray
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        if (prescriptions.isEmpty()) {
            canvas.drawText("1. Medicamento según indicaciones clínicas.", 40f, currentY, paint)
            currentY += 25f
        } else {
            prescriptions.forEachIndexed { index, item ->
                canvas.drawText("${index + 1}. $item", 40f, currentY, paint)
                currentY += 25f
            }
        }

        currentY += 15f
        if (indications.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = primaryColor
            canvas.drawText("Indicaciones y cuidados específicos:", 40f, currentY, paint)
            currentY += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = darkGray
            val lines = indications.chunked(70)
            for (line in lines) {
                canvas.drawText(line, 40f, currentY, paint)
                currentY += 18f
            }
        }

        // Signature Line at bottom
        val footerY = 740f
        paint.color = Color.DKGRAY
        paint.strokeWidth = 1.2f
        canvas.drawLine(180f, footerY, 415f, footerY, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val doctorSignText = "MVZ. $doctorName"
        val textWidth = paint.measureText(doctorSignText)
        canvas.drawText(doctorSignText, (595f - textWidth) / 2f, footerY + 18f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val licenseText = "Cédula Profesional: $doctorLicense"
        val licWidth = paint.measureText(licenseText)
        canvas.drawText(licenseText, (595f - licWidth) / 2f, footerY + 32f, paint)

        // Bottom footer note
        paint.color = Color.GRAY
        paint.textSize = 8f
        val notice = "Documento médico válido emitido por VetDosis Pro. Válido únicamente con firma autógrafa del MVZ responsable."
        val noticeWidth = paint.measureText(notice)
        canvas.drawText(notice, (595f - noticeWidth) / 2f, 810f, paint)

        document.finishPage(page)

        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "Receta_${patientName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.flush()
            outputStream.close()

            // Open or share PDF
            sharePdfFile(context, file)
            file
        } catch (e: Exception) {
            document.close()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun sharePdfFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Receta Médica Veterinaria")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir o Imprimir Receta PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "PDF guardado en: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
