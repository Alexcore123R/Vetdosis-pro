package com.example.ui.screens.emergencies

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.service.AlarmSoundManager
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyToxScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Toxicología", "RCP RECOVER", "Transfusión", "Oncología (m²)")

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
            0 -> ToxicologyTab()
            1 -> CprRecoverTab()
            2 -> BloodTransfusionTab()
            3 -> OncologyBsaTab()
        }
    }
}

@Composable
fun ToxicologyTab() {
    var dogWeightStr by remember { mutableStateOf("12.0") }
    var selectedChocolate by remember { mutableStateOf(ChocolateType.DARK_HIGH) }
    var gramsEatenStr by remember { mutableStateOf("100.0") }

    val weight = dogWeightStr.toDoubleOrNull() ?: 12.0
    val grams = gramsEatenStr.toDoubleOrNull() ?: 100.0

    val toxResult = EmergencyToxCalculator.calculateChocolateToxicity(weight, selectedChocolate, grams)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Calculadora de Toxicología por Chocolate (Teobromina)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Evaluación de riesgo tóxico según % de cacao, peso y dosis de teobromina por kg.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dogWeightStr,
                        onValueChange = { dogWeightStr = it },
                        label = { Text("Peso del perro (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = gramsEatenStr,
                        onValueChange = { gramsEatenStr = it },
                        label = { Text("Gramos ingeridos (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Tipo de Chocolate ingerido:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedChocolate.displayName)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ChocolateType.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.displayName} (~${t.theobromineMgPerGram} mg/g)") },
                                onClick = {
                                    selectedChocolate = t
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            val (cardColor, borderColor, textColor) = when (toxResult.severityLevel) {
                ToxicitySeverity.BENIGN -> Triple(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                ToxicitySeverity.MILD -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), Color(0xFFB45309))
                ToxicitySeverity.MODERATE -> Triple(Color(0xFFFED7AA), Color(0xFFEA580C), Color(0xFFC2410C))
                ToxicitySeverity.SEVERE -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Color(0xFF991B1B))
            }

            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = cardColor,
                borderColor = borderColor
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (toxResult.severityLevel == ToxicitySeverity.SEVERE) Icons.Default.Dangerous else Icons.Default.Warning,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toxResult.severityLevel.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dosis ingerida: ${SafetyDoseGuard.formatSafe(toxResult.mgPerKg)} mg/kg (${SafetyDoseGuard.formatSafe(toxResult.totalTheobromineMg)} mg totales de teobromina)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Signos Clínicos Esperados:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(toxResult.clinicalSignsExpected, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Protocolo Terapéutico de Urgencia:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(toxResult.treatmentProtocol, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Quick Antidotes reference
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Otros Tóxicos Frecuentes y Antídotos:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Rodenticidas Anticoagulantes: Vitamina K1 (Fitomenadiona) 2.5 - 5 mg/kg SC/PO durante 21-28 días.", style = MaterialTheme.typography.bodySmall)
                Text("• Permetrinas en gatos: Baño tibio con lavavajillas suave para remover cutáneo, Methocarbamol o Diazepam para temblores, lípidos intravenosos (ILE).", style = MaterialTheme.typography.bodySmall)
                Text("• Paracetamol en gatos: N-Acetilcisteína (NAC) 140 mg/kg dosis de carga, luego 70 mg/kg c/6h.", style = MaterialTheme.typography.bodySmall)
                Text("• Xilitol: Hipoglucemia severa. Bolo Dextrosa 50% 1 ml/kg diluido 1:1, luego infusión al 2.5 - 5%.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CprRecoverTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var patientWeightStr by remember { mutableStateOf("10.0") }
    var useHighDose by remember { mutableStateOf(false) }
    var isMetronomeRunning by remember { mutableStateOf(false) }

    val weight = patientWeightStr.toDoubleOrNull() ?: 10.0
    val drugs = EmergencyToxCalculator.recoverCprDrugs

    // Audio / visual compression metronome (100-120 bpm)
    LaunchedEffect(isMetronomeRunning) {
        if (isMetronomeRunning) {
            while (isMetronomeRunning) {
                AlarmSoundManager.playWarningBeep(context, coroutineScope)
                delay(550) // ~110 bpm
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Soporte Vital y RCP (Guías RECOVER Internacionales)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dosis de emergencia precalculadas para paro cardiorrespiratorio y metrónomo de compresiones a 110 lpm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weight and Metronome Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = patientWeightStr,
                        onValueChange = { patientWeightStr = it },
                        label = { Text("Peso del Paciente (kg)") },
                        modifier = Modifier.width(150.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Button(
                        onClick = { isMetronomeRunning = !isMetronomeRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMetronomeRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isMetronomeRunning) Icons.Default.Pause else Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isMetronomeRunning) "Pausar Metrónomo" else "Metrónomo 110 lpm")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = useHighDose, onCheckedChange = { useHighDose = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (useHighDose) "Dosis ALTA Epinefrina (Paro prolongado > 10 min)" else "Dosis Estándar RECOVER (Primeros ciclos)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(drugs) { drug ->
            val volMl = drug.calculateVolumeMl(weight, useHighDose && drug.drugName.contains("Epinefrina"))
            val doseMg = if (useHighDose && drug.drugName.contains("Epinefrina")) drug.highDoseMgKg * weight else drug.lowDoseMgKg * weight

            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = drug.drugName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${drug.route} • ${drug.intervalMinutes}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = drug.clinicalPurpose,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${SafetyDoseGuard.formatSafe(volMl)} ml",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${SafetyDoseGuard.formatSafe(doseMg)} mg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BloodTransfusionTab() {
    var species by remember { mutableStateOf(AnimalSpecies.CANINE) }
    var weightStr by remember { mutableStateOf("15.0") }
    var currentHtStr by remember { mutableStateOf("12.0") } // 12% severe anemia
    var targetHtStr by remember { mutableStateOf("25.0") }
    var donorHtStr by remember { mutableStateOf("40.0") }

    val w = weightStr.toDoubleOrNull() ?: 15.0
    val curHt = currentHtStr.toDoubleOrNull() ?: 12.0
    val tgtHt = targetHtStr.toDoubleOrNull() ?: 25.0
    val donHt = donorHtStr.toDoubleOrNull() ?: 40.0

    val transRes = EmergencyToxCalculator.calculateBloodTransfusion(species, w, curHt, tgtHt, donHt)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Calculadora de Transfusión Sanguínea",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Volumen de sangre entera y concentrado de eritrocitos con velocidad segura de infusión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = species == AnimalSpecies.CANINE,
                    onClick = { species = AnimalSpecies.CANINE },
                    label = { Text("Canino (90 ml/kg)") }
                )
                FilterChip(
                    selected = species == AnimalSpecies.FELINE,
                    onClick = { species = AnimalSpecies.FELINE },
                    label = { Text("Felino (66 ml/kg)") }
                )
            }
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = currentHtStr,
                        onValueChange = { currentHtStr = it },
                        label = { Text("Ht Actual %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetHtStr,
                        onValueChange = { targetHtStr = it },
                        label = { Text("Ht Meta %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = donorHtStr,
                        onValueChange = { donorHtStr = it },
                        label = { Text("Ht Donante %") },
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
                    text = "VOLUMEN TRANSFUSIONAL CALCULADO",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sangre Entera: ${SafetyDoseGuard.formatSafe(transRes.volumeWholeBloodMl)} ml",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "O Concentrado Eritrocitario (PRBC): ${SafetyDoseGuard.formatSafe(transRes.volumePackedRedCellsMl)} ml",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Velocidad Prueba (Primeros 30 min): ${SafetyDoseGuard.formatSafe(transRes.maxInfusionRateFirst30MinMlHr)} ml/h",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Velocidad Regular Mantenimiento: ${SafetyDoseGuard.formatSafe(transRes.standardRateMlHr)} ml/h",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(transRes.instructions, style = MaterialTheme.typography.bodySmall)
            }
        }

        // KCl in Fluids Warning Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color(0xFFFEF3C7)
            ) {
                Text(
                    text = "⚠️ REGLA DE SEGURIDAD PARA POTASIO (KCl):",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Nunca administrar KCl a una tasa mayor de 0.5 mEq/kg/hora. La infusión rápida de potasio provoca asistolia cardiaca irreversible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun OncologyBsaTab() {
    var species by remember { mutableStateOf(AnimalSpecies.CANINE) }
    var weightStr by remember { mutableStateOf("25.0") }

    val w = weightStr.toDoubleOrNull() ?: 25.0
    val bsaRes = EmergencyToxCalculator.calculateBsa(species, w)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Superficie Corporal (m²) para Oncología",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cálculo en metros cuadrados para fármacos quimioterápicos para evitar toxicidades severas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = species == AnimalSpecies.CANINE,
                    onClick = { species = AnimalSpecies.CANINE },
                    label = { Text("Canino (k=10.1)") }
                )
                FilterChip(
                    selected = species == AnimalSpecies.FELINE,
                    onClick = { species = AnimalSpecies.FELINE },
                    label = { Text("Felino (k=10.0)") }
                )
            }
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Peso del Paciente (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "SUPERFICIE CORPORAL ESTIMADA",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${SafetyDoseGuard.formatSafe(bsaRes.bsaM2)} m²",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(bsaRes.formulaUsed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dosis de Referencia según m²:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("• Doxorrubicina (30 mg/m²): ${SafetyDoseGuard.formatSafe(bsaRes.doxorubicinSampleDoseMg)} mg totales IV en infusión lenta con ECG", style = MaterialTheme.typography.bodySmall)
                Text("• Vincristina (0.7 mg/m²): ${SafetyDoseGuard.formatSafe(bsaRes.vincristineSampleDoseMg)} mg totales IV estricta", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
