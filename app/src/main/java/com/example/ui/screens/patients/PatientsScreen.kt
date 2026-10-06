package com.example.ui.screens.patients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ConsultationHistoryEntity
import com.example.data.local.entities.PatientEntity
import com.example.data.model.AnimalSpecies
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun PatientsScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (PatientEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(searchQuery, patients) {
        if (searchQuery.isBlank()) patients
        else patients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.ownerName.contains(searchQuery, ignoreCase = true) ||
                    it.breed.contains(searchQuery, ignoreCase = true)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expedientes y Pacientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${patients.size} pacientes registrados en clínica",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_patient_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nuevo")
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("patient_search_input"),
                placeholder = { Text("Buscar por nombre de mascota o propietario...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        if (filtered.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No se encontraron pacientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filtered) { patient ->
                PatientCardItem(
                    patient = patient,
                    onClick = {
                        viewModel.selectPatient(patient)
                        onNavigateToDetail(patient)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newPatient ->
                viewModel.savePatient(newPatient)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PatientCardItem(
    patient: PatientEntity,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${patient.weightKg} kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${patient.species} • ${patient.breed.ifBlank { "Mestizo" }} • ${patient.sex}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Propietario: ${patient.ownerName.ifBlank { "Sin asignar" }} (${patient.ownerPhone})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onSave: (PatientEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf(AnimalSpecies.CANINE) }
    var breed by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("10.0") }
    var ageMonthsStr by remember { mutableStateOf("24") }
    var sex by remember { mutableStateOf("Macho") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Paciente") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Paciente") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Text("Especie", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AnimalSpecies.values().take(4).forEach { sp ->
                            FilterChip(
                                selected = sp == selectedSpecies,
                                onClick = { selectedSpecies = sp },
                                label = { Text(sp.displayName.substringBefore(" ")) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("Peso (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = ageMonthsStr,
                            onValueChange = { ageMonthsStr = it },
                            label = { Text("Edad (meses)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Nombre del Propietario") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Teléfono WhatsApp") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Alergias o notas médicas") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            PatientEntity(
                                name = name,
                                species = selectedSpecies.displayName,
                                breed = breed,
                                weightKg = weightStr.toDoubleOrNull() ?: 10.0,
                                ageMonths = ageMonthsStr.toIntOrNull() ?: 12,
                                sex = sex,
                                ownerName = ownerName,
                                ownerPhone = ownerPhone,
                                allergies = allergies
                            )
                        )
                    }
                }
            ) {
                Text("Guardar Paciente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
