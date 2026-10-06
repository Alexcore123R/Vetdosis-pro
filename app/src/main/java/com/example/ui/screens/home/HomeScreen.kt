package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ClinicalBranch
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.NeumorphicStatCard

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val products by viewModel.inventoryProducts.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val hospitalized by viewModel.hospitalizedPatients.collectAsState()
    val currentStaff by viewModel.activeStaffName.collectAsState()
    val currentRole by viewModel.activeStaffRole.collectAsState()

    var showStaffSwitchDialog by remember { mutableStateOf(false) }

    val totalRevenue = sales.sumOf { it.totalAmount }
    val totalCost = sales.sumOf { it.totalCost }
    val netProfit = totalRevenue - totalCost

    // Calculate expiry alerts (30 and 60 days)
    val productsExpiring30Days = products.filter { it.daysUntilExpiry in 1..30 }
    val productsExpiring60Days = products.filter { it.daysUntilExpiry in 31..60 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Staff Profile Header with Switcher
        item {
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStaffSwitchDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                elevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentStaff,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Rol: $currentRole • Toque para cambiar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onNavigate("staff_emergency") },
                            modifier = Modifier.testTag("home_staff_emergency_btn")
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = "Ficha Médica Personal", tint = MaterialTheme.colorScheme.error)
                        }
                        Icon(Icons.Default.SwitchAccount, contentDescription = "Cambiar usuario", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Branch Selector Bar (User Request: Pequeñas Especies, Exóticos, Grandes Especies/Producción, Gestión)
        item {
            val activeBranch by viewModel.selectedBranch.collectAsState()
            Column {
                Text(
                    text = "Área Clínica Especializada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ClinicalBranch.values()) { branch ->
                        val isSelected = activeBranch == branch
                        Surface(
                            modifier = Modifier
                                .clickable { viewModel.selectBranch(branch) }
                                .testTag("home_branch_chip_${branch.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (branch) {
                                    ClinicalBranch.SMALL_ANIMALS -> Icons.Default.Pets
                                    ClinicalBranch.EXOTICS -> Icons.Default.FlutterDash
                                    ClinicalBranch.LARGE_PRODUCTION -> Icons.Default.Agriculture
                                    ClinicalBranch.CLINIC_SUITE -> Icons.Default.LocalHospital
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = branch.shortName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = branch.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Access Workflows Tailored to Active Branch
        item {
            val activeBranch by viewModel.selectedBranch.collectAsState()
            Text(
                text = "Herramientas de ${activeBranch.title}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            when (activeBranch) {
                ClinicalBranch.SMALL_ANIMALS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Dosis",
                            subtitle = "Perro / Gato",
                            icon = Icons.Default.Calculate,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("calculator") }
                        )
                        QuickActionButton(
                            title = "Sedación",
                            subtitle = "Protocolos M3",
                            icon = Icons.Default.MedicalServices,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("protocols") }
                        )
                        QuickActionButton(
                            title = "Fluidoterapia",
                            subtitle = "CRI & Goteo",
                            icon = Icons.Default.WaterDrop,
                            accentColor = Color(0xFF0077B6),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("advanced_calculators") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Constantes",
                            subtitle = "FC/FR/Temp",
                            icon = Icons.Default.MonitorHeart,
                            accentColor = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("vitals") }
                        )
                        QuickActionButton(
                            title = "Receta Oficial",
                            subtitle = "COFEPRIS PDF",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("prescription") }
                        )
                        QuickActionButton(
                            title = "Curvas & Vacunas",
                            subtitle = "Seguimiento",
                            icon = Icons.Default.Timeline,
                            accentColor = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("weights_vaccines") }
                        )
                    }
                }

                ClinicalBranch.EXOTICS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Metabolismo",
                            subtitle = "BMR K·W^0.75",
                            icon = Icons.Default.Calculate,
                            accentColor = Color(0xFF0D5C58),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("exotics_calc") }
                        )
                        QuickActionButton(
                            title = "Toxicidades",
                            subtitle = "Alertas Mortales",
                            icon = Icons.Default.Warning,
                            accentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("exotics_calc") }
                        )
                        QuickActionButton(
                            title = "Fluidos Exóticos",
                            subtitle = "Vías ICe / IO",
                            icon = Icons.Default.WaterDrop,
                            accentColor = Color(0xFF0077B6),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("exotics_calc") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Dosis Exóticos",
                            subtitle = "Aves / Reptiles",
                            icon = Icons.Default.Pets,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("calculator") }
                        )
                        QuickActionButton(
                            title = "Constantes",
                            subtitle = "Exóticos",
                            icon = Icons.Default.MonitorHeart,
                            accentColor = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("vitals") }
                        )
                        QuickActionButton(
                            title = "Asistente IA",
                            subtitle = "Fauna Silvestre",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("chat") }
                        )
                    }
                }

                ClinicalBranch.LARGE_PRODUCTION -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Dosis por Hato",
                            subtitle = "Lote / Corral",
                            icon = Icons.Default.Agriculture,
                            accentColor = Color(0xFF15803D),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("production_calc") }
                        )
                        QuickActionButton(
                            title = "Agua Bebida",
                            subtitle = "Tinaco ppm",
                            icon = Icons.Default.WaterDrop,
                            accentColor = Color(0xFF0077B6),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("production_calc") }
                        )
                        QuickActionButton(
                            title = "En Alimento",
                            subtitle = "g / Tonelada",
                            icon = Icons.Default.Grass,
                            accentColor = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("production_calc") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Retiro SENASICA",
                            subtitle = "Carne & Leche",
                            icon = Icons.Default.Security,
                            accentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("production_calc") }
                        )
                        QuickActionButton(
                            title = "Dosis Equino/Vaca",
                            subtitle = "mg/kg peso alto",
                            icon = Icons.Default.Calculate,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("calculator") }
                        )
                        QuickActionButton(
                            title = "Receta Oficial",
                            subtitle = "COFEPRIS/SADER",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("prescription") }
                        )
                    }
                }

                ClinicalBranch.CLINIC_SUITE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Tratamientos",
                            subtitle = "Tabla Horas",
                            icon = Icons.Default.Schedule,
                            accentColor = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("treatments_sheet") }
                        )
                        QuickActionButton(
                            title = "Hospitalización",
                            subtitle = "Avisos Auditivos",
                            icon = Icons.Default.Hotel,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("hospital") }
                        )
                        QuickActionButton(
                            title = "Expedientes",
                            subtitle = "Notas de Voz IA",
                            icon = Icons.Default.FolderShared,
                            accentColor = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("patients") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Inventario & Lotes",
                            subtitle = "Caducidades",
                            icon = Icons.Default.Inventory2,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("inventory") }
                        )
                        QuickActionButton(
                            title = "Libro COFEPRIS",
                            subtitle = "Psicotrópicos",
                            icon = Icons.Default.Security,
                            accentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("controlled_logs") }
                        )
                        QuickActionButton(
                            title = "Citas WhatsApp",
                            subtitle = "Agenda",
                            icon = Icons.Default.CalendarMonth,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("calendar") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Pendientes & Bonos",
                            subtitle = "Tablón & Servicios",
                            icon = Icons.Default.AssignmentTurnedIn,
                            accentColor = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("clinic_operations") }
                        )
                        QuickActionButton(
                            title = "Ficha Médica",
                            subtitle = "Personal Accidente",
                            icon = Icons.Default.HealthAndSafety,
                            accentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("staff_emergency") }
                        )
                        QuickActionButton(
                            title = "Estética & Baños",
                            subtitle = "Grooming & Bonos",
                            icon = Icons.Default.ContentCut,
                            accentColor = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("clinic_operations") }
                        )
                    }
                }
            }
        }

        // Expiry & Batch Alerts Banner (30 and 60 days)
        if (productsExpiring30Days.isNotEmpty() || productsExpiring60Days.isNotEmpty()) {
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ALERTA DE CADUCIDADES EN FARMACIA",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (productsExpiring30Days.isNotEmpty()) {
                                Text(
                                    text = "🔴 ${productsExpiring30Days.size} lote(s) vencen en MENOS DE 30 DÍAS: " +
                                            productsExpiring30Days.joinToString { "${it.name} (${it.batchNumber})" },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (productsExpiring60Days.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🟠 ${productsExpiring60Days.size} lote(s) vencen en MENOS DE 60 DÍAS: " +
                                            productsExpiring60Days.joinToString { "${it.name} (${it.batchNumber})" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onNavigate("inventory") }) {
                            Text("Revisar Lotes en Inventario")
                        }
                    }
                }
            }
        }

        // Clinical Statistics Dashboard Cards
        item {
            Text(
                text = "Rendimiento y Métricas Clínicas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeumorphicStatCard(
                    title = "Ventas del Mes",
                    value = "$${String.format("%.0f", totalRevenue)}",
                    subtitle = "Ganancia: $${String.format("%.0f", netProfit)}",
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.primary
                )

                NeumorphicStatCard(
                    title = "Hospitalizados",
                    value = "${hospitalized.count { !it.isDone }}",
                    subtitle = "${hospitalized.size} registrados",
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Quick Clinical Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate("controlled_logs") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Libro COFEPRIS", maxLines = 1)
                }

                OutlinedButton(
                    onClick = { onNavigate("weights_vaccines") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Curvas & Vacunas", maxLines = 1)
                }
            }
        }
    }

    if (showStaffSwitchDialog) {
        StaffSwitchDialog(
            currentStaff = currentStaff,
            currentRole = currentRole,
            onDismiss = { showStaffSwitchDialog = false },
            onSelect = { name, role ->
                viewModel.setActiveStaff(name, role)
                showStaffSwitchDialog = false
            }
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = modifier.clickable(onClick = onClick),
        elevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StaffSwitchDialog(
    currentStaff: String,
    currentRole: String,
    onDismiss: () -> Unit,
    onSelect: (name: String, role: String) -> Unit
) {
    val staffList = listOf(
        Pair("MVZ. Alejandro Morales", "Director Médico / Administrador"),
        Pair("MVZ. Mariana Garza", "Médico Veterinario Titular"),
        Pair("Pasante Sofía Méndez", "Auxiliar Quirúrgico / Hospitalización"),
        Pair("Téc. Roberto Silva", "Encargado de Farmacia / Almacén")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Usuario Activo en Clínica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selecciona el perfil para registrar autorizaciones de dosis, firmas de recetas y libro de psicotrópicos:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                staffList.forEach { (name, role) ->
                    val isSelected = name == currentStaff
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(name, role) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onSelect(name, role) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
