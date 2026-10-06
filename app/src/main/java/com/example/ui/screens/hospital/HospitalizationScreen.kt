package com.example.ui.screens.hospital

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
import com.example.data.local.entities.HospitalPatientEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun HospitalizationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hospitalizedList by viewModel.hospitalizedPatients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
                        text = "Hospitalización y Horarios de Medicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Avisos auditivos y control de aplicación de dosis por paciente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_hospital_patient_button")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Nuevo Paciente", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Alarm Test Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sistema de Avisos Auditivos y Notificaciones",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Emite señales acústicas de alta frecuencia para cambios de suero y administración puntual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.checkAndTriggerMedicationAlarm(context) },
                        modifier = Modifier.testTag("test_hospital_alarm_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar")
                    }
                }
            }
        }

        if (hospitalizedList.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay pacientes hospitalizados actualmente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(hospitalizedList) { item ->
                HospitalPatientCard(
                    patient = item,
                    onAdminister = { viewModel.markHospitalMedicationAdministered(item) },
                    onAlarmSound = { viewModel.checkAndTriggerMedicationAlarm(context) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddHospitalPatientDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newPatient ->
                viewModel.addHospitalPatient(newPatient)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HospitalPatientCard(
    patient: HospitalPatientEntity,
    onAdminister: () -> Unit,
    onAlarmSound: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (patient.isDone) MaterialTheme.colorScheme.outlineVariant
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = patient.cageBox,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Diagnóstico: ${patient.diagnosis}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (patient.isDone) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = if (patient.isDone) "COMPLETADO" else "HORA: ${patient.scheduledTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (patient.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "💊 Fármaco y Dosis:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = patient.doseDescription,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        if (patient.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Notas: ${patient.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!patient.isDone) {
                Button(
                    onClick = onAdminister,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Marcar Administrado")
                }

                FilledTonalIconButton(onClick = onAlarmSound) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = "Activar Alarma")
                }
            } else {
                OutlinedButton(
                    onClick = { /* already done */ },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dosis Registrada en Historial Clínico")
                }
            }
        }
    }
}

@Composable
fun AddHospitalPatientDialog(
    onDismiss: () -> Unit,
    onSave: (HospitalPatientEntity) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("CANINE") }
    var cageBox by remember { mutableStateOf("Jaula 01") }
    var diagnosis by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var doseInfo by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("16:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Ingreso a Hospitalización") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Nombre del Paciente") })
                OutlinedTextField(value = cageBox, onValueChange = { cageBox = it }, label = { Text("Jaula / Box") })
                OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico") })
                OutlinedTextField(value = doseInfo, onValueChange = { doseInfo = it }, label = { Text("Dosis y Medicación (ml / horario)") })
                OutlinedTextField(value = scheduledTime, onValueChange = { scheduledTime = it }, label = { Text("Horario de aplicación (ej. 16:00)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isNotBlank()) {
                        onSave(
                            HospitalPatientEntity(
                                patientName = patientName,
                                species = species,
                                cageBox = cageBox,
                                diagnosis = diagnosis,
                                medicationName = medication,
                                doseDescription = doseInfo,
                                scheduledTime = scheduledTime,
                                nextDoseTimestamp = System.currentTimeMillis() + 7200000L
                            )
                        )
                    }
                }
            ) {
                Text("Ingresar Paciente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
