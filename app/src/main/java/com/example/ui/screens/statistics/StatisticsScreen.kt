package com.example.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.NeumorphicStatCard

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    val hospitalized by viewModel.hospitalizedPatients.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val products by viewModel.inventoryProducts.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()

    val totalRevenue = sales.sumOf { it.totalAmount }
    val totalCost = sales.sumOf { it.totalCost }
    val netProfit = totalRevenue - totalCost

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Panel de Estadísticas y Rendimiento Clínico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Métricas de pacientes, ventas de farmacia y control de insumos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeumorphicStatCard(
                    title = "Total Pacientes",
                    value = "${patients.size}",
                    subtitle = "En expediente activo",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )

                NeumorphicStatCard(
                    title = "Hospitalizados",
                    value = "${hospitalized.count { !it.isDone }}",
                    subtitle = "Con dosis activa",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeumorphicStatCard(
                    title = "Ventas Totales",
                    value = "$${String.format("%.0f", totalRevenue)}",
                    subtitle = "${sales.size} ventas POS",
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.primary
                )

                NeumorphicStatCard(
                    title = "Ganancia Neta",
                    value = "$${String.format("%.0f", netProfit)}",
                    subtitle = "Margen de farmacia",
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Species Breakdown
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Distribución de Pacientes por Especie",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val canineCount = patients.count { it.species.contains("Canino", ignoreCase = true) || it.species.contains("Perro", ignoreCase = true) }
                val felineCount = patients.count { it.species.contains("Felino", ignoreCase = true) || it.species.contains("Gato", ignoreCase = true) }
                val otherCount = patients.size - (canineCount + felineCount)

                SpeciesDistributionBar("Caninos (Perros)", canineCount, patients.size, MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                SpeciesDistributionBar("Felinos (Gatos)", felineCount, patients.size, MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                SpeciesDistributionBar("Otras Especies (Equinos/Exóticos)", otherCount, patients.size, MaterialTheme.colorScheme.tertiary)
            }
        }

        // AI Restocking & Demand Recommendation
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recomendaciones de IA para Resurtimiento",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Alta rotación detectada en antiinflamatorios (Meloxicam 5% y Carprofeno). Se sugiere mantener lote mínimo de 15 piezas.\n" +
                            "• Temporada de alta demanda en antibióticos de amplio espectro (Amoxicilina + Clavulánico y Enrofloxacina).\n" +
                            "• Advertencia: ${lowStock.size} productos tienen inventario por debajo del umbral de seguridad.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SpeciesDistributionBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0.05f, 1f) else 0.1f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$count (${if (total > 0) (count * 100 / total) else 0}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
