package com.example.ui.screens.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.PrepopulatedData
import com.example.data.model.AnimalSpecies
import com.example.service.AlarmSoundManager
import com.example.ui.components.NeumorphicCard
import kotlinx.coroutines.launch

@Composable
fun PhysiologicalConstantsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSpecies by remember { mutableStateOf(AnimalSpecies.CANINE) }
    val normalRange = PrepopulatedData.physiologicalRanges[selectedSpecies] ?: PrepopulatedData.physiologicalRanges.values.first()

    // Patient live values
    var inputHr by remember { mutableStateOf("95") }
    var inputRr by remember { mutableStateOf("22") }
    var inputTemp by remember { mutableStateOf("38.5") }
    var inputCrt by remember { mutableStateOf("1.5") }
    var inputSystolic by remember { mutableStateOf("120") }

    val hrVal = inputHr.toIntOrNull()
    val rrVal = inputRr.toIntOrNull()
    val tempVal = inputTemp.toDoubleOrNull()
    val crtVal = inputCrt.toDoubleOrNull()

    // Alert evaluations
    val hrAlert = when {
        hrVal == null -> null
        hrVal > normalRange.hrMax -> "Taquicardia (> ${normalRange.hrMax} lpm)"
        hrVal < normalRange.hrMin -> "Bradicardia (< ${normalRange.hrMin} lpm)"
        else -> null
    }

    val tempAlert = when {
        tempVal == null -> null
        tempVal > normalRange.tempMax -> "Fiebre / Hipertermia (> ${normalRange.tempMax} °C)"
        tempVal < normalRange.tempMin -> "Hipotermia (< ${normalRange.tempMin} °C)"
        else -> null
    }

    val crtAlert = when {
        crtVal == null -> null
        crtVal > normalRange.crtMaxSec -> "Perfusión comprometida / Posible Shock (> 2 seg)"
        else -> null
    }

    val rrAlert = when {
        rrVal == null -> null
        rrVal > normalRange.rrMax -> "Taquipnea (> ${normalRange.rrMax} rpm)"
        rrVal < normalRange.rrMin -> "Bradipnea (< ${normalRange.rrMin} rpm)"
        else -> null
    }

    val hasCriticalAlert = hrAlert != null || tempAlert != null || crtAlert != null || rrAlert != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Monitor de Constantes Fisiológicas por Especie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Referencia rápida y evaluación en tiempo real con alerta visual de taquicardia, fiebre o hipotermia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Species Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(AnimalSpecies.values()) { spec ->
                    FilterChip(
                        selected = spec == selectedSpecies,
                        onClick = {
                            selectedSpecies = spec
                            val r = PrepopulatedData.physiologicalRanges[spec]
                            if (r != null) {
                                inputHr = ((r.hrMin + r.hrMax) / 2).toString()
                                inputRr = ((r.rrMin + r.rrMax) / 2).toString()
                                inputTemp = String.format("%.1f", (r.tempMin + r.tempMax) / 2.0)
                            }
                        },
                        label = { Text(spec.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Reference Ranges Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Valores Normales de Referencia — ${selectedSpecies.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConstantRangeItem(
                        title = "FC (Cardíaca)",
                        range = "${normalRange.hrMin} - ${normalRange.hrMax} lpm",
                        icon = Icons.Default.Favorite
                    )
                    ConstantRangeItem(
                        title = "FR (Respiratoria)",
                        range = "${normalRange.rrMin} - ${normalRange.rrMax} rpm",
                        icon = Icons.Default.Air
                    )
                    ConstantRangeItem(
                        title = "Temp. Rectal",
                        range = "${normalRange.tempMin} - ${normalRange.tempMax} °C",
                        icon = Icons.Default.Thermostat
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConstantRangeItem(
                        title = "TLLC (Capilar)",
                        range = "< ${normalRange.crtMaxSec} segundos",
                        icon = Icons.Default.Timer
                    )
                    ConstantRangeItem(
                        title = "Presión Sistólica",
                        range = "${normalRange.systolicBpMin} - ${normalRange.systolicBpMax} mmHg",
                        icon = Icons.Default.Speed
                    )
                }
            }
        }

        // Live Patient Monitor Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (hasCriticalAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Evaluación del Paciente en Vivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Digita los valores medidos en consulta o monitoreo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (hasCriticalAlert) {
                        IconButton(
                            onClick = {
                                AlarmSoundManager.playWarningBeep(context, coroutineScope)
                            }
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "Sonido Alarma",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputHr,
                        onValueChange = { inputHr = it },
                        label = { Text("FC (lpm)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vital_input_hr"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = hrAlert != null
                    )

                    OutlinedTextField(
                        value = inputRr,
                        onValueChange = { inputRr = it },
                        label = { Text("FR (rpm)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vital_input_rr"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = rrAlert != null
                    )

                    OutlinedTextField(
                        value = inputTemp,
                        onValueChange = { inputTemp = it },
                        label = { Text("Temp (°C)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vital_input_temp"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = tempAlert != null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputCrt,
                        onValueChange = { inputCrt = it },
                        label = { Text("TLLC (seg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = crtAlert != null
                    )

                    OutlinedTextField(
                        value = inputSystolic,
                        onValueChange = { inputSystolic = it },
                        label = { Text("PA Sist (mmHg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Badges
                if (hasCriticalAlert) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        hrAlert?.let {
                            AlertBadge(text = it, isCritical = true)
                        }
                        tempAlert?.let {
                            AlertBadge(text = it, isCritical = true)
                        }
                        rrAlert?.let {
                            AlertBadge(text = it, isCritical = false)
                        }
                        crtAlert?.let {
                            AlertBadge(text = it, isCritical = true)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Constantes fisiológicas dentro de rangos normales para ${selectedSpecies.displayName}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConstantRangeItem(
    title: String,
    range: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(range, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AlertBadge(text: String, isCritical: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = if (isCritical) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCritical) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error
            )
        }
    }
}
