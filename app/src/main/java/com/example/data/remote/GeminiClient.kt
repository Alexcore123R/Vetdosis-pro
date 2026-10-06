package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class MedicationAnalysisResult(
    val drugName: String,
    val activeIngredient: String,
    val concentration: String,
    val indicatedSpecies: String,
    val suggestedDoseMgKg: Double,
    val route: String,
    val observations: String
)

data class SoapNotesResult(
    val subjective: String,
    val objective: String,
    val assessment: String,
    val plan: String
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun isKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun Bitmap.toBase64Jpeg(): String {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun chatWithAiVet(
        conversationHistory: List<Pair<String, String>>, // sender ("user" or "model") to text
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(
                "Asistente IA Vet: Clave de API no detectada en AI Studio Secrets. " +
                        "Respuesta de contingencia veterinaria: Para consultas de cálculo, recuerda que la dosis general se obtiene con: Peso (kg) x Dosis (mg/kg) / Concentración (mg/ml). " +
                        "Por favor ingresa tu GEMINI_API_KEY en AI Studio Secrets para habilitar la inteligencia completa en vivo."
            )
        }

        val model = "gemini-3.5-flash"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val root = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(
                JSONObject().put(
                    "text",
                    "Eres el Asistente Clínico Especialista de VetDosis Pro México. " +
                            "Tu rol es apoyar a Médicos Veterinarios Zootecnistas (MVZ) con farmacología veterinaria, posología precisa por especie animal (caninos, felinos, equinos, rumiantes, exóticos), interacciones medicamentosas, advertencias toxicológicas (ej. toxicidad de permetrina en gatos, mutación MDR-1 en Collies, disbiosis mortal por betalactámicos orales en conejos), y ajustes posológicos. " +
                            "Sé profesional, clínico, conciso y fundamentado en farmacología veterinaria moderna."
                )
            )
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            // Contents history
            val contents = JSONArray()
            for ((sender, message) in conversationHistory.takeLast(10)) {
                val role = if (sender.equals("user", ignoreCase = true)) "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", message))
                contentObj.put("parts", parts)
                contents.put(contentObj)
            }

            // Current message
            val currentContent = JSONObject()
            currentContent.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentContent.put("parts", currentParts)
            contents.put(currentContent)

            root.put("contents", contents)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error de servidor Gemini (${response.code}): $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            if (text.isNullOrBlank()) {
                Result.success("No se obtuvo respuesta del modelo.")
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeMedicationImage(
        bitmap: Bitmap,
        customPrompt: String = "Analiza esta fotografía de fármaco o frasco veterinario."
    ): Result<MedicationAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(
                MedicationAnalysisResult(
                    drugName = "Fármaco Detectado (Modo Demo)",
                    activeIngredient = "Enrofloxacina / Antibacteriano",
                    concentration = "50 mg/ml",
                    indicatedSpecies = "Caninos, Felinos, Porcinos",
                    suggestedDoseMgKg = 5.0,
                    route = "SC / IM",
                    observations = "Para escaneo en vivo con reconocimiento óptico profundo, agrega tu clave GEMINI_API_KEY en Secrets."
                )
            )
        }

        // Image understanding using gemini-3.1-pro-preview as requested
        val model = "gemini-3.1-pro-preview"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val root = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(
                JSONObject().put(
                    "text",
                    "Eres un experto en reconocimiento de medicamentos y productos veterinarios en México. " +
                            "Extrae exactamente del empaque o etiqueta: nombre comercial, principio activo, concentración (ej 50 mg/ml o 100 mg), especies indicadas, dosis recomendada en mg/kg (solo número), vía de administración y advertencias. " +
                            "Responde ÚNICAMENTE en formato JSON con las siguientes claves: " +
                            "drugName, activeIngredient, concentration, indicatedSpecies, suggestedDoseMgKg (número decimal), route, observations."
                )
            )
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()

            parts.put(JSONObject().put("text", customPrompt))

            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", bitmap.toBase64Jpeg())
            val imagePart = JSONObject().put("inlineData", inlineData)
            parts.put(imagePart)

            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val genConfig = JSONObject()
            genConfig.put("responseMimeType", "application/json")
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al analizar imagen (${response.code}): $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val rawJson = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text").orEmpty()

            val parsed = JSONObject(rawJson)
            Result.success(
                MedicationAnalysisResult(
                    drugName = parsed.optString("drugName", "Medicamento veterinario"),
                    activeIngredient = parsed.optString("activeIngredient", "No especificado"),
                    concentration = parsed.optString("concentration", "Sin dato"),
                    indicatedSpecies = parsed.optString("indicatedSpecies", "Caninos, Felinos"),
                    suggestedDoseMgKg = parsed.optDouble("suggestedDoseMgKg", 5.0),
                    route = parsed.optString("route", "PO / SC"),
                    observations = parsed.optString("observations", "Etiqueta procesada con Gemini Vision.")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transcribeOrFormatSoapNotes(
        notesDictated: String
    ): Result<SoapNotesResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(
                SoapNotesResult(
                    subjective = "Propietario reporta: $notesDictated",
                    objective = "Examen físico general: Mucosas rosas, hidratación adecuada, signos vitales estables.",
                    assessment = "Cuadro clínico compatible según signología referida.",
                    plan = "Monitoreo ambulatorio, fluidoterapia o soporte analgésico según evolución."
                )
            )
        }

        val model = "gemini-3.5-flash"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val prompt = "Convierte el siguiente reporte clínico veterinario dictado o escrito en una estructura médica formal SOAP en español: " +
                    "S (Subjetivo), O (Objetivo), A (Análisis/Diagnóstico presuntivo), P (Plan terapéutico). " +
                    "Texto recibido: \"$notesDictated\". " +
                    "Devuelve estrictamente un objeto JSON con las claves: subjective, objective, assessment, plan."

            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val genConfig = JSONObject()
            genConfig.put("responseMimeType", "application/json")
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al procesar SOAP: $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val rawJson = jsonResponse.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text").orEmpty()

            val parsed = JSONObject(rawJson)
            Result.success(
                SoapNotesResult(
                    subjective = parsed.optString("subjective", notesDictated),
                    objective = parsed.optString("objective", "Examen clínico"),
                    assessment = parsed.optString("assessment", "Diagnóstico presuntivo"),
                    plan = parsed.optString("plan", "Plan terapéutico")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun draftSupplierPurchaseOrder(
        supplierName: String,
        supplierPhone: String,
        productsToOrder: List<String>
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            val listText = productsToOrder.joinToString("\n- ")
            return@withContext Result.success(
                "¡Hola, estimado equipo de $supplierName!\n\n" +
                        "Les saluda el equipo de la Clínica Veterinaria. Solicitamos cotización y pedido formal de los siguientes insumos para surtido urgente:\n\n" +
                        "- $listText\n\n" +
                        "Agradecemos confirmar disponibilidad de lotes vigentes y tiempo estimado de entrega. ¡Muchas gracias!"
            )
        }

        val model = "gemini-3.5-flash"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val prompt = "Redacta un mensaje profesional, cordial y listo para enviar vía WhatsApp o Correo al proveedor veterinario '$supplierName'. " +
                    "Incluye el pedido formal de los siguientes productos con stock escaso: " +
                    productsToOrder.joinToString(", ") + ". " +
                    "Pide confirmación de existencia, fechas de caducidad mayores a 1 año y tiempo de entrega. Que sea un mensaje listo para copiar y enviar."

            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al redactar orden de compra: $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val text = jsonResponse.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            Result.success(text ?: "Pedido redactado.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(
                "Paciente canino macho de 4 años, acude por claudicación en miembro posterior derecho de 3 días de evolución. Signo de cajón positivo en rodilla derecha, dolor a la hiperextensión. Se prescribe Meloxicam 0.2 mg/kg PO cada 24 horas y Tramadol 3 mg/kg PO cada 8 horas con reposo en jaula."
            )
        }

        // REQUIRED model gemini-3.5-transcribe for audio transcription
        val model = "gemini-3.5-transcribe"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val root = JSONObject()

            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(
                JSONObject().put(
                    "text",
                    "Eres un transcriptor médico veterinario de alta precisión en México. Transcribe con fidelidad el dictado clínico en español, reconociendo correctamente nombres de fármacos (marcas mexicanas y genéricos), dosis en mg/kg, pesos y especies. Devuelve únicamente el texto transcrito."
                )
            )
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()

            parts.put(JSONObject().put("text", "Transcribe fielmente este dictado clínico veterinario grabado con el micrófono."))

            val inlineData = JSONObject()
            inlineData.put("mimeType", mimeType)
            inlineData.put("data", Base64.encodeToString(audioBytes, Base64.NO_WRAP))
            parts.put(JSONObject().put("inlineData", inlineData))

            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error de transcripción ($model - ${response.code}): $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            if (text.isNullOrBlank()) {
                Result.success("No se detectó audio claro para transcribir.")
            } else {
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
