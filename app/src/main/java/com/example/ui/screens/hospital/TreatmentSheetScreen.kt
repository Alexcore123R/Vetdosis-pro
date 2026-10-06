package com.example.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.TreatmentSheetRecordEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TreatmentSheetScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.treatmentSheetRecords.collectAsState()
    val activeStaff by viewModel.activeStaffName.collectAsState()
    var selectedHourFilter by remember { mutableStateOf("TODAS") }
    var showAddDialog by remember { mutableStateOf(false) }

    val hoursList = listOf("TODAS", "08:00", "12:00", "14:00", "16:00", "18:00", "20:00", "00:00")

    val filteredRecords = remember(records, selectedHourFilter) {
        if (selectedHourFilter == "TODAS") records
        else records.filter { it.hourSlot == selectedHourFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hoja de Tratamientos (Matriz Horaria)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Control en tiempo real: Quién aplicó, cuándo y dosis exacta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_treatment_record_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Horario")
                }
            }
        }

        // Hour Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(hoursList) { h ->
                    FilterChip(
                        selected = h == selectedHourFilter,
                        onClick = { selectedHourFilter = h },
                        label = { Text(h) }
                    )
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay registros de medicación para este bloque horario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredRecords) { rec ->
                TreatmentRowCard(
                    record = rec,
                    currentStaff = activeStaff,
                    onToggleAdminister = {
                        val isNowDone = !rec.isDone
                        val now = System.currentTimeMillis()
                        viewModel.updateTreatmentSheetRecord(
                            rec.copy(
                                isDone = isNowDone,
                                administeredBy = if (isNowDone) activeStaff else "",
                                administeredTimestamp = if (isNowDone) now else 0L
                            )
                        )
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddTreatmentItemDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { newRecord ->
                viewModel.addTreatmentSheetRecord(newRecord)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TreatmentRowCard(
    record: TreatmentSheetRecordEntity,
    currentStaff: String,
    onToggleAdminister: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (record.isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (record.isDone) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = record.hourSlot,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (record.isDone) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${record.patientName} (${record.cageBox})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💊 ${record.medicationName} — Dosis: ${record.doseMl} (${record.route})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (record.isDone) {
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.administeredTimestamp))
                    Text(
                        text = "✔ Aplicado a las $timeStr por: ${record.administeredBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "⏳ Pendiente de aplicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Checkbox(
                checked = record.isDone,
                onCheckedChange = { onToggleAdminister() }
            )
        }
    }
}

@Composable
fun AddTreatmentItemDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (TreatmentSheetRecordEntity) -> Unit
) {
    var patientName by remember { mutableStateOf("Rocky") }
    var cageBox by remember { mutableStateOf("Jaula Caninos 02") }
    var medicationName by remember { mutableStateOf("Meloxivet 5%") }
    var doseMl by remember { mutableStateOf("1.14 ml") }
    var route by remember { mutableStateOf("SC") }
    var hourSlot by remember { mutableStateOf("18:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Medicamento a Hoja Horaria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Paciente") })
                OutlinedTextField(value = cageBox, onValueChange = { cageBox = it }, label = { Text("Jaula / Box") })
                OutlinedTextField(value = medicationName, onValueChange = { medicationName = it }, label = { Text("Medicamento") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = doseMl, onValueChange = { doseMl = it }, label = { Text("Volumen ml") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Vía") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = hourSlot, onValueChange = { hourSlot = it }, label = { Text("Horario (ej. 18:00)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isNotBlank() && medicationName.isNotBlank()) {
                        onSave(
                            TreatmentSheetRecordEntity(
                                hospitalPatientId = 1L,
                                patientName = patientName,
                                cageBox = cageBox,
                                medicationName = medicationName,
                                doseMl = doseMl,
                                route = route,
                                hourSlot = hourSlot
                            )
                        )
                    }
                }
            ) {
                Text("Asignar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
