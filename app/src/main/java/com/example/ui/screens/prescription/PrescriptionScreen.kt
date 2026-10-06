package com.example.ui.screens.prescription

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ControlledSubstanceLogEntity
import com.example.data.local.entities.PatientEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun PrescriptionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()
    val prescriptionItems by viewModel.prescriptionItems.collectAsState()
    val careNotes by viewModel.prescriptionCareNotes.collectAsState()
    val isControlled by viewModel.isControlledRecipe.collectAsState()
    val folio by viewModel.controlledFolio.collectAsState()

    val clinicName by viewModel.clinicName.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()
    val doctorLicense by viewModel.doctorLicense.collectAsState()

    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var newItemText by remember { mutableStateOf("") }
    var showAddItemDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patients) {
        if (selectedPatient == null && patients.isNotEmpty()) {
            selectedPatient = patients.first()
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
                text = "Receta Médica Membretada y Oficial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Formato estándar o especial COFEPRIS / SENASICA para control de psicotrópicos y antibióticos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action Buttons Row (Top)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.exportPrescriptionPdf(context, selectedPatient)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Imprimir PDF")
                }

                FilledTonalButton(
                    onClick = {
                        viewModel.sendPrescriptionWhatsApp(context, selectedPatient)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("send_whatsapp_prescription_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }
        }

        // COFEPRIS / SENASICA Control Switch Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (isControlled) Color(0xFF7C3AED).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                borderColor = if (isControlled) Color(0xFF7C3AED) else MaterialTheme.colorScheme.outlineVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = if (isControlled) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Receta Especial Retenida (COFEPRIS / SENASICA)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isControlled) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isControlled) "Folio: $folio • Requisito de retención en farmacia y libro de control"
                            else "Activar para antibióticos de uso restringido o psicotrópicos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isControlled,
                        onCheckedChange = { viewModel.isControlledRecipe.value = it }
                    )
                }

                if (isControlled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            selectedPatient?.let { p ->
                                viewModel.addControlledLog(
                                    ControlledSubstanceLogEntity(
                                        folioRecipe = folio,
                                        drugName = prescriptionItems.firstOrNull()?.substringBefore(":") ?: "Fármaco Controlado",
                                        activeIngredient = "Principio Activo Controlado",
                                        batchNumber = "LOT-2026-SENASICA",
                                        patientName = p.name,
                                        ownerName = p.ownerName,
                                        doctorName = doctorName,
                                        doctorLicense = doctorLicense,
                                        quantityDispensed = 1.0,
                                        balanceRemaining = 40.0,
                                        groupType = "GRUPO_III_PSICOTROPICO",
                                        notes = "Receta retenida en mostrador según NOM-059-ZOO"
                                    )
                                )
                                Toast.makeText(context, "Asentado en Libro de Control COFEPRIS", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar en Libro de Control")
                    }
                }
            }
        }

        // Patient Selector Dropdown
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Seleccionar Paciente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedPatient?.let { "${it.name} (${it.species}, ${it.weightKg}kg) - Prop: ${it.ownerName}" } ?: "Seleccionar paciente...",
                            maxLines = 1
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
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

        // Live Prescription Paper Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Letterhead Header
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = clinicName.uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "MVZ. $doctorName | Cédula Prof: $doctorLicense",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (isControlled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "FOLIO COFEPRIS / SENASICA: $folio (RECETA RETENIDA)",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Patient Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paciente: ${selectedPatient?.name ?: "General"} (${selectedPatient?.species ?: "Canino"})",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Peso: ${selectedPatient?.weightKg ?: 10.0} kg",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rx Symbol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rp /",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(onClick = { showAddItemDialog = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Agregar Fármaco", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // List of items
                    if (prescriptionItems.isEmpty()) {
                        Text(
                            text = "Presiona '+' o usa el calculador de dosis para agregar medicamentos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        prescriptionItems.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}. $item",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.removePrescriptionItem(idx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Care Instructions Field
                    Text("Indicaciones y Cuidados:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = careNotes,
                        onValueChange = { viewModel.prescriptionCareNotes.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Signature line preview
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Firma del Médico Veterinario Zootecnista", style = MaterialTheme.typography.labelSmall)
                        Text("MVZ. $doctorName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Agregar Medicamento a la Receta") },
            text = {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Fármaco, dosis, vía y frecuencia") },
                    placeholder = { Text("ej. Meloxivet 5mg/ml: 1.5 ml SC cada 24h por 4 días.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.addPrescriptionItem(newItemText)
                            newItemText = ""
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
