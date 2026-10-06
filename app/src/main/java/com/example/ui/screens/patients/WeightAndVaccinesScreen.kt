package com.example.ui.screens.patients

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.PatientEntity
import com.example.data.local.entities.PatientWeightEntity
import com.example.data.local.entities.VaccinationRecordEntity
import com.example.service.WhatsAppHelper
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightAndVaccinesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()

    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var weightHistory by remember { mutableStateOf<List<PatientWeightEntity>>(emptyList()) }
    var vaccineRecords by remember { mutableStateOf<List<VaccinationRecordEntity>>(emptyList()) }

    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showAddVaccineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patients) {
        if (selectedPatient == null && patients.isNotEmpty()) {
            selectedPatient = patients.first()
        }
    }

    LaunchedEffect(selectedPatient) {
        selectedPatient?.let { p ->
            viewModel.loadWeightHistory(p.id) { list ->
                weightHistory = list
            }
            viewModel.loadVaccines(p.id) { list ->
                vaccineRecords = list
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Curva de Peso y Medicina Preventiva",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Monitoreo ponderal y recordatorios automatizados de vacunas y desparasitación por WhatsApp.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Patient Selector Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Seleccionar Paciente:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedPatient?.let { "${it.name} (${it.species}) - ${it.weightKg} kg" } ?: "Seleccionar...")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.species}) - ${p.weightKg} kg") },
                                onClick = {
                                    selectedPatient = p
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Weight Curve Visual Timeline Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Evolución y Curva de Peso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Historial ponderal de ${selectedPatient?.name ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showAddWeightDialog = true },
                        modifier = Modifier.testTag("add_weight_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Registrar Peso", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (weightHistory.isEmpty()) {
                    Text("No hay registros de peso aún.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        weightHistory.forEachIndexed { idx, w ->
                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(w.dateTimestamp))
                            val prevWeight = if (idx > 0) weightHistory[idx - 1].weightKg else w.weightKg
                            val diff = w.weightKg - prevWeight

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(dateStr, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            if (w.notes.isNotBlank()) {
                                                Text(w.notes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${w.weightKg} kg", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        if (idx > 0 && diff != 0.0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (diff > 0) "+${String.format("%.1f", diff)}" else String.format("%.1f", diff),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (diff > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vaccines & Deworming Section
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Esquema de Vacunación y Desparasitación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Avisos automáticos directos al WhatsApp del propietario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showAddVaccineDialog = true },
                        modifier = Modifier.testTag("add_vaccine_button")
                    ) {
                        Icon(Icons.Default.AddModerator, contentDescription = "Programar Vacuna", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (vaccineRecords.isEmpty()) {
                    Text("No hay vacunas o desparasitaciones registradas.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vaccineRecords.forEach { vac ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vac.treatmentType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "Próxima dosis: ${vac.nextDueDate} • Lote: ${vac.batchNumber.ifBlank { "Vigente" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            selectedPatient?.let { p ->
                                                WhatsAppHelper.sendAppointmentReminder(
                                                    context = context,
                                                    ownerPhone = p.ownerPhone,
                                                    patientName = p.name,
                                                    dateString = vac.nextDueDate,
                                                    timeString = "En horario de consulta",
                                                    reason = vac.treatmentType,
                                                    clinicName = clinicName
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Aviso")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWeightDialog) {
        var newWeightStr by remember { mutableStateOf("28.8") }
        var notesStr by remember { mutableStateOf("Control post-tratamiento") }

        AlertDialog(
            onDismissRequest = { showAddWeightDialog = false },
            title = { Text("Registrar Nuevo Peso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newWeightStr,
                        onValueChange = { newWeightStr = it },
                        label = { Text("Peso en kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Notas de evolución") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = newWeightStr.toDoubleOrNull() ?: 10.0
                        selectedPatient?.let { p ->
                            viewModel.recordPatientWeight(p.id, w, notesStr)
                            showAddWeightDialog = false
                            Toast.makeText(context, "Peso guardado", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWeightDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showAddVaccineDialog) {
        var treatmentType by remember { mutableStateOf("Vacuna Antirrábica Anual") }
        var nextDueDate by remember { mutableStateOf("2026-11-01") }
        var batchNumber by remember { mutableStateOf("LOT-RAB-2026") }

        AlertDialog(
            onDismissRequest = { showAddVaccineDialog = false },
            title = { Text("Programar Vacuna / Desparasitación") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = treatmentType, onValueChange = { treatmentType = it }, label = { Text("Tipo de Vacuna / Desparasitante") })
                    OutlinedTextField(value = nextDueDate, onValueChange = { nextDueDate = it }, label = { Text("Fecha Próxima (YYYY-MM-DD)") })
                    OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Lote del Biológico") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPatient?.let { p ->
                            viewModel.addVaccineRecord(
                                VaccinationRecordEntity(
                                    patientId = p.id,
                                    patientName = p.name,
                                    treatmentType = treatmentType,
                                    appliedDate = "Hoy",
                                    nextDueDate = nextDueDate,
                                    batchNumber = batchNumber
                                )
                            )
                            showAddVaccineDialog = false
                        }
                    }
                ) {
                    Text("Programar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVaccineDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
