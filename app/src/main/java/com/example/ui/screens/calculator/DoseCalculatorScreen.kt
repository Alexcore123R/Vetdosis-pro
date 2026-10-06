package com.example.ui.screens.calculator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.local.entities.DrugEntity
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseCalculatorScreen(
    viewModel: MainViewModel,
    onNavigateToPrescription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val species by viewModel.calcSpecies.collectAsState()
    val weightInput by viewModel.calcWeightInput.collectAsState()
    val drugQuery by viewModel.calcDrugQuery.collectAsState()
    val selectedDrug by viewModel.calcSelectedDrug.collectAsState()
    val customDose by viewModel.calcCustomDoseMgKg.collectAsState()
    val customConc by viewModel.calcCustomConcentration.collectAsState()
    val frequency by viewModel.calcFrequency.collectAsState()
    val route by viewModel.calcRoute.collectAsState()
    val durationDays by viewModel.calcDurationDays.collectAsState()
    val calculationResult by viewModel.doseCalculationResult.collectAsState()
    val allDrugs by viewModel.drugs.collectAsState()

    var showAddDrugDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Commercial presentation toggle: Liquid (ml) or Tablets (comprimidos)
    var isTabletPresentation by remember { mutableStateOf(false) }
    var tabletStrengthMg by remember { mutableStateOf("50.0") }

    val filteredDrugs = remember(drugQuery, allDrugs, species) {
        if (drugQuery.isBlank()) {
            allDrugs.take(8)
        } else {
            allDrugs.filter {
                it.name.contains(drugQuery, ignoreCase = true) ||
                        it.activeIngredient.contains(drugQuery, ignoreCase = true) ||
                        it.mexicanTradeNames.contains(drugQuery, ignoreCase = true)
            }
        }
    }

    // Safety checks
    val weightDouble = weightInput.toDoubleOrNull() ?: 10.0
    val enteredDoseDouble = customDose.toDoubleOrNull() ?: selectedDrug?.defaultDoseMgKg ?: 0.0

    val safetyAlert = remember(selectedDrug, enteredDoseDouble, species, weightDouble) {
        if (selectedDrug != null && enteredDoseDouble > 0) {
            SafetyDoseGuard.checkDoseSafety(
                drugName = selectedDrug!!.name,
                species = species,
                weightKg = weightDouble,
                enteredDoseMgKg = enteredDoseDouble,
                minSafeMgKg = selectedDrug!!.minDoseMgKg,
                maxSafeMgKg = selectedDrug!!.maxDoseMgKg
            )
        } else null
    }

    val contraindicationWarning = remember(selectedDrug, species) {
        if (selectedDrug != null) {
            SafetyDoseGuard.checkSpeciesContraindications(selectedDrug!!.name, species)
        } else null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Species Selector Chips Grouped by Clinical Branch
        item {
            val activeBranch by viewModel.selectedBranch.collectAsState()
            var filterBranchOnly by remember { mutableStateOf(true) }

            Text(
                text = "1. Selecciona Especie Animal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Branch selector tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(ClinicalBranch.values().filter { it != ClinicalBranch.CLINIC_SUITE }) { branch ->
                    val isSelected = filterBranchOnly && activeBranch == branch
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            filterBranchOnly = true
                            viewModel.selectBranch(branch)
                        },
                        label = { Text(branch.shortName, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = !filterBranchOnly,
                        onClick = { filterBranchOnly = false },
                        label = { Text("Todas", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val displayedSpecies = remember(activeBranch, filterBranchOnly) {
                if (filterBranchOnly) {
                    AnimalSpecies.values().filter { it.branch == activeBranch }
                } else {
                    AnimalSpecies.values().toList()
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(displayedSpecies) { itemSpecies ->
                    FilterChip(
                        selected = itemSpecies == species,
                        onClick = {
                            viewModel.calcSpecies.value = itemSpecies
                            if (weightInput.isBlank() || weightInput == "0") {
                                viewModel.calcWeightInput.value = itemSpecies.defaultWeightKg.toString()
                            }
                        },
                        label = { Text(itemSpecies.displayName) },
                        leadingIcon = {
                            val icon = when (itemSpecies) {
                                AnimalSpecies.CANINE, AnimalSpecies.FELINE -> Icons.Default.Pets
                                AnimalSpecies.EQUINE, AnimalSpecies.BOVINE, AnimalSpecies.PORCINE, AnimalSpecies.OVINE_CAPRINE -> Icons.Default.Agriculture
                                AnimalSpecies.RABBIT, AnimalSpecies.FERRET, AnimalSpecies.RODENT, AnimalSpecies.REPTILE, AnimalSpecies.BIRDS_EXOTICS -> Icons.Default.FlutterDash
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Weight Input Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. Peso del Paciente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fórmula: Dosis = Peso (kg) × Dosis (mg/kg)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = weightInput.toDoubleOrNull() ?: 10.0
                                if (current > 0.5) {
                                    viewModel.calcWeightInput.value = String.format("%.1f", current - 0.5)
                                }
                            }
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Restar peso")
                        }

                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { viewModel.calcWeightInput.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .width(105.dp)
                                .testTag("weight_input_field"),
                            trailingIcon = { Text("kg", fontWeight = FontWeight.Bold) },
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                val current = weightInput.toDoubleOrNull() ?: 10.0
                                viewModel.calcWeightInput.value = String.format("%.1f", current + 0.5)
                            }
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Sumar peso")
                        }
                    }
                }
            }
        }

        // Drug Search with Autocomplete Dropdown
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. Fármaco o Marca Comercial (México)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showAddDrugDialog = true },
                        modifier = Modifier.testTag("add_custom_drug_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Fármaco", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = drugQuery,
                        onValueChange = {
                            viewModel.calcDrugQuery.value = it
                            showDropdownMenu = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drug_search_input"),
                        placeholder = { Text("Buscar ej. Meloxicam, Baytril, Clavamox, Tramavet...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (drugQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.calcDrugQuery.value = ""
                                    viewModel.calcSelectedDrug.value = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = showDropdownMenu && filteredDrugs.isNotEmpty(),
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredDrugs.forEach { drug ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(drug.name, fontWeight = FontWeight.Bold)
                                            if (drug.isUserPersonal) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        text = "DOSIS PERSONAL",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            "Comercial MX: ${drug.mexicanTradeNames}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Rango seguro: ${drug.minDoseMgKg} - ${drug.maxDoseMgKg} mg/kg (${drug.concentrationMgMl} mg/ml)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectDrugForCalculation(drug)
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                }

                selectedDrug?.let { drug ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Fármaco seleccionado: ${drug.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (drug.isUserPersonal) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "• [Dosis Personal Clínica - Aprobada]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                "Rango terapéutico seguro: ${drug.minDoseMgKg} a ${drug.maxDoseMgKg} mg/kg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Marcas comerciales México: ${drug.mexicanTradeNames}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // SAFETY GUARDS & CONTRAINDICATION RED ALERTS
        if (contraindicationWarning != null) {
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.error,
                    borderColor = Color.Red
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "¡CONTRAINDICACIÓN CLÍNICA CRÍTICA!",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contraindicationWarning,
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        safetyAlert?.let { alert ->
            if (alert.status != DoseSafetyStatus.SAFE) {
                item {
                    val containerCol = if (alert.status == DoseSafetyStatus.DANGER_EXCESSIVE) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.errorContainer

                    val textCol = if (alert.status == DoseSafetyStatus.DANGER_EXCESSIVE) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.error

                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = containerCol,
                        borderColor = Color.Red
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = textCol, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = alert.title,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    color = textCol,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Posology Adjustments: Dose mg/kg & Concentration mg/ml & Presentation
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4. Ajustes Posológicos y Presentación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Comprimidos", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = isTabletPresentation,
                            onCheckedChange = { isTabletPresentation = it },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customDose,
                        onValueChange = { viewModel.calcCustomDoseMgKg.value = it },
                        label = { Text("Dosis (mg/kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    if (!isTabletPresentation) {
                        OutlinedTextField(
                            value = customConc,
                            onValueChange = { viewModel.calcCustomConcentration.value = it },
                            label = { Text("Conc. (mg/ml)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = tabletStrengthMg,
                            onValueChange = { tabletStrengthMg = it },
                            label = { Text("Comp. mg/tab") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frequency and Route
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var freqMenuOpen by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { freqMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(frequency.code, maxLines = 1)
                        }
                        DropdownMenu(expanded = freqMenuOpen, onDismissRequest = { freqMenuOpen = false }) {
                            AdministrationFrequency.values().forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text("${freq.code} - ${freq.label}") },
                                    onClick = {
                                        viewModel.calcFrequency.value = freq
                                        freqMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    var routeMenuOpen by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { routeMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(route.label.substringBefore(" ("), maxLines = 1)
                        }
                        DropdownMenu(expanded = routeMenuOpen, onDismissRequest = { routeMenuOpen = false }) {
                            AdministrationRoute.values().forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.label) },
                                    onClick = {
                                        viewModel.calcRoute.value = r
                                        routeMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Calculation Result Banner
        item {
            calculationResult?.let { res ->
                val tabStrength = tabletStrengthMg.toDoubleOrNull() ?: 50.0
                val totalTablets = if (tabStrength > 0) res.totalDoseMg / tabStrength else 0.0

                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RESULTADO DE DOSIFICACIÓN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            if (!isTabletPresentation) {
                                Text(
                                    text = "${SafetyDoseGuard.formatSafe(res.totalVolumeMl)} ml",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "${SafetyDoseGuard.formatSafe(totalTablets)} tableta(s)",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Equivalente a ${SafetyDoseGuard.formatSafe(res.totalDoseMg)} mg activos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = res.frequency.code,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📋 Indicación Clínica:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val formattedInstructions = if (!isTabletPresentation) res.instructions
                    else "Administrar ${SafetyDoseGuard.formatSafe(totalTablets)} tableta(s) (${SafetyDoseGuard.formatSafe(res.totalDoseMg)} mg de ${res.drugName}) ${res.frequency.label} vía ${res.route.label} por ${res.durationDays} días."

                    Text(
                        text = formattedInstructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addPrescriptionItem(formattedInstructions)
                                onNavigateToPrescription()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_to_prescription_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Añadir a Receta")
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.sendPrescriptionWhatsApp(context, null)
                            },
                            modifier = Modifier.testTag("whatsapp_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp")
                        }
                    }
                }
            } ?: run {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ingresa el peso y selecciona o escribe un fármaco para calcular la dosis en ml y mg automáticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Dialog to add custom drug with validation & safety limits
    if (showAddDrugDialog) {
        AddDrugDialogValidated(
            onDismiss = { showAddDrugDialog = false },
            onAdd = { newDrug ->
                viewModel.addNewCustomDrug(newDrug)
                viewModel.selectDrugForCalculation(newDrug)
                showAddDrugDialog = false
                Toast.makeText(context, "Dosis personalizada registrada y etiquetada", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(Modifier)

@Composable
fun AddDrugDialogValidated(
    onDismiss: () -> Unit,
    onAdd: (DrugEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var activeIngredient by remember { mutableStateOf("") }
    var targetSpecies by remember { mutableStateOf("CANINE,FELINE") }
    var minDoseStr by remember { mutableStateOf("2.0") }
    var maxDoseStr by remember { mutableStateOf("5.0") }
    var concMgMlStr by remember { mutableStateOf("50.0") }
    var unitStr by remember { mutableStateOf("mg/ml") }
    var routeStr by remember { mutableStateOf("PO / SC") }
    var contraindications by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Dosis Personal / Clínica") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = "Campos obligatorios requeridos para control de calidad médica:",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Fármaco *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = activeIngredient, onValueChange = { activeIngredient = it }, label = { Text("Principio Activo *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = targetSpecies, onValueChange = { targetSpecies = it }, label = { Text("Especies aplicables (ej. CANINE,FELINE) *") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minDoseStr,
                            onValueChange = { minDoseStr = it },
                            label = { Text("Dosis Mín mg/kg *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = maxDoseStr,
                            onValueChange = { maxDoseStr = it },
                            label = { Text("Dosis Máx mg/kg *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = concMgMlStr,
                            onValueChange = { concMgMlStr = it },
                            label = { Text("Concentración *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = unitStr,
                            onValueChange = { unitStr = it },
                            label = { Text("Unidad (mg/ml) *") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { OutlinedTextField(value = routeStr, onValueChange = { routeStr = it }, label = { Text("Vía de administración *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = contraindications, onValueChange = { contraindications = it }, label = { Text("Contraindicaciones y Advertencias") }, modifier = Modifier.fillMaxWidth()) }

                errorMessage?.let { err ->
                    item {
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minD = minDoseStr.toDoubleOrNull() ?: 0.0
                    val maxD = maxDoseStr.toDoubleOrNull() ?: 0.0
                    val conc = concMgMlStr.toDoubleOrNull() ?: 0.0

                    if (name.isBlank() || activeIngredient.isBlank() || minD <= 0 || maxD <= 0 || conc <= 0) {
                        errorMessage = "Por favor completa todos los campos obligatorios (*)."
                        return@Button
                    }
                    if (maxD < minD) {
                        errorMessage = "La dosis máxima no puede ser inferior a la mínima."
                        return@Button
                    }
                    // Reject absurd inputs (e.g. > 500 mg/kg for normal veterinary drug)
                    if (maxD > 500.0) {
                        errorMessage = "¡Dosis absurda detectada (${maxD} mg/kg)! Supera los límites veterinarios aceptables."
                        return@Button
                    }

                    onAdd(
                        DrugEntity(
                            name = name,
                            activeIngredient = activeIngredient,
                            mexicanTradeNames = name,
                            targetSpecies = targetSpecies,
                            minDoseMgKg = minD,
                            maxDoseMgKg = maxD,
                            defaultDoseMgKg = (minD + maxD) / 2.0,
                            concentrationMgMl = conc,
                            unit = unitStr,
                            route = routeStr,
                            contraindications = contraindications,
                            isCustom = true,
                            isUserPersonal = true,
                            isApprovedByAdmin = true
                        )
                    )
                }
            ) {
                Text("Validar y Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
