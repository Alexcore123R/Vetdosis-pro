package com.example.ui.screens.patients

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.entities.ConsultationHistoryEntity
import com.example.data.local.entities.PatientEntity
import com.example.data.model.AnimalSpecies
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatientDetailScreen(
    viewModel: MainViewModel,
    patient: PatientEntity,
    onBack: () -> Unit,
    onCalculateDose: () -> Unit,
    onPrescribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val consultations by viewModel.patientConsultations.collectAsState()
    var showAddConsultationDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${patient.species} • ${patient.breed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Patient Overview Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Peso Clínico", style = MaterialTheme.typography.labelSmall)
                        Text("${patient.weightKg} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("Edad", style = MaterialTheme.typography.labelSmall)
                        Text("${patient.ageMonths} meses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Sexo", style = MaterialTheme.typography.labelSmall)
                        Text(patient.sex, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Propietario: ${patient.ownerName} (${patient.ownerPhone})", style = MaterialTheme.typography.bodySmall)
                if (patient.allergies.isNotBlank()) {
                    Text("Alergias: ${patient.allergies}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.calcWeightInput.value = patient.weightKg.toString()
                            viewModel.calcPatientName.value = patient.name
                            val sp = AnimalSpecies.values().find { it.displayName.contains(patient.species, ignoreCase = true) } ?: AnimalSpecies.CANINE
                            viewModel.calcSpecies.value = sp
                            onCalculateDose()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calcular Dosis")
                    }

                    OutlinedButton(
                        onClick = onPrescribe,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receta PDF")
                    }
                }
            }
        }

        // Consultations Timeline Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Clínico y Consultas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddConsultationDialog = true },
                    modifier = Modifier.testTag("add_consultation_button")
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Consulta")
                }
            }
        }

        if (consultations.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay consultas previas registradas para este paciente. Presiona 'Nueva Consulta' para registrar el examen físico o dictar la nota clínica con IA.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(consultations) { consult ->
                ConsultationHistoryCard(consult = consult)
            }
        }
    }

    if (showAddConsultationDialog) {
        AddConsultationDialog(
            viewModel = viewModel,
            patient = patient,
            onDismiss = { showAddConsultationDialog = false },
            onSave = { newConsult ->
                viewModel.addConsultation(newConsult)
                showAddConsultationDialog = false
            }
        )
    }
}

@Composable
fun ConsultationHistoryCard(consult: ConsultationHistoryEntity) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        val dateText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(consult.dateTimestamp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Motivo: ${consult.reason}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Diagnóstico: ${consult.diagnosis}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tratamiento: ${consult.treatment}",
            style = MaterialTheme.typography.bodySmall
        )

        if (consult.soapNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Nota SOAP (Asistente IA):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(consult.soapNotes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun AddConsultationDialog(
    viewModel: MainViewModel,
    patient: PatientEntity,
    onDismiss: () -> Unit,
    onSave: (ConsultationHistoryEntity) -> Unit
) {
    val context = LocalContext.current
    var reason by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var dictatedText by remember { mutableStateOf("") }
    val isProcessingSoap by viewModel.isSoapProcessing.collectAsState()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val isTranscribingAudio by viewModel.isTranscribingAudio.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.startVoiceRecording(context)
            if (!started) {
                Toast.makeText(context, "No se pudo iniciar el grabador de audio.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso de micrófono necesario para dictado por voz.", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Consulta Médica") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo de Consulta") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico Presuntivo/Definitivo") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = treatment, onValueChange = { treatment = it }, label = { Text("Tratamiento Prescrito") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dictado de Audio IA (gemini-3.5-transcribe)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {
                                if (isRecordingAudio) {
                                    viewModel.stopVoiceRecordingAndTranscribe(context) { text ->
                                        dictatedText = if (dictatedText.isBlank()) text else "$dictatedText\n$text"
                                    }
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceRecording(context)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("dialog_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isRecordingAudio) Icons.Default.StopCircle else Icons.Default.Mic,
                                contentDescription = "Grabar audio de consulta",
                                tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isRecordingAudio) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔴 Grabando voz... Toca el botón rojo para transcribir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (isTranscribingAudio) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Transcribiendo con modelo gemini-3.5-transcribe...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    OutlinedTextField(
                        value = dictatedText,
                        onValueChange = { dictatedText = it },
                        label = { Text("Notas clínicas dictadas...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                item {
                    if (isProcessingSoap) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        OutlinedButton(
                            onClick = {
                                if (dictatedText.isNotBlank()) {
                                    viewModel.processSoapDictation(dictatedText) { soap ->
                                        if (diagnosis.isBlank()) diagnosis = soap.assessment
                                        if (treatment.isBlank()) treatment = soap.plan
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Estructurar SOAP con IA")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        val soap = viewModel.lastSoapResult.value
                        val soapFormatted = if (soap != null) {
                            "S: ${soap.subjective}\nO: ${soap.objective}\nA: ${soap.assessment}\nP: ${soap.plan}"
                        } else dictatedText

                        onSave(
                            ConsultationHistoryEntity(
                                patientId = patient.id,
                                reason = reason,
                                diagnosis = diagnosis.ifBlank { "Evaluación general" },
                                treatment = treatment.ifBlank { "Manejo ambulatorio" },
                                weightAtConsultKg = patient.weightKg,
                                soapNotes = soapFormatted
                            )
                        )
                    }
                }
            ) {
                Text("Guardar Consulta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
