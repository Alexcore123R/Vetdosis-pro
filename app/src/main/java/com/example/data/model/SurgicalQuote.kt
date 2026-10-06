package com.example.data.model

data class QuoteItem(
    val name: String,
    val category: String, // "Honorarios", "Anestesia", "Material", "Hospitalización"
    val cost: Double
)

data class SurgicalQuote(
    val procedureName: String,
    val patientName: String,
    val species: String,
    val weightKg: Double,
    val ownerName: String,
    val items: List<QuoteItem>,
    val notes: String = ""
) {
    val totalCost: Double
        get() = items.sumOf { it.cost }

    fun formatForWhatsApp(clinicName: String, doctorName: String): String {
        return buildString {
            appendLine("📋 *COTIZACIÓN QUIRÚRGICA FORMAL*")
            appendLine("🏥 *$clinicName*")
            appendLine("👨‍⚕️ MVZ: $doctorName")
            appendLine("----------------------------------------")
            appendLine("🐾 *Paciente:* $patientName ($species, ${weightKg}kg)")
            appendLine("🩺 *Procedimiento:* $procedureName")
            appendLine("----------------------------------------")
            appendLine("💵 *DESGLOSE DE CONCEPTOS:*")
            items.forEach { item ->
                appendLine("• ${item.name} (${item.category}): $${String.format(java.util.Locale.US, "%.2f", item.cost)}")
            }
            appendLine("----------------------------------------")
            appendLine("💰 *TOTAL ESTIMADO:* $${String.format(java.util.Locale.US, "%.2f", totalCost)} MXN")
            appendLine("----------------------------------------")
            if (notes.isNotBlank()) {
                appendLine("ℹ️ *Notas:* $notes")
            }
            appendLine("⚠️ Incluye fármacos anestésicos intraoperatorios, monitoreo continuo y material estéril.")
            appendLine("Para confirmar y apartar fecha de quirófano, responda a este mensaje.")
        }
    }
}
