package com.example.ui.screens.controlled

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
import com.example.data.local.entities.ControlledSubstanceLogEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ControlledSubstancesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.controlledLogs.collectAsState()
    var showNewLogDialog by remember { mutableStateOf(false) }

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
                        text = "Libro de Control COFEPRIS / SENASICA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Registro oficial de recetas retenidas, antibióticos y psicotrópicos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showNewLogDialog = true },
                    modifier = Modifier.testTag("add_controlled_log_button")
                ) {
                    Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Asentar Salida")
                }
            }
        }

        // Legal Notice Banner
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NORMATIVA OFICIAL MEXICANA (NOM) - SALUD & SENASICA",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Toda prescripción de Ketamina, Tramadol, Barbitúricos o Antibióticos controlados requiere folio consecutivo, receta retenida en clínica por 2 años y libro foliado.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay asientos registrados en el libro de control.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(logs) { entry ->
                ControlledLogItemCard(log = entry)
            }
        }
    }

    if (showNewLogDialog) {
        AddControlledLogDialog(
            viewModel = viewModel,
            onDismiss = { showNewLogDialog = false },
            onSave = { newLog ->
                viewModel.addControlledLog(newLog)
                showNewLogDialog = false
            }
        )
    }
}

@Composable
fun ControlledLogItemCard(log: ControlledSubstanceLogEntity) {
    val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))

    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "FOLIO: ${log.folioRecipe}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.drugName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${log.activeIngredient} • Lote: ${log.batchNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Paciente: ${log.patientName} (Prop: ${log.ownerName})", style = MaterialTheme.typography.bodySmall)
                Text("MVZ Prescriptor: ${log.doctorName} (${log.doctorLicense})", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${log.quantityDispensed} ml",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Saldo: ${log.balanceRemaining} ml",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (log.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Causa / Procedimiento: ${log.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddControlledLogDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (ControlledSubstanceLogEntity) -> Unit
) {
    var drugName by remember { mutableStateOf("Anesket 100mg/ml (Ketamina)") }
    var batchNumber by remember { mutableStateOf("LOT-2026-KT") }
    var patientName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1.5") }
    var balanceRemainingStr by remember { mutableStateOf("45.0") }
    var procedureNotes by remember { mutableStateOf("Sedación quirúrgica OVH") }

    val doctorName by viewModel.doctorName.collectAsState()
    val doctorLicense by viewModel.doctorLicense.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asentar Salida en Libro de Control") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = "Genera el asiento foliado oficial con retención de receta:",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item { OutlinedTextField(value = drugName, onValueChange = { drugName = it }, label = { Text("Fármaco Psicotrópico o Antibiótico") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Número de Lote") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Paciente") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Propietario") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Cantidad egresada (ml)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = balanceRemainingStr,
                            onValueChange = { balanceRemainingStr = it },
                            label = { Text("Saldo restante en frasco") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                item { OutlinedTextField(value = procedureNotes, onValueChange = { procedureNotes = it }, label = { Text("Motivo / Procedimiento") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isNotBlank()) {
                        val folio = "SENASICA-REC-" + SimpleDateFormat("yyyy-MMdd", Locale.getDefault()).format(Date()) + "-${(100..999).random()}"
                        onSave(
                            ControlledSubstanceLogEntity(
                                folioRecipe = folio,
                                drugName = drugName,
                                activeIngredient = drugName.substringBefore("(").trim(),
                                batchNumber = batchNumber,
                                patientName = patientName,
                                ownerName = ownerName.ifBlank { "Particular" },
                                doctorName = doctorName,
                                doctorLicense = doctorLicense,
                                quantityDispensed = quantityStr.toDoubleOrNull() ?: 1.0,
                                balanceRemaining = balanceRemainingStr.toDoubleOrNull() ?: 0.0,
                                groupType = "GRUPO_III_PSICOTROPICO",
                                notes = "$procedureNotes [Receta retenida en farmacia]"
                            )
                        )
                    }
                }
            ) {
                Text("Asentar Registro Oficial")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
