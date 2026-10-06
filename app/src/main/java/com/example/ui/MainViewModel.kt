package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import com.example.data.remote.MedicationAnalysisResult
import com.example.data.remote.SoapNotesResult
import com.example.service.AlarmSoundManager
import com.example.service.AudioRecorderHelper
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val prefs: SharedPreferences = application.getSharedPreferences("vet_dosis_prefs", Context.MODE_PRIVATE)

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Clinical Branch Selector (Pequeñas Especies, Exóticos, Grandes Especies/Producción, Gestión Clínica)
    val selectedBranch = MutableStateFlow(
        try {
            ClinicalBranch.valueOf(prefs.getString("selected_branch", ClinicalBranch.SMALL_ANIMALS.name) ?: ClinicalBranch.SMALL_ANIMALS.name)
        } catch (e: Exception) {
            ClinicalBranch.SMALL_ANIMALS
        }
    )

    fun selectBranch(branch: ClinicalBranch) {
        selectedBranch.value = branch
        prefs.edit().putString("selected_branch", branch.name).apply()
        when (branch) {
            ClinicalBranch.SMALL_ANIMALS -> calcSpecies.value = AnimalSpecies.CANINE
            ClinicalBranch.EXOTICS -> calcSpecies.value = AnimalSpecies.RABBIT
            ClinicalBranch.LARGE_PRODUCTION -> calcSpecies.value = AnimalSpecies.BOVINE
            ClinicalBranch.CLINIC_SUITE -> {}
        }
    }

    // Voice Recording & Live Audio Transcription using model gemini-3.5-transcribe
    val isRecordingAudio = MutableStateFlow(false)
    val isTranscribingAudio = MutableStateFlow(false)
    val lastTranscribedText = MutableStateFlow("")

    fun startVoiceRecording(context: Context): Boolean {
        val result = AudioRecorderHelper.startRecording(context)
        val success = result.isSuccess
        isRecordingAudio.value = success
        return success
    }

    fun stopVoiceRecordingAndTranscribe(
        context: Context,
        onSuccess: (String) -> Unit = {}
    ) {
        val audioFile = AudioRecorderHelper.stopRecording()
        isRecordingAudio.value = false

        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
            val fallback = "No se grabó audio válido."
            lastTranscribedText.value = fallback
            onSuccess(fallback)
            return
        }

        isTranscribingAudio.value = true
        viewModelScope.launch {
            try {
                val bytes = audioFile.readBytes()
                val result = GeminiClient.transcribeAudio(bytes, "audio/mp4")
                isTranscribingAudio.value = false
                val text = result.getOrElse { "Error de transcripción: ${it.message}" }
                lastTranscribedText.value = text
                onSuccess(text)
            } catch (e: Exception) {
                isTranscribingAudio.value = false
                val errorMsg = "Error al procesar audio: ${e.message}"
                lastTranscribedText.value = errorMsg
                onSuccess(errorMsg)
            } finally {
                try {
                    audioFile.delete()
                } catch (ignored: Exception) {}
            }
        }
    }

    // Active Staff & Role Management
    val activeStaffName = MutableStateFlow(prefs.getString("staff_name", "MVZ. Alejandro Morales") ?: "MVZ. Alejandro Morales")
    val activeStaffRole = MutableStateFlow(prefs.getString("staff_role", "Director Médico / Administrador") ?: "Director Médico / Administrador")

    fun setActiveStaff(name: String, role: String) {
        activeStaffName.value = name
        activeStaffRole.value = role
        prefs.edit().putString("staff_name", name).putString("staff_role", role).apply()
    }

    // Theme & Settings State
    val selectedTheme = MutableStateFlow(
        try {
            AppColorTheme.valueOf(prefs.getString("app_theme", AppColorTheme.EMERALD_VET.name) ?: AppColorTheme.EMERALD_VET.name)
        } catch (e: Exception) {
            AppColorTheme.EMERALD_VET
        }
    )
    val isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))

    // Clinic Profile for Letterhead & Recetas (COFEPRIS / SENASICA)
    val clinicName = MutableStateFlow(prefs.getString("clinic_name", "Hospital Veterinario San Francisco") ?: "Hospital Veterinario San Francisco")
    val doctorName = MutableStateFlow(prefs.getString("doctor_name", "Alejandro Morales") ?: "Alejandro Morales")
    val doctorLicense = MutableStateFlow(prefs.getString("doctor_license", "CED-VET-982341-MX") ?: "CED-VET-982341-MX")
    val clinicPhone = MutableStateFlow(prefs.getString("clinic_phone", "+52 55 1234 5678") ?: "+52 55 1234 5678")
    val clinicAddress = MutableStateFlow(prefs.getString("clinic_address", "Av. Insurgentes Sur 1450, CDMX") ?: "Av. Insurgentes Sur 1450, CDMX")

    fun saveClinicProfile(name: String, doctor: String, license: String, phone: String, address: String) {
        clinicName.value = name
        doctorName.value = doctor
        doctorLicense.value = license
        clinicPhone.value = phone
        clinicAddress.value = address

        prefs.edit()
            .putString("clinic_name", name)
            .putString("doctor_name", doctor)
            .putString("doctor_license", license)
            .putString("clinic_phone", phone)
            .putString("clinic_address", address)
            .apply()
    }

    fun setTheme(theme: AppColorTheme) {
        selectedTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    // ----------------------------------------------------
    // Database Flows
    // ----------------------------------------------------
    val patients: StateFlow<List<PatientEntity>> = db.patientDao().getAllPatients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drugs: StateFlow<List<DrugEntity>> = db.drugDao().getAllDrugs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hospitalizedPatients: StateFlow<List<HospitalPatientEntity>> = db.hospitalDao().getAllHospitalized()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatmentSheetRecords: StateFlow<List<TreatmentSheetRecordEntity>> = db.hospitalDao().getAllTreatmentSheetRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryProducts: StateFlow<List<ProductEntity>> = db.inventoryDao().getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = db.inventoryDao().getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val controlledLogs: StateFlow<List<ControlledSubstanceLogEntity>> = db.inventoryDao().getAllControlledLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = db.inventoryDao().getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = db.appointmentDao().getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffProfiles: StateFlow<List<StaffEmergencyProfileEntity>> = db.staffDao().getAllStaffProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clinicTasks: StateFlow<List<ClinicTaskEntity>> = db.clinicOperationsDao().getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMessages: StateFlow<List<ClinicTeamMessageEntity>> = db.clinicOperationsDao().getAllTeamMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groomingAppointments: StateFlow<List<GroomingAppointmentEntity>> = db.clinicOperationsDao().getAllGroomingAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clinicServices: StateFlow<List<ClinicServiceEntity>> = db.clinicOperationsDao().getAllServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffCommissions: StateFlow<List<StaffPerformanceCommissionEntity>> = db.clinicOperationsDao().getAllCommissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------------------------------------------
    // Dose Calculator State
    // ----------------------------------------------------
    val calcSpecies = MutableStateFlow(AnimalSpecies.CANINE)
    val calcWeightInput = MutableStateFlow("15.0")
    val calcDrugQuery = MutableStateFlow("")
    val calcSelectedDrug = MutableStateFlow<DrugEntity?>(null)
    val calcCustomDoseMgKg = MutableStateFlow("")
    val calcCustomConcentration = MutableStateFlow("")
    val calcFrequency = MutableStateFlow(AdministrationFrequency.BID)
    val calcRoute = MutableStateFlow(AdministrationRoute.ORAL)
    val calcDurationDays = MutableStateFlow(5)
    val calcPatientName = MutableStateFlow("")

    val doseCalculationResult: StateFlow<DoseCalculationResult?> = combine(
        calcSpecies,
        calcWeightInput,
        calcSelectedDrug,
        calcCustomDoseMgKg,
        calcCustomConcentration,
        calcFrequency,
        calcRoute,
        calcDurationDays
    ) { params ->
        val species = params[0] as AnimalSpecies
        val weightStr = params[1] as String
        val drug = params[2] as? DrugEntity
        val customDoseStr = params[3] as String
        val customConcStr = params[4] as String
        val freq = params[5] as AdministrationFrequency
        val route = params[6] as AdministrationRoute
        val days = params[7] as Int

        val weight = weightStr.toDoubleOrNull() ?: 0.0
        if (weight <= 0.0) return@combine null

        val doseMgKg = customDoseStr.toDoubleOrNull() ?: drug?.defaultDoseMgKg ?: 0.0
        val concMgMl = customConcStr.toDoubleOrNull() ?: drug?.concentrationMgMl ?: 0.0

        if (doseMgKg <= 0.0 || concMgMl <= 0.0) return@combine null

        val totalMg = weight * doseMgKg
        val volumeMl = totalMg / concMgMl

        val drugName = drug?.name ?: "Fármaco personalizado"
        val active = drug?.activeIngredient ?: "Principio activo"

        val instructions = "Administrar ${SafetyDoseGuard.formatSafe(volumeMl)} ml (${SafetyDoseGuard.formatSafe(totalMg)} mg) ${freq.label} por ${route.label} durante $days días."

        DoseCalculationResult(
            drugName = drugName,
            activeIngredient = active,
            species = species,
            weightKg = weight,
            doseMgKg = doseMgKg,
            concentrationMgMl = concMgMl,
            totalDoseMg = totalMg,
            totalVolumeMl = volumeMl,
            frequency = freq,
            route = route,
            durationDays = days,
            instructions = instructions,
            warnings = drug?.contraindications
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDrugForCalculation(drug: DrugEntity) {
        calcSelectedDrug.value = drug
        calcDrugQuery.value = drug.name
        calcCustomDoseMgKg.value = drug.defaultDoseMgKg.toString()
        calcCustomConcentration.value = drug.concentrationMgMl.toString()
    }

    fun addNewCustomDrug(drug: DrugEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.drugDao().insertDrug(drug)
        }
    }

    // ----------------------------------------------------
    // Weights & Preventive Medicine
    // ----------------------------------------------------
    fun recordPatientWeight(patientId: Long, weightKg: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().insertWeightRecord(
                PatientWeightEntity(
                    patientId = patientId,
                    weightKg = weightKg,
                    notes = notes
                )
            )
            // Also update current patient weight
            val currentPatient = db.patientDao().getPatientById(patientId)
            currentPatient?.let {
                db.patientDao().updatePatient(it.copy(weightKg = weightKg))
            }
        }
    }

    fun loadWeightHistory(patientId: Long, onResult: (List<PatientWeightEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().getWeightHistory(patientId).collect {
                onResult(it)
            }
        }
    }

    fun addVaccineRecord(record: VaccinationRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().insertVaccination(record)
        }
    }

    fun loadVaccines(patientId: Long, onResult: (List<VaccinationRecordEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().getVaccinationRecords(patientId).collect {
                onResult(it)
            }
        }
    }

    // ----------------------------------------------------
    // Informed Consents
    // ----------------------------------------------------
    fun saveConsent(consent: InformedConsentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().insertConsent(consent)
        }
    }

    // ----------------------------------------------------
    // Controlled Substances Book (COFEPRIS / SENASICA)
    // ----------------------------------------------------
    fun addControlledLog(log: ControlledSubstanceLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.inventoryDao().insertControlledLog(log)
        }
    }

    // ----------------------------------------------------
    // Treatment Sheet (Matriz Paciente x Hora)
    // ----------------------------------------------------
    fun addTreatmentSheetRecord(record: TreatmentSheetRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.hospitalDao().insertTreatmentSheetItem(record)
        }
    }

    fun updateTreatmentSheetRecord(record: TreatmentSheetRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.hospitalDao().updateTreatmentSheetItem(record)
        }
    }

    // ----------------------------------------------------
    // Hospitalization & Alarms
    // ----------------------------------------------------
    fun checkAndTriggerMedicationAlarm(context: Context) {
        AlarmSoundManager.playMedicationAlarm(context, viewModelScope)
    }

    fun markHospitalMedicationAdministered(patient: HospitalPatientEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = patient.copy(
                isDone = true,
                isDue = false,
                notes = "${patient.notes} [Administrado por ${activeStaffName.value} a las ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}]"
            )
            db.hospitalDao().updateHospitalPatient(updated)
        }
    }

    fun addHospitalPatient(item: HospitalPatientEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.hospitalDao().insertHospitalPatient(item)
        }
    }

    // ----------------------------------------------------
    // Patients & Clinical Records
    // ----------------------------------------------------
    val selectedPatient = MutableStateFlow<PatientEntity?>(null)
    val patientConsultations = MutableStateFlow<List<ConsultationHistoryEntity>>(emptyList())

    fun selectPatient(patient: PatientEntity) {
        selectedPatient.value = patient
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().getConsultationHistory(patient.id).collect {
                patientConsultations.value = it
            }
        }
    }

    fun savePatient(patient: PatientEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = db.patientDao().insertPatient(patient)
            if (selectedPatient.value?.id == patient.id) {
                selectedPatient.value = patient.copy(id = if (patient.id == 0L) id else patient.id)
            }
        }
    }

    fun addConsultation(consult: ConsultationHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.patientDao().insertConsultation(consult)
        }
    }

    // ----------------------------------------------------
    // AI Chat & Dictation with Gemini
    // ----------------------------------------------------
    val chatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "model" to "¡Hola, Colega! Soy tu Asistente Clínico de VetDosis Pro. Puedo orientarte en farmacología, interacciones, toxicología, protocolos anestésicos y ajustes posológicos por especie. ¿En qué paciente o caso podemos trabajar hoy?"
        )
    )
    val isChatLoading = MutableStateFlow(false)

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val current = chatMessages.value.toMutableList()
        current.add("user" to userText)
        chatMessages.value = current
        isChatLoading.value = true

        viewModelScope.launch {
            val result = GeminiClient.chatWithAiVet(current, userText)
            isChatLoading.value = false
            val responseText = result.getOrElse { "Error al conectar con Gemini: ${it.message}" }
            val updated = chatMessages.value.toMutableList()
            updated.add("model" to responseText)
            chatMessages.value = updated
        }
    }

    fun clearChat() {
        chatMessages.value = listOf(
            "model" to "¡Hola, Colega! Soy tu Asistente Clínico de VetDosis Pro. Puedo orientarte en farmacología, interacciones, toxicología, protocolos anestésicos y ajustes posológicos por especie. ¿En qué paciente o caso podemos trabajar hoy?"
        )
    }

    // AI SOAP Audio/Text Dictation
    val isSoapProcessing = MutableStateFlow(false)
    val lastSoapResult = MutableStateFlow<SoapNotesResult?>(null)

    fun processSoapDictation(notesText: String, onComplete: (SoapNotesResult) -> Unit) {
        isSoapProcessing.value = true
        viewModelScope.launch {
            val res = GeminiClient.transcribeOrFormatSoapNotes(notesText)
            isSoapProcessing.value = false
            res.onSuccess {
                lastSoapResult.value = it
                onComplete(it)
            }
        }
    }

    // Image Label Scanner
    val isImageAnalyzing = MutableStateFlow(false)
    val analyzedMedication = MutableStateFlow<MedicationAnalysisResult?>(null)

    fun analyzeMedicationPhoto(bitmap: Bitmap) {
        isImageAnalyzing.value = true
        viewModelScope.launch {
            val res = GeminiClient.analyzeMedicationImage(bitmap)
            isImageAnalyzing.value = false
            res.onSuccess {
                analyzedMedication.value = it
                // Pre-fill calculator
                calcDrugQuery.value = it.drugName
                if (it.suggestedDoseMgKg > 0) {
                    calcCustomDoseMgKg.value = it.suggestedDoseMgKg.toString()
                }
            }
        }
    }

    // ----------------------------------------------------
    // Prescriptions & Official PDF / WhatsApp (COFEPRIS Controlled)
    // ----------------------------------------------------
    val prescriptionItems = MutableStateFlow<List<String>>(
        listOf(
            "Meloxivet 5mg/ml: Administrar 1.14 ml SC cada 24 horas por 4 días.",
            "Ondansetrón 2mg/ml: Administrar 4.2 ml IV cada 8 horas por 3 días."
        )
    )
    val prescriptionCareNotes = MutableStateFlow(
        "Ofrecer dieta blanda gastroentérica en porciones pequeñas. Mantener agua fresca a libre acceso. Reposo relativo."
    )
    val isControlledRecipe = MutableStateFlow(false)
    val controlledFolio = MutableStateFlow("SENASICA-REC-2026-0042")

    fun addPrescriptionItem(item: String) {
        prescriptionItems.value = prescriptionItems.value + item
    }

    fun removePrescriptionItem(index: Int) {
        prescriptionItems.value = prescriptionItems.value.toMutableList().also { it.removeAt(index) }
    }

    fun exportPrescriptionPdf(context: Context, patient: PatientEntity?): File? {
        val pName = patient?.name ?: calcPatientName.value.ifBlank { "Paciente General" }
        val pSpecies = patient?.species ?: calcSpecies.value.displayName
        val pBreed = patient?.breed ?: "No especificada"
        val pAge = "${patient?.ageMonths ?: 24} meses"
        val pWeight = patient?.weightKg ?: (calcWeightInput.value.toDoubleOrNull() ?: 10.0)
        val pOwner = patient?.ownerName ?: "Particular"

        val notesExtra = if (isControlledRecipe.value) {
            "${prescriptionCareNotes.value}\n\n[RECETA ESPECIAL CON RETENCIÓN COFEPRIS/SENASICA - FOLIO: ${controlledFolio.value}. Válida únicamente por 30 días a partir de su emisión.]"
        } else {
            prescriptionCareNotes.value
        }

        return PdfGenerator.generateAndSharePrescriptionPdf(
            context = context,
            clinicName = clinicName.value,
            doctorName = doctorName.value,
            doctorLicense = doctorLicense.value,
            clinicAddress = clinicAddress.value,
            clinicPhone = clinicPhone.value,
            patientName = pName,
            speciesName = pSpecies,
            breed = pBreed,
            age = pAge,
            weightKg = pWeight,
            ownerName = pOwner,
            prescriptions = prescriptionItems.value,
            indications = notesExtra
        )
    }

    fun sendPrescriptionWhatsApp(context: Context, patient: PatientEntity?) {
        val phone = patient?.ownerPhone ?: ""
        val pName = patient?.name ?: calcPatientName.value.ifBlank { "Paciente" }
        val pSpecies = patient?.species ?: calcSpecies.value.displayName
        val pWeight = patient?.weightKg ?: (calcWeightInput.value.toDoubleOrNull() ?: 10.0)

        val medicationText = prescriptionItems.value.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")
        val careExtra = if (isControlledRecipe.value) {
            "${prescriptionCareNotes.value}\n*(Receta Oficial Folio: ${controlledFolio.value} con retención en farmacia)*"
        } else prescriptionCareNotes.value

        WhatsAppHelper.sendPrescription(
            context = context,
            ownerPhone = phone,
            patientName = pName,
            speciesName = pSpecies,
            weightKg = pWeight,
            clinicName = clinicName.value,
            doctorName = doctorName.value,
            medicationText = medicationText,
            careInstructions = careExtra
        )
    }

    // ----------------------------------------------------
    // Inventory, Sales & Cash Register
    // ----------------------------------------------------
    fun registerSale(
        itemsSummary: String,
        totalAmount: Double,
        totalCost: Double,
        paymentMethod: String,
        clientName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.inventoryDao().insertSale(
                SaleEntity(
                    totalAmount = totalAmount,
                    totalCost = totalCost,
                    paymentMethod = paymentMethod,
                    itemsSummary = itemsSummary,
                    clientName = clientName
                )
            )
        }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.inventoryDao().insertProduct(product)
        }
    }

    // AI Supplier Order Generator
    val isDraftingOrder = MutableStateFlow(false)
    val supplierOrderDraft = MutableStateFlow("")

    fun draftSupplierOrder(supplierName: String, supplierPhone: String, productsToOrder: List<String>) {
        isDraftingOrder.value = true
        viewModelScope.launch {
            val res = GeminiClient.draftSupplierPurchaseOrder(supplierName, supplierPhone, productsToOrder)
            isDraftingOrder.value = false
            supplierOrderDraft.value = res.getOrElse { "Error al redactar: ${it.message}" }
        }
    }

    // ----------------------------------------------------
    // Appointments
    // ----------------------------------------------------
    fun addAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.appointmentDao().insertAppointment(appointment)
        }
    }

    // ----------------------------------------------------
    // Staff Emergency Profiles
    // ----------------------------------------------------
    fun addStaffProfile(profile: StaffEmergencyProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.staffDao().insertStaffProfile(profile)
        }
    }

    fun updateStaffProfile(profile: StaffEmergencyProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.staffDao().updateStaffProfile(profile)
        }
    }

    fun deleteStaffProfile(profile: StaffEmergencyProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.staffDao().deleteStaffProfile(profile)
        }
    }

    // ----------------------------------------------------
    // Clinic Tasks & Daily To-Dos
    // ----------------------------------------------------
    fun addClinicTask(task: ClinicTaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().insertTask(task)
        }
    }

    fun toggleTaskCompleted(task: ClinicTaskEntity, staffName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedByStaff = if (!task.isCompleted) staffName else "",
                completedTimestamp = if (!task.isCompleted) System.currentTimeMillis() else 0L
            )
            db.clinicOperationsDao().updateTask(updated)
        }
    }

    fun deleteTask(task: ClinicTaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().deleteTask(task)
        }
    }

    // ----------------------------------------------------
    // Team Messages
    // ----------------------------------------------------
    fun sendTeamMessage(messageText: String, category: String = "GENERAL") {
        if (messageText.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().insertTeamMessage(
                ClinicTeamMessageEntity(
                    senderName = activeStaffName.value,
                    senderRole = activeStaffRole.value,
                    message = messageText.trim(),
                    category = category
                )
            )
        }
    }

    // ----------------------------------------------------
    // Grooming & Baths (Estéticas Caninas y Felinas)
    // ----------------------------------------------------
    fun addGroomingAppointment(appointment: GroomingAppointmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().insertGroomingAppointment(appointment)
        }
    }

    fun updateGroomingStatus(appointment: GroomingAppointmentEntity, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().updateGroomingAppointment(appointment.copy(status = newStatus))
            // If completed, register groomer commission automatically
            if (newStatus == "Terminada") {
                db.clinicOperationsDao().insertCommission(
                    StaffPerformanceCommissionEntity(
                        staffName = appointment.assignedGroomerStaff,
                        serviceName = "Estética: ${appointment.serviceType}",
                        serviceCategory = "ESTETICA",
                        patientName = "${appointment.patientName} (${appointment.breed})",
                        totalBilledAmount = appointment.cost,
                        commissionEarned = appointment.groomerCommission,
                        notes = "Cita de estética finalizada.",
                        isPaidOut = false
                    )
                )
            }
        }
    }

    fun deleteGroomingAppointment(appointment: GroomingAppointmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().deleteGroomingAppointment(appointment)
        }
    }

    // ----------------------------------------------------
    // Clinic Services Catalog with Editable Costs
    // ----------------------------------------------------
    fun addClinicService(service: ClinicServiceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().insertService(service)
        }
    }

    fun updateClinicService(service: ClinicServiceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().updateService(service)
        }
    }

    fun deleteClinicService(service: ClinicServiceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().deleteService(service)
        }
    }

    // ----------------------------------------------------
    // Staff Performance & Commissions (Bonos y Porcentajes)
    // ----------------------------------------------------
    fun recordStaffServiceCommission(
        staffName: String,
        serviceName: String,
        serviceCategory: String,
        patientName: String,
        totalBilled: Double,
        commissionEarned: Double,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().insertCommission(
                StaffPerformanceCommissionEntity(
                    staffName = staffName,
                    serviceName = serviceName,
                    serviceCategory = serviceCategory,
                    patientName = patientName,
                    totalBilledAmount = totalBilled,
                    commissionEarned = commissionEarned,
                    notes = notes,
                    isPaidOut = false
                )
            )
        }
    }

    fun toggleCommissionPaid(commission: StaffPerformanceCommissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clinicOperationsDao().updateCommission(commission.copy(isPaidOut = !commission.isPaidOut))
        }
    }
}
