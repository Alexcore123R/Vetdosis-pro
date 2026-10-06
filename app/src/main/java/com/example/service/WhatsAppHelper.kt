package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun sendPrescription(
        context: Context,
        ownerPhone: String,
        patientName: String,
        speciesName: String,
        weightKg: Double,
        clinicName: String,
        doctorName: String,
        medicationText: String,
        careInstructions: String
    ) {
        val cleanPhone = ownerPhone.replace("[^0-9+]".toRegex(), "")
        val message = buildString {
            appendLine("📋 *RECETA MÉDICA VETERINARIA*")
            appendLine("🏥 *$clinicName*")
            appendLine("👨‍⚕️ MVZ: $doctorName")
            appendLine("----------------------------------------")
            appendLine("🐾 *Paciente:* $patientName ($speciesName)")
            appendLine("⚖️ *Peso:* $weightKg kg")
            appendLine("----------------------------------------")
            appendLine("💊 *INDICACIONES DE MEDICACIÓN:*")
            appendLine(medicationText)
            appendLine("----------------------------------------")
            if (careInstructions.isNotBlank()) {
                appendLine("⚠️ *CUIDADOS Y RECOMENDACIONES:*")
                appendLine(careInstructions)
                appendLine("----------------------------------------")
            }
            appendLine("📞 Ante cualquier duda o reacción adversa, comuníquese de inmediato a nuestra clínica.")
            appendLine("¡Deseamos una pronta recuperación para $patientName! ❤️")
        }

        openWhatsApp(context, cleanPhone, message)
    }

    fun sendAppointmentReminder(
        context: Context,
        ownerPhone: String,
        patientName: String,
        dateString: String,
        timeString: String,
        reason: String,
        clinicName: String
    ) {
        val cleanPhone = ownerPhone.replace("[^0-9+]".toRegex(), "")
        val message = buildString {
            appendLine("🔔 *RECORDATORIO DE CITA VETERINARIA*")
            appendLine("Hola, te recordamos tu próxima cita en *$clinicName*:")
            appendLine("🐾 *Paciente:* $patientName")
            appendLine("📅 *Fecha:* $dateString")
            appendLine("⏰ *Hora:* $timeString")
            appendLine("🩺 *Motivo:* $reason")
            appendLine("\nPor favor confirma tu asistencia respondiendo a este mensaje.")
        }

        openWhatsApp(context, cleanPhone, message)
    }

    fun sendSupplierOrder(
        context: Context,
        supplierPhone: String,
        orderText: String
    ) {
        val cleanPhone = supplierPhone.replace("[^0-9+]".toRegex(), "")
        openWhatsApp(context, cleanPhone, orderText)
    }

    private fun openWhatsApp(context: Context, phone: String, message: String) {
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = if (phone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$phone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
