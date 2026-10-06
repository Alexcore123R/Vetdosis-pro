package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.entities.PatientEntity
import com.example.data.model.ClinicalBranch
import com.example.ui.components.NeumorphicCard
import com.example.ui.screens.calendar.CalendarAppointmentsScreen
import com.example.ui.screens.calculator.AdvancedCalculatorsScreen
import com.example.ui.screens.calculator.DoseCalculatorScreen
import com.example.ui.screens.chat.AiVetChatScreen
import com.example.ui.screens.consent.InformedConsentScreen
import com.example.ui.screens.controlled.ControlledSubstancesScreen
import com.example.ui.screens.exotics.ExoticsCalculatorsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.hospital.HospitalizationScreen
import com.example.ui.screens.hospital.TreatmentSheetScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.operations.ClinicOperationsScreen
import com.example.ui.screens.patients.PatientDetailScreen
import com.example.ui.screens.patients.PatientsScreen
import com.example.ui.screens.patients.WeightAndVaccinesScreen
import com.example.ui.screens.prescription.PrescriptionScreen
import com.example.ui.screens.production.ProductionCalculatorsScreen
import com.example.ui.screens.protocols.AnesthesiaProtocolsScreen
import com.example.ui.screens.scanner.MedicationScannerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.staff.StaffEmergencyProfilesScreen
import com.example.ui.screens.statistics.StatisticsScreen
import com.example.ui.screens.vitals.PhysiologicalConstantsScreen
import kotlinx.coroutines.launch

data class DrawerMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val branch: ClinicalBranch? = null,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeBranch by viewModel.selectedBranch.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var viewingPatient by remember { mutableStateOf<PatientEntity?>(null) }

    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val hospitalizedList by viewModel.hospitalizedPatients.collectAsState()
    val activeHospitalCount = hospitalizedList.count { !it.isDone }

    // Audio Dictation Dialog State
    var showAudioDictationDialog by remember { mutableStateOf(false) }
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val isTranscribingAudio by viewModel.isTranscribingAudio.collectAsState()
    val transcribedText by viewModel.lastTranscribedText.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.startVoiceRecording(context)
            if (started) {
                showAudioDictationDialog = true
            } else {
                Toast.makeText(context, "No se pudo iniciar el grabador de audio.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Se requiere permiso de micrófono para transcribir audio.", Toast.LENGTH_LONG).show()
        }
    }

    // Filterable menu item by selected branch
    var showAllItemsInDrawer by remember { mutableStateOf(false) }

    val allMenuItems = listOf(
        // General / Home
        DrawerMenuItem("home", "Inicio & Panel Rápido", Icons.Default.Home, null),

        // Pequeñas Especies
        DrawerMenuItem("calculator", "Calculador Canino/Felino", Icons.Default.Calculate, ClinicalBranch.SMALL_ANIMALS),
        DrawerMenuItem("protocols", "Protocolos de Sedación", Icons.Default.MedicalServices, ClinicalBranch.SMALL_ANIMALS),
        DrawerMenuItem("advanced_calculators", "Fluidoterapia & CRI", Icons.Default.WaterDrop, ClinicalBranch.SMALL_ANIMALS),
        DrawerMenuItem("weights_vaccines", "Curva de Peso & Vacunas", Icons.Default.Timeline, ClinicalBranch.SMALL_ANIMALS),
        DrawerMenuItem("vitals", "Constantes Fisiológicas", Icons.Default.MonitorHeart, null),

        // Fauna Silvestre & Exóticos
        DrawerMenuItem("exotics_calc", "Metabolismo & Toxicidad Exóticos", Icons.Default.Pets, ClinicalBranch.EXOTICS),

        // Grandes Especies & Producción
        DrawerMenuItem("production_calc", "Lote, Agua, Alimento & Retiro", Icons.Default.Agriculture, ClinicalBranch.LARGE_PRODUCTION),

        // Gestión Clínica & Hospital
        DrawerMenuItem("clinic_operations", "Pendientes, Estética & Bonos", Icons.Default.AssignmentTurnedIn, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("staff_emergency", "Personal & Ficha Médica", Icons.Default.HealthAndSafety, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("hospital", "Hospitalización & Avisos", Icons.Default.Hotel, ClinicalBranch.CLINIC_SUITE, if (activeHospitalCount > 0) "$activeHospitalCount" else null),
        DrawerMenuItem("treatments_sheet", "Hoja de Tratamientos (Horas)", Icons.Default.Schedule, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("patients", "Expedientes y Pacientes", Icons.Default.FolderShared, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("consents", "Consentimientos & Firma", Icons.Default.Draw, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("prescription", "Receta Membretada PDF", Icons.Default.ReceiptLong, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("controlled_logs", "Libro COFEPRIS / SENASICA", Icons.Default.Security, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("inventory", "Inventario & Caducidades", Icons.Default.Inventory2, ClinicalBranch.CLINIC_SUITE, if (lowStockList.isNotEmpty()) "${lowStockList.size}!" else null),
        DrawerMenuItem("scanner", "Escáner de Fármacos IA", Icons.Default.DocumentScanner, null),
        DrawerMenuItem("chat", "Asistente IA Clínico & Dictado", Icons.Default.AutoAwesome, null),
        DrawerMenuItem("calendar", "Agenda y Citas WhatsApp", Icons.Default.CalendarMonth, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("statistics", "Panel de Estadísticas", Icons.Default.BarChart, ClinicalBranch.CLINIC_SUITE),
        DrawerMenuItem("settings", "Ajustes y Temas", Icons.Default.Settings, null)
    )

    val displayedMenuItems = remember(activeBranch, showAllItemsInDrawer) {
        if (showAllItemsInDrawer) {
            allMenuItems
        } else {
            allMenuItems.filter { item ->
                item.branch == null || item.branch == activeBranch || item.id == "home" || item.id == "settings" || item.id == "chat"
            }
        }
    }

    // BackHandler for secondary screens
    if (viewingPatient != null) {
        BackHandler {
            viewingPatient = null
        }
    } else if (currentScreen != "home") {
        BackHandler {
            viewModel.navigateTo("home")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer Header
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "VetDosis Pro",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "Farmacología & Clínica Especializada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Branch Switcher Chips in Drawer Header
                        Text(
                            text = "RAMA CLÍNICA ACTIVA:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ClinicalBranch.values()) { branch ->
                                val isSelected = activeBranch == branch && !showAllItemsInDrawer
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        showAllItemsInDrawer = false
                                        viewModel.selectBranch(branch)
                                    },
                                    label = { Text(branch.shortName, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("drawer_branch_${branch.id}")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = showAllItemsInDrawer,
                                    onClick = { showAllItemsInDrawer = !showAllItemsInDrawer },
                                    label = { Text("Todas", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Menu items
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(displayedMenuItems) { item ->
                        val isSelected = currentScreen == item.id && viewingPatient == null
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            badge = {
                                item.badge?.let { b ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (b.contains("!")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = b,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                viewingPatient = null
                                viewModel.navigateTo(item.id)
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 1.dp)
                                .testTag("drawer_item_${item.id}"),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Drawer Footer
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VetDosis Pro v2.5 • COFEPRIS / SENASICA Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val titleText = when {
                            viewingPatient != null -> "Expediente: ${viewingPatient?.name}"
                            currentScreen == "home" -> "VetDosis Pro"
                            currentScreen == "calculator" -> "Calculador de Dosis"
                            currentScreen == "advanced_calculators" -> "Fluidoterapia & CRI"
                            currentScreen == "protocols" -> "Protocolos de Sedación"
                            currentScreen == "vitals" -> "Constantes Fisiológicas"
                            currentScreen == "production_calc" -> "Grandes Especies & Producción"
                            currentScreen == "exotics_calc" -> "Fauna Silvestre & Exóticos"
                            currentScreen == "clinic_operations" -> "Pendientes, Estética & Bonos"
                            currentScreen == "staff_emergency" -> "Ficha Médica del Personal"
                            currentScreen == "hospital" -> "Hospitalización"
                            currentScreen == "treatments_sheet" -> "Hoja de Tratamientos"
                            currentScreen == "patients" -> "Expedientes"
                            currentScreen == "weights_vaccines" -> "Curvas & Vacunas"
                            currentScreen == "consents" -> "Consentimientos Legales"
                            currentScreen == "prescription" -> "Receta Membretada"
                            currentScreen == "controlled_logs" -> "Libro COFEPRIS"
                            currentScreen == "inventory" -> "Inventario & Lotes"
                            currentScreen == "scanner" -> "Escáner IA"
                            currentScreen == "chat" -> "Asistente IA Vet"
                            currentScreen == "calendar" -> "Agenda de Citas"
                            currentScreen == "statistics" -> "Estadísticas"
                            currentScreen == "settings" -> "Ajustes de Clínica"
                            else -> "VetDosis Pro"
                        }
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        if (viewingPatient != null) {
                            IconButton(onClick = { viewingPatient = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                            }
                        } else {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú lateral deslizable")
                            }
                        }
                    },
                    actions = {
                        // Quick Voice Dictation / Audio Transcription Button (gemini-3.5-transcribe)
                        IconButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    val started = viewModel.startVoiceRecording(context)
                                    if (started) {
                                        showAudioDictationDialog = true
                                    } else {
                                        showAudioDictationDialog = true
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.testTag("topbar_mic_button")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Dictado de Audio por Voz",
                                tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                viewingPatient = null
                                viewModel.navigateTo("chat")
                            },
                            modifier = Modifier.testTag("topbar_chat_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Asistente IA", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (viewingPatient != null) {
                    PatientDetailScreen(
                        viewModel = viewModel,
                        patient = viewingPatient!!,
                        onBack = { viewingPatient = null },
                        onCalculateDose = {
                            viewingPatient = null
                            viewModel.navigateTo("calculator")
                        },
                        onPrescribe = {
                            viewingPatient = null
                            viewModel.navigateTo("prescription")
                        }
                    )
                } else {
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                        "calculator" -> DoseCalculatorScreen(
                            viewModel = viewModel,
                            onNavigateToPrescription = { viewModel.navigateTo("prescription") }
                        )
                        "advanced_calculators" -> AdvancedCalculatorsScreen(viewModel = viewModel)
                        "production_calc" -> ProductionCalculatorsScreen(viewModel = viewModel)
                        "exotics_calc" -> ExoticsCalculatorsScreen(viewModel = viewModel)
                        "clinic_operations" -> ClinicOperationsScreen(viewModel = viewModel)
                        "staff_emergency" -> StaffEmergencyProfilesScreen(viewModel = viewModel)
                        "protocols" -> AnesthesiaProtocolsScreen()
                        "vitals" -> PhysiologicalConstantsScreen()
                        "hospital" -> HospitalizationScreen(viewModel = viewModel)
                        "treatments_sheet" -> TreatmentSheetScreen(viewModel = viewModel)
                        "patients" -> PatientsScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { viewingPatient = it }
                        )
                        "weights_vaccines" -> WeightAndVaccinesScreen(viewModel = viewModel)
                        "consents" -> InformedConsentScreen(viewModel = viewModel)
                        "prescription" -> PrescriptionScreen(viewModel = viewModel)
                        "controlled_logs" -> ControlledSubstancesScreen(viewModel = viewModel)
                        "inventory" -> InventoryScreen(viewModel = viewModel)
                        "scanner" -> MedicationScannerScreen(
                            viewModel = viewModel,
                            onNavigateToCalculator = { viewModel.navigateTo("calculator") }
                        )
                        "chat" -> AiVetChatScreen(viewModel = viewModel)
                        "calendar" -> CalendarAppointmentsScreen(viewModel = viewModel)
                        "statistics" -> StatisticsScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Audio Dictation & Transcription Dialog (gemini-3.5-transcribe)
    if (showAudioDictationDialog) {
        AlertDialog(
            onDismissRequest = {
                if (isRecordingAudio) {
                    viewModel.stopVoiceRecordingAndTranscribe(context)
                }
                showAudioDictationDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transcripción por Voz IA (gemini-3.5-transcribe)")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecordingAudio) {
                        Text(
                            text = "🔴 Grabando audio con el micrófono...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Habla claramente indicando especie, signos clínicos, peso o indicaciones farmacológicas. Pulsa 'Detener y Transcribir' al terminar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (isTranscribingAudio) {
                        Text(
                            text = "⏳ Transcribiendo audio con el modelo gemini-3.5-transcribe...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    } else {
                        Text(
                            text = "Texto Transcrito:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = transcribedText.ifBlank { "Presiona 'Iniciar Grabación' para hablar." },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (isRecordingAudio) {
                    Button(
                        onClick = {
                            viewModel.stopVoiceRecordingAndTranscribe(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detener y Transcribir")
                    }
                } else if (!isTranscribingAudio) {
                    Button(
                        onClick = {
                            if (transcribedText.isNotBlank()) {
                                Toast.makeText(context, "Texto copiado al portapapeles y listo para notas.", Toast.LENGTH_SHORT).show()
                            }
                            showAudioDictationDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (isRecordingAudio) {
                            viewModel.stopVoiceRecordingAndTranscribe(context)
                        }
                        showAudioDictationDialog = false
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
