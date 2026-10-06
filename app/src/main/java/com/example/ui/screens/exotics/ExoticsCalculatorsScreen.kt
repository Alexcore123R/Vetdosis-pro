package com.example.ui.screens.exotics

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
import com.example.data.model.ExoticSafetyAndMetabolism
import com.example.data.model.ExoticTaxonGroup
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExoticsCalculatorsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Peso Metabólico", "Toxicidades & Contraindicaciones", "Fluidoterapia Exóticos")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Tab Row
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
                    modifier = Modifier.testTag("exotic_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> MetabolicScalingSection()
            1 -> ToxicityAlertsSection()
            2 -> ExoticFluidsSection()
        }
    }
}

@Composable
private fun MetabolicScalingSection() {
    var weightGramsStr by remember { mutableStateOf("350.0") }
    var selectedTaxon by remember { mutableStateOf(ExoticTaxonGroup.EXOTIC_MAMMALS) }

    val weightGrams = weightGramsStr.toDoubleOrNull() ?: 350.0
    val result = remember(weightGrams, selectedTaxon) {
        ExoticSafetyAndMetabolism.calculateMetabolicScaling(weightGrams, selectedTaxon)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Escalamiento por Peso Metabólico (Kleiber: BMR = K · W^0.75)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "En animales de compañía no convencionales y fauna silvestre, dosificar por mg/kg lineal subestima las necesidades posológicas o hídricas debido a su metabolismo acelerado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Seleccionar Grupo Taxonómico (Constante K):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExoticTaxonGroup.values().forEach { taxon ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedTaxon == taxon,
                            onClick = { selectedTaxon = taxon },
                            modifier = Modifier.testTag("taxon_${taxon.name}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(taxon.label, style = MaterialTheme.typography.bodySmall, fontWeight = if (selectedTaxon == taxon) FontWeight.Bold else FontWeight.Normal)
                            Text("K = ${taxon.kConstant.toInt()} kcal/día • Requerimiento hídrico base: ${taxon.dailyWaterMlKg.toInt()} ml/kg/día", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = weightGramsStr,
                    onValueChange = { weightGramsStr = it },
                    label = { Text("Peso del Paciente en Gramos (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("exotic_weight_input")
                )
            }
        }

        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "Valores Fisiometabólicos Calculados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Peso en kg:", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.3f", result.weightKg)} kg (${result.weightGrams.toInt()} g)", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tasa Metabólica Basal (BMR):", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.basalMetabolicRateKcalDay)} kcal/día", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Energía de Mantenimiento (MER):", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.maintenanceEnergyKcalDay)} kcal/día", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fluidos de Mantenimiento:", style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", result.dailyFluidMaintenanceMl)} ml / 24h", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0077B6))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToxicityAlertsSection() {
    val alerts = ExoticSafetyAndMetabolism.toxicityAlerts

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Guía Crítica de Fármacos Contraindicados en Fauna y Exóticos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Precaución: El uso de dosis de perros/gatos en lagomorfos, roedores y reptiles puede desencadenar letalidad fulminante.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(alerts) { alert ->
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (alert.severityLevel.contains("MORTAL")) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                borderColor = if (alert.severityLevel.contains("MORTAL")) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = alert.drugName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = alert.severityLevel,
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Especies Prohibidas: ${alert.contraindicatedSpecies}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fisiopatología: ${alert.pathophysiologicalMechanism}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ Alternativa terapéutica segura: ${alert.saferAlternative}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExoticFluidsSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Protocolos de Fluidoterapia y Vías en Exóticos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vía Intracelómica (ICe) - Reptiles y Quelonios",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Indicada en deshidratación moderada a severa cuando la cateterización venosa es imposible. Inyección en fosa prefemoral craneal a la extremidad posterior en tortugas. Evitar vejiga y pulmones. Calentar siempre los fluidos a 28-30°C.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vía Intraósea (IO) - Aves y Pequeños Mamíferos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sitios de elección en aves: Cúbito distal o Tibiotarso proximal. CRÍTICO: NUNCA usar Húmero ni Fémur en aves (son huesos neumáticos comunicados con los sacos aéreos; infundir fluidos aquí ahogaría al ave por inundación pulmonar).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vía Subcutánea (SC) - Conejos y Roedores",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0077B6)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Volumen máximo: 10-20 ml/kg por sitio anatómico en la región interescapular o flancos. Usar soluciones isotónicas (Normosol-R, Ringer Lactato, NaCl 0.9%). Calentar fluidos a temperatura corporal (38-39°C).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
