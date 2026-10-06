package com.example.ui.screens.production

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ProductionCalculators
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionCalculatorsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Dosis por Lote", "Agua de Bebida", "En Alimento", "Retiro SENASICA")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("prod_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> HerdBathDosingSection()
            1 -> WaterMedicationSection()
            2 -> FeedMedicationSection()
            3 -> SenasicaWithdrawalSection()
        }
    }
}

@Composable
private fun HerdBathDosingSection() {
    var headCountStr by remember { mutableStateOf("50") }
    var avgWeightStr by remember { mutableStateOf("450.0") }
    var doseMgKgStr by remember { mutableStateOf("10.0") }
    var concMgMlStr by remember { mutableStateOf("200.0") }
    var bottleSizeStr by remember { mutableStateOf("250.0") }

    val headCount = headCountStr.toIntOrNull() ?: 1
    val avgWeight = avgWeightStr.toDoubleOrNull() ?: 450.0
    val doseMgKg = doseMgKgStr.toDoubleOrNull() ?: 10.0
    val concMgMl = concMgMlStr.toDoubleOrNull() ?: 200.0
    val bottleSize = bottleSizeStr.toDoubleOrNull() ?: 250.0

    val result = remember(headCount, avgWeight, doseMgKg, concMgMl, bottleSize) {
        ProductionCalculators.calculateHerdBatchDosing(
            headCount = headCount,
            averageWeightKg = avgWeight,
            doseMgKg = doseMgKg,
            concentrationMgMl = concMgMl,
            bottleSizeMl = bottleSize
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Cálculo de Dosis por Hato / Lote (Bovinos, Ovinos, Cerdos)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Calcula la biomasa total del corral y determina cuántos frascos comerciales requieres para el tratamiento completo.",
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
                        label = { Text("Cabezas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("herd_head_input")
                    )
                    OutlinedTextField(
                        value = avgWeightStr,
                        onValueChange = { avgWeightStr = it },
                        label = { Text("Peso Promedio (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("herd_weight_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = doseMgKgStr,
                        onValueChange = { doseMgKgStr = it },
                        label = { Text("Dosis (mg/kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = concMgMlStr,
                        onValueChange = { concMgMlStr = it },
                        label = { Text("Conc. (mg/ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bottleSizeStr,
                    onValueChange = { bottleSizeStr = it },
                    label = { Text("Presentación Comercial Frasco (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "Resultado para el Hato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biomasa Total del Hato:", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.totalBiomassKg)} kg", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dosis por Animal:", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.dosePerAnimalMl)} ml / cabeza", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Volumen Total Requerido:", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.totalVolumeNeededMl)} ml", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "FRASCOS NECESARIOS: ${result.bottlesNeeded} frasco(s) de ${result.bottleSizeMl.toInt()} ml",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Sobrante remanente estimado: ${String.format("%.1f", result.leftoversMl)} ml",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterMedicationSection() {
    var headsStr by remember { mutableStateOf("100") }
    var avgWeightStr by remember { mutableStateOf("40.0") }
    var dailyWaterLitersStr by remember { mutableStateOf("4.0") }
    var tankVolumeStr by remember { mutableStateOf("1000.0") }
    var targetPpmStr by remember { mutableStateOf("100.0") }
    var productConcStr by remember { mutableStateOf("10.0") } // 10%

    val heads = headsStr.toIntOrNull() ?: 1
    val avgWeight = avgWeightStr.toDoubleOrNull() ?: 40.0
    val waterPerHead = dailyWaterLitersStr.toDoubleOrNull() ?: 4.0
    val tankVolume = tankVolumeStr.toDoubleOrNull() ?: 1000.0
    val targetPpm = targetPpmStr.toDoubleOrNull() ?: 100.0
    val productConc = productConcStr.toDoubleOrNull() ?: 10.0

    val result = remember(heads, avgWeight, waterPerHead, tankVolume, targetPpm, productConc) {
        ProductionCalculators.calculateWaterMedication(
            headCount = heads,
            averageWeightKg = avgWeight,
            dailyWaterIntakeLitersPerHead = waterPerHead,
            tankVolumeLiters = tankVolume,
            targetPpmOrMgL = targetPpm,
            productConcentrationPercent = productConc
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Medicación Masiva en Agua de Bebida (Tinacos / Tanques)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Calcula la adición precisa de antibióticos solubles (amoxicilina, tilosina, enrofloxacina) o antiparasitarios en el tinaco.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = headsStr,
                        onValueChange = { headsStr = it },
                        label = { Text("No. Animales") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dailyWaterLitersStr,
                        onValueChange = { dailyWaterLitersStr = it },
                        label = { Text("Consumo L/Cab/día") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tankVolumeStr,
                        onValueChange = { tankVolumeStr = it },
                        label = { Text("Capacidad Tinaco (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetPpmStr,
                        onValueChange = { targetPpmStr = it },
                        label = { Text("Meta ppm (mg/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = productConcStr,
                    onValueChange = { productConcStr = it },
                    label = { Text("Concentración del Producto Comercial (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color(0xFF0077B6).copy(alpha = 0.12f),
                borderColor = Color(0xFF0077B6).copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0077B6))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dosificación en Tanque de Agua",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0077B6)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Agregar a este tinaco: ${String.format("%.1f", result.productNeededForTankGramsOrMl)} ml ó g",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0077B6)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("• Consumo total del lote: ${String.format("%.0f", result.totalDailyWaterLiters)} L/día")
                Text("• Duración estimada del tinaco: ${String.format("%.1f", result.tankDurationHours)} horas")
                Text("• Producto total diario para el lote: ${String.format("%.1f", result.totalProductNeededPerDayGramsOrMl)} g o ml/día")

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedMedicationSection() {
    var headsStr by remember { mutableStateOf("200") }
    var feedPerHeadStr by remember { mutableStateOf("1.5") } // kg/head/day
    var gramsPerTonStr by remember { mutableStateOf("300.0") } // 300g of active per ton
    var tonsToMixStr by remember { mutableStateOf("5.0") }
    var premixConcStr by remember { mutableStateOf("20.0") } // 20% premix

    val heads = headsStr.toIntOrNull() ?: 1
    val feedPerHead = feedPerHeadStr.toDoubleOrNull() ?: 1.5
    val gramsPerTon = gramsPerTonStr.toDoubleOrNull() ?: 300.0
    val tonsToMix = tonsToMixStr.toDoubleOrNull() ?: 5.0
    val premixConc = premixConcStr.toDoubleOrNull() ?: 20.0

    val result = remember(heads, feedPerHead, gramsPerTon, tonsToMix, premixConc) {
        ProductionCalculators.calculateFeedMedication(
            headCount = heads,
            averageWeightKg = 50.0,
            dailyFeedPerHeadKg = feedPerHead,
            gramsPerTon = gramsPerTon,
            tonsToMix = tonsToMix,
            premixConcentrationPercent = premixConc
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Medicación Masiva en Alimento Balanceado (Premezclas)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cálculo de inclusión de premezclas medicamentosas por tonelada elaborada en tolva o planta de alimentos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tonsToMixStr,
                        onValueChange = { tonsToMixStr = it },
                        label = { Text("Toneladas a Mezclar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gramsPerTonStr,
                        onValueChange = { gramsPerTonStr = it },
                        label = { Text("Activo g/Tonelada") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = premixConcStr,
                        onValueChange = { premixConcStr = it },
                        label = { Text("Conc. Premezcla (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = headsStr,
                        onValueChange = { headsStr = it },
                        label = { Text("Animales del Hato") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color(0xFFD97706).copy(alpha = 0.12f),
                borderColor = Color(0xFFD97706).copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Premezcla Comercial Requerida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${String.format("%.2f", result.totalPremixToMixKg)} kg de premezcla",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD97706)
                )
                Text(
                    text = "para mezclar en ${String.format("%.1f", tonsToMix)} toneladas de alimento.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Dosis activa ingerida estimada: ${String.format("%.1f", result.dailyActiveIngConsumedPerHeadMg)} mg/cabeza/día")
                Text("• Consumo total de alimento del lote: ${String.format("%.1f", result.totalFeedDailyKg)} kg/día")

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SenasicaWithdrawalSection() {
    val items = ProductionCalculators.withdrawalPeriods

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Tiempos de Retiro Oficiales (Carne y Leche - SENASICA)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cumplimiento de la NOM-064-ZOO y regulaciones de inocuidad agroalimentaria en México.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(items) { info ->
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.drugName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Especies: ${info.targetSpecies}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🥩 Carne: ${info.meatWithdrawalDays} días",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🥛 Leche: ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = info.milkWithdrawalHoursOrDays,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (info.milkWithdrawalHoursOrDays.contains("PROHIBIDO", ignoreCase = true) || info.milkWithdrawalHoursOrDays.contains("NO", ignoreCase = true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Regulación: ${info.senasicaRegulations}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
