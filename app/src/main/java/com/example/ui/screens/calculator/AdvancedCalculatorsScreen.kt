package com.example.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCalculatorsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Fluidoterapia", "CRI (Infusión)", "Exóticos (Alometría)", "Producción / Piara")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> FluidTherapyTab()
            1 -> CriTab()
            2 -> ExoticMetabolicTab()
            3 -> HerdProductionTab()
        }
    }
}

@Composable
fun FluidTherapyTab() {
    var weightInput by remember { mutableStateOf("12.0") }
    var selectedSpecies by remember { mutableStateOf(AnimalSpecies.CANINE) }
    var dehydrationPercent by remember { mutableStateOf("7.0") }
    var ongoingLosses by remember { mutableStateOf("150.0") }
    var hoursToRehydrate by remember { mutableStateOf("24") }

    val weight = weightInput.toDoubleOrNull() ?: 12.0
    val dehy = dehydrationPercent.toDoubleOrNull() ?: 0.0
    val losses = ongoingLosses.toDoubleOrNull() ?: 0.0
    val hours = hoursToRehydrate.toIntOrNull() ?: 24

    val result = FluidTherapyCalculator.calculateFluidPlan(weight, selectedSpecies, dehy, losses, hours)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Calculadora de Fluidoterapia Completa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Mantenimiento fisiológico + Déficit de deshidratación + Pérdidas continuas (vómito/diarrea).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dehydrationPercent,
                        onValueChange = { dehydrationPercent = it },
                        label = { Text("Deshidratación %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ongoingLosses,
                        onValueChange = { ongoingLosses = it },
                        label = { Text("Pérdidas continuas (ml)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hoursToRehydrate,
                        onValueChange = { hoursToRehydrate = it },
                        label = { Text("Tiempo rehidratación (h)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        // Results Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "PLAN DE FLUIDOS CALCULADO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${SafetyDoseGuard.formatSafe(result.rateMlPerHour)} ml / hora",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Volumen Total 24h: ${SafetyDoseGuard.formatSafe(result.totalVolume24hMl)} ml",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${result.normoDropsPerMin.toInt()} gtt/min",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Normogotero 20gtt",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Mantenimiento basal: ${SafetyDoseGuard.formatSafe(result.maintenance24hMl)} ml/día", style = MaterialTheme.typography.bodySmall)
                Text("• Déficit de deshidratación: ${SafetyDoseGuard.formatSafe(result.dehydrationDeficitMl)} ml", style = MaterialTheme.typography.bodySmall)
                Text("• Pérdidas continuas añadidas: ${SafetyDoseGuard.formatSafe(result.ongoingLossesMl)} ml", style = MaterialTheme.typography.bodySmall)
                Text("• Con microgotero (60 gtt/ml): ${result.microDropsPerMin.toInt()} microgotas / minuto", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CriTab() {
    var drugName by remember { mutableStateOf("Ketamina / Analgesia CRI") }
    var weightInput by remember { mutableStateOf("10.0") }
    var doseMcgInput by remember { mutableStateOf("10.0") } // 10 mcg/kg/min
    var bagVolumeInput by remember { mutableStateOf("500.0") } // 500 ml
    var fluidRateInput by remember { mutableStateOf("30.0") } // 30 ml/h
    var concInput by remember { mutableStateOf("100.0") } // 100 mg/ml Anesket

    val weight = weightInput.toDoubleOrNull() ?: 10.0
    val doseMcg = doseMcgInput.toDoubleOrNull() ?: 10.0
    val bagVol = bagVolumeInput.toDoubleOrNull() ?: 500.0
    val fluidRate = fluidRateInput.toDoubleOrNull() ?: 30.0
    val conc = concInput.toDoubleOrNull() ?: 100.0

    val cri = CriCalculator.calculateCri(weight, doseMcg, bagVol, fluidRate, conc)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Infusión Continua a Ritmo Constante (CRI)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cálculo en microgramos/kg/minuto con dosificación exacta de fármaco a mezclar en la bolsa de suero.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = drugName,
                    onValueChange = { drugName = it },
                    label = { Text("Fármaco en CRI") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = doseMcgInput,
                        onValueChange = { doseMcgInput = it },
                        label = { Text("Dosis (mcg/kg/min)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bagVolumeInput,
                        onValueChange = { bagVolumeInput = it },
                        label = { Text("Bolsa Suero (ml)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = fluidRateInput,
                        onValueChange = { fluidRateInput = it },
                        label = { Text("Fluidos (ml/h)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = concInput,
                        onValueChange = { concInput = it },
                        label = { Text("Conc (mg/ml)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "PREPARACIÓN DE CRI PARA ENFERMERÍA",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Agregar ${SafetyDoseGuard.formatSafe(cri.totalDrugMlToAdd)} ml",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${SafetyDoseGuard.formatSafe(cri.totalDrugMgToAdd)} mg de $drugName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${SafetyDoseGuard.formatSafe(cri.durationHoursOfBag)} horas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Duración de la bolsa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = cri.instructions,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ExoticMetabolicTab() {
    var speciesType by remember { mutableStateOf("Aves") }
    var weightGramsInput by remember { mutableStateOf("350.0") } // 350 gr parrot
    var doseInput by remember { mutableStateOf("10.0") }
    var concInput by remember { mutableStateOf("50.0") }

    val wGrams = weightGramsInput.toDoubleOrNull() ?: 350.0
    val dose = doseInput.toDoubleOrNull() ?: 10.0
    val conc = concInput.toDoubleOrNull() ?: 50.0

    val res = ExoticMetabolicCalculator.calculateMetabolicDose(speciesType, wGrams, dose, conc)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Ajuste por Peso Metabólico en Exóticos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Escalamiento alométrico (k × W^0.75) para reptiles, aves y micromamíferos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Aves", "Reptiles", "Pequeños Mamíferos").forEach { sp ->
                    FilterChip(
                        selected = sp == speciesType,
                        onClick = { speciesType = sp },
                        label = { Text(sp) }
                    )
                }
            }
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightGramsInput,
                        onValueChange = { weightGramsInput = it },
                        label = { Text("Peso en gramos (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = doseInput,
                        onValueChange = { doseInput = it },
                        label = { Text("Dosis mg/kg") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = concInput,
                    onValueChange = { concInput = it },
                    label = { Text("Concentración (mg/ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "DOSIS ALOMÉTRICA AJUSTADA",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${SafetyDoseGuard.formatSafe(res.volumeMl)} ml (${SafetyDoseGuard.formatSafe(res.allometricAdjustedDoseMg)} mg)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Dosis lineal sin ajuste hubiera sido: ${SafetyDoseGuard.formatSafe(res.standardDoseMg)} mg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(res.explanation, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HerdProductionTab() {
    var headCountStr by remember { mutableStateOf("60") }
    var avgWeightStr by remember { mutableStateOf("30.0") } // 30 kg pigs
    var doseMgKgStr by remember { mutableStateOf("10.0") }
    var concStr by remember { mutableStateOf("100.0") }
    var tankVolumeStr by remember { mutableStateOf("1000.0") }

    val heads = headCountStr.toIntOrNull() ?: 60
    val avgW = avgWeightStr.toDoubleOrNull() ?: 30.0
    val dose = doseMgKgStr.toDoubleOrNull() ?: 10.0
    val conc = concStr.toDoubleOrNull() ?: 100.0
    val tank = tankVolumeStr.toDoubleOrNull() ?: 1000.0

    val herdRes = HerdProductionCalculator.calculateHerdDose(heads, avgW, dose, conc, tank)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Casos de Producción: Bovinos, Ovinos y Cerdos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cálculo por lote/cabeza y dosificación masiva en agua de bebida (ppm) o pienso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = headCountStr,
                        onValueChange = { headCountStr = it },
                        label = { Text("Número de cabezas") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = avgWeightStr,
                        onValueChange = { avgWeightStr = it },
                        label = { Text("Peso promedio (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = doseMgKgStr,
                        onValueChange = { doseMgKgStr = it },
                        label = { Text("Dosis mg/kg") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = concStr,
                        onValueChange = { concStr = it },
                        label = { Text("Conc mg/ml (o mg/g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = tankVolumeStr,
                        onValueChange = { tankVolumeStr = it },
                        label = { Text("Tinaco (L)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "DOSIFICACIÓN POR LOTE Y EN AGUA",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Dosis individual: ${SafetyDoseGuard.formatSafe(herdRes.volumePerHeadMl)} ml / cabeza",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total para lote (${herdRes.totalHeads} animales): ${SafetyDoseGuard.formatSafe(herdRes.totalDrugVolumeForHerdLiters)} Litros",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💧 Medicación en Tinaco de ${tank.toInt()} L:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = herdRes.explanation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
