package com.example.ui.screens.operations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.entities.*
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicOperationsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Notas & Pendientes", "Mensajería Equipo", "Estética & Baños", "Servicios & Bonos")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Scrollable Tab Row
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("ops_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> TasksAndNotesSection(viewModel = viewModel)
            1 -> TeamMessagingSection(viewModel = viewModel)
            2 -> GroomingAppointmentsSection(viewModel = viewModel)
            3 -> ServicesAndCommissionsSection(viewModel = viewModel)
        }
    }
}

// -----------------------------------------------------------------------------
// PESTAÑA 1: TABLÓN DE NOTAS Y PENDIENTES DEL DÍA
// -----------------------------------------------------------------------------
@Composable
fun TasksAndNotesSection(viewModel: MainViewModel) {
    val tasks by viewModel.clinicTasks.collectAsState()
    val activeStaff by viewModel.activeStaffName.collectAsState()
    val staffProfiles by viewModel.staffProfiles.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("TODAS") } // "TODAS", "PENDIENTES", "COMPLETADAS"

    val filteredTasks = remember(tasks, filterStatus) {
        when (filterStatus) {
            "PENDIENTES" -> tasks.filter { !it.isCompleted }
            "COMPLETADAS" -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tablón de Pendientes y Asignación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier.testTag("add_task_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Pendiente")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TODAS" to "Todos", "PENDIENTES" to "Pendientes", "COMPLETADAS" to "Completadas").forEach { (code, label) ->
                        FilterChip(
                            selected = filterStatus == code,
                            onClick = { filterStatus = code },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("¡No hay tareas pendientes en este filtro!", fontWeight = FontWeight.Bold)
                            Text("Agrega tareas o notas para coordinar al equipo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(filteredTasks) { task ->
                val priorityColor = when (task.priority) {
                    "ALTA" -> MaterialTheme.colorScheme.error
                    "MEDIA" -> Color(0xFFD97706)
                    else -> MaterialTheme.colorScheme.primary
                }

                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskCompleted(task, activeStaff) },
                            modifier = Modifier.testTag("task_check_${task.id}")
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    color = priorityColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = task.priority,
                                        color = priorityColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Asignado: ${task.assignedToStaff}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "Vence: ${task.dueDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (task.isCompleted && task.completedByStaff.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✔ Completado por ${task.completedByStaff}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteTask(task) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar tarea", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("NORMAL") }
        var assignedStaff by remember { mutableStateOf("Todos") }
        var dueDate by remember { mutableStateOf("Hoy") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Nuevo Pendiente de Clínica") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Tarea") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detalles o Instrucciones") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Text("Prioridad:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NORMAL", "MEDIA", "ALTA").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p) }
                            )
                        }
                    }

                    Text("Asignar a:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = assignedStaff == "Todos",
                                onClick = { assignedStaff = "Todos" },
                                label = { Text("Todos") }
                            )
                        }
                        items(staffProfiles) { profile ->
                            FilterChip(
                                selected = assignedStaff == profile.fullName,
                                onClick = { assignedStaff = profile.fullName },
                                label = { Text(profile.fullName.take(15)) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Fecha / Hora Límite") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addClinicTask(
                                ClinicTaskEntity(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    assignedToStaff = assignedStaff,
                                    createdByStaff = activeStaff,
                                    dueDate = dueDate
                                )
                            )
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Asignar Tarea")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// PESTAÑA 2: MENSAJERÍA ENTRE MIEMBROS DEL EQUIPO
// -----------------------------------------------------------------------------
@Composable
fun TeamMessagingSection(viewModel: MainViewModel) {
    val messages by viewModel.teamMessages.collectAsState()
    val activeStaff by viewModel.activeStaffName.collectAsState()
    val activeRole by viewModel.activeStaffRole.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("GENERAL") }

    val categories = listOf("GENERAL", "URGENTE", "AVISO", "TURNO")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Canal Interno de Mensajería de la Clínica",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Comunícate con recepción, quirófano, hospital y estética en tiempo real.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Message List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderName.equals(activeStaff, ignoreCase = true)
                val catColor = when (msg.category) {
                    "URGENTE" -> MaterialTheme.colorScheme.error
                    "AVISO" -> Color(0xFFD97706)
                    "TURNO" -> Color(0xFF0077B6)
                    else -> MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Surface(
                                    color = catColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = msg.category,
                                        color = catColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = msg.senderRole,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = msg.message, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(4.dp))
                            val timeStr = remember(msg.timestamp) {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Category Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Send message input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                label = { Text("Escribe un aviso para el equipo...") },
                modifier = Modifier.weight(1f).testTag("team_message_input")
            )

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        viewModel.sendTeamMessage(messageInput, selectedCategory)
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .testTag("send_team_message_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PESTAÑA 3: AGENDA DE ESTÉTICAS Y BAÑOS (GROOMING)
// -----------------------------------------------------------------------------
@Composable
fun GroomingAppointmentsSection(viewModel: MainViewModel) {
    val groomingList by viewModel.groomingAppointments.collectAsState()
    val staffProfiles by viewModel.staffProfiles.collectAsState()
    var showAddGroomingDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agenda de Estética Canina & Baños",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Control de citas, servicios higiénicos y comisiones para estilistas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(onClick = { showAddGroomingDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agendar")
                    }
                }
            }

            if (groomingList.isEmpty()) {
                item {
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay citas de estética programadas", fontWeight = FontWeight.Bold)
                            Text("Toca 'Agendar' para programar un baño o corte.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(groomingList) { appt ->
                val statusColor = when (appt.status) {
                    "Terminada" -> Color(0xFF16A34A)
                    "En Baño / Secado" -> Color(0xFF0077B6)
                    else -> MaterialTheme.colorScheme.primary
                }

                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${appt.patientName} (${appt.breed})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Servicio: ${appt.serviceType}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Propietario: ${appt.ownerName} (${appt.ownerPhone})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = appt.status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "📅 Horario: ${appt.dateString} a las ${appt.timeString} • Estilista: ${appt.assignedGroomerStaff}", style = MaterialTheme.typography.bodySmall)

                    if (appt.skinObservations.isNotBlank()) {
                        Text(text = "Observaciones de Piel: ${appt.skinObservations}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Precio: $${String.format("%.0f", appt.cost)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Comisión Estilista: $${String.format("%.0f", appt.groomerCommission)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (appt.status != "Terminada") {
                                OutlinedButton(
                                    onClick = { viewModel.updateGroomingStatus(appt, "En Baño / Secado") }
                                ) {
                                    Text("En Baño", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = { viewModel.updateGroomingStatus(appt, "Terminada") }
                                ) {
                                    Text("Finalizar ✔", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            IconButton(onClick = { viewModel.deleteGroomingAppointment(appt) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGroomingDialog) {
        var patientName by remember { mutableStateOf("") }
        var breed by remember { mutableStateOf("") }
        var ownerName by remember { mutableStateOf("") }
        var ownerPhone by remember { mutableStateOf("") }
        var serviceType by remember { mutableStateOf("Baño Básico & Uñas") }
        var groomer by remember { mutableStateOf("Carlos Ramírez") }
        var dateStr by remember { mutableStateOf("Hoy") }
        var timeStr by remember { mutableStateOf("12:00") }
        var costStr by remember { mutableStateOf("380.0") }
        var commissionStr by remember { mutableStateOf("150.0") }
        var skinObs by remember { mutableStateOf("Piel sana") }

        AlertDialog(
            onDismissRequest = { showAddGroomingDialog = false },
            title = { Text("Agendar Cita de Estética") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Mascota") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Propietario") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Teléfono") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        OutlinedTextField(value = serviceType, onValueChange = { serviceType = it }, label = { Text("Tipo de Servicio de Estética") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = groomer, onValueChange = { groomer = it }, label = { Text("Estilista Asignado") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Fecha") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = timeStr, onValueChange = { timeStr = it }, label = { Text("Hora") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costStr,
                                onValueChange = { costStr = it },
                                label = { Text("Costo Total ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = commissionStr,
                                onValueChange = { commissionStr = it },
                                label = { Text("Comisión Estilista ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(value = skinObs, onValueChange = { skinObs = it }, label = { Text("Observaciones de Piel / Pelo") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (patientName.isNotBlank()) {
                            val cost = costStr.toDoubleOrNull() ?: 380.0
                            val comm = commissionStr.toDoubleOrNull() ?: 150.0
                            viewModel.addGroomingAppointment(
                                GroomingAppointmentEntity(
                                    patientName = patientName,
                                    breed = breed,
                                    ownerName = ownerName,
                                    ownerPhone = ownerPhone,
                                    serviceType = serviceType,
                                    assignedGroomerStaff = groomer,
                                    dateString = dateStr,
                                    timeString = timeStr,
                                    cost = cost,
                                    groomerCommission = comm,
                                    skinObservations = skinObs,
                                    status = "Programada"
                                )
                            )
                            showAddGroomingDialog = false
                        }
                    }
                ) {
                    Text("Agendar Estética")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroomingDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// PESTAÑA 4: CATÁLOGO DE SERVICIOS Y TABLÓN DE DESEMPEÑO / COMISIONES
// -----------------------------------------------------------------------------
@Composable
fun ServicesAndCommissionsSection(viewModel: MainViewModel) {
    val services by viewModel.clinicServices.collectAsState()
    val commissions by viewModel.staffCommissions.collectAsState()
    val staffProfiles by viewModel.staffProfiles.collectAsState()
    val context = LocalContext.current

    var subTab by remember { mutableStateOf(0) } // 0: Catálogo de Servicios, 1: Tablón de Desempeño y Liquidación
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showRecordCommissionDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ClinicServiceEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                label = { Text("Catálogo de Servicios") }
            )
            FilterChip(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                label = { Text("Liquidación y Comisiones de Personal") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (subTab == 0) {
            // CATÁLOGO DE SERVICIOS
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lista de Servicios Clínicos con Costos Editables",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Define precios oficiales y bonos/comisiones automáticas para consultas, cirugías y estética.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(onClick = { showAddServiceDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo")
                        }
                    }
                }

                items(services) { srv ->
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = srv.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Categoría: ${srv.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (srv.description.isNotBlank()) {
                                    Text(
                                        text = srv.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%.0f", srv.standardPrice)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    color = Color(0xFF16A34A).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    val commText = if (srv.defaultStaffCommissionType == "PERCENTAGE") {
                                        "${srv.defaultStaffCommissionValue.toInt()}% comisión"
                                    } else {
                                        "$${srv.defaultStaffCommissionValue.toInt()} bono"
                                    }
                                    Text(
                                        text = "Bono: $commText",
                                        color = Color(0xFF16A34A),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingService = srv }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar Costos")
                            }

                            TextButton(
                                onClick = { viewModel.deleteClinicService(srv) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        } else {
            // TABLÓN DE DESEMPEÑO Y LIQUIDACIÓN
            val totalCommissions = commissions.sumOf { it.commissionEarned }
            val pendingCommissions = commissions.filter { !it.isPaidOut }.sumOf { it.commissionEarned }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = "Liquidación y Bonos de Desempeño del Equipo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Comisiones Históricas:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${String.format("%.2f", totalCommissions)}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pendiente por Liquidar:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${String.format("%.2f", pendingCommissions)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showRecordCommissionDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registrar Servicio Realizado por Personal")
                        }
                    }
                }

                item {
                    Text(
                        text = "Historial de Servicios y Bonos Asignados",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(commissions) { comm ->
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comm.staffName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${comm.serviceName} • Paciente: ${comm.patientName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (comm.notes.isNotBlank()) {
                                    Text(
                                        text = comm.notes,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+$${String.format("%.2f", comm.commissionEarned)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = "Cobrado: $${String.format("%.0f", comm.totalBilledAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusText = if (comm.isPaidOut) "✔ Liquidado / Pagado" else "⏳ Pendiente de Pago"
                            val statusColor = if (comm.isPaidOut) Color(0xFF16A34A) else MaterialTheme.colorScheme.error

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )

                            OutlinedButton(
                                onClick = { viewModel.toggleCommissionPaid(comm) }
                            ) {
                                Text(if (comm.isPaidOut) "Marcar Pendiente" else "Marcar Pagado ✔", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for adding / editing service
    if (showAddServiceDialog || editingService != null) {
        val srv = editingService
        var name by remember(srv) { mutableStateOf(srv?.name ?: "") }
        var category by remember(srv) { mutableStateOf(srv?.category ?: "CONSULTA") }
        var description by remember(srv) { mutableStateOf(srv?.description ?: "") }
        var priceStr by remember(srv) { mutableStateOf(srv?.standardPrice?.toString() ?: "450.0") }
        var commType by remember(srv) { mutableStateOf(srv?.defaultStaffCommissionType ?: "PERCENTAGE") }
        var commValStr by remember(srv) { mutableStateOf(srv?.defaultStaffCommissionValue?.toString() ?: "35.0") }

        AlertDialog(
            onDismissRequest = {
                showAddServiceDialog = false
                editingService = null
            },
            title = { Text(if (srv == null) "Nuevo Servicio Clínico" else "Editar Costos de Servicio") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Servicio") }, modifier = Modifier.fillMaxWidth())

                    Text("Categoría:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("CONSULTA", "CIRUGIA", "ESTETICA", "LABORATORIO", "PREVENTIVO", "HOSPITAL")) { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
                        }
                    }

                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Precio al Público ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = commValStr,
                            onValueChange = { commValStr = it },
                            label = { Text(if (commType == "PERCENTAGE") "Comisión (%)" else "Bono Fijo ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = commType == "PERCENTAGE",
                            onClick = { commType = "PERCENTAGE" },
                            label = { Text("Porcentaje (%)") }
                        )
                        FilterChip(
                            selected = commType == "FIXED",
                            onClick = { commType = "FIXED" },
                            label = { Text("Bono Fijo ($)") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val price = priceStr.toDoubleOrNull() ?: 450.0
                            val commVal = commValStr.toDoubleOrNull() ?: 35.0
                            val entity = srv?.copy(
                                name = name.trim(),
                                category = category,
                                description = description.trim(),
                                standardPrice = price,
                                defaultStaffCommissionType = commType,
                                defaultStaffCommissionValue = commVal
                            ) ?: ClinicServiceEntity(
                                name = name.trim(),
                                category = category,
                                description = description.trim(),
                                standardPrice = price,
                                defaultStaffCommissionType = commType,
                                defaultStaffCommissionValue = commVal,
                                isCustom = true
                            )

                            if (srv == null) viewModel.addClinicService(entity) else viewModel.updateClinicService(entity)
                            showAddServiceDialog = false
                            editingService = null
                        }
                    }
                ) {
                    Text("Guardar Servicio")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddServiceDialog = false
                    editingService = null
                }) { Text("Cancelar") }
            }
        )
    }

    // Dialog for recording manual service execution with commission
    if (showRecordCommissionDialog) {
        var staffName by remember { mutableStateOf(staffProfiles.firstOrNull()?.fullName ?: "MVZ. Alejandro Morales") }
        var selectedServiceName by remember { mutableStateOf(services.firstOrNull()?.name ?: "Consulta General") }
        var patientName by remember { mutableStateOf("") }
        var billedStr by remember { mutableStateOf("450.0") }
        var commissionStr by remember { mutableStateOf("157.5") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordCommissionDialog = false },
            title = { Text("Registrar Servicio Realizado") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Profesional que realizó el servicio:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(staffProfiles) { stf ->
                                FilterChip(
                                    selected = staffName == stf.fullName,
                                    onClick = { staffName = stf.fullName },
                                    label = { Text(stf.fullName.take(15)) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Servicio brindado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(services) { s ->
                                FilterChip(
                                    selected = selectedServiceName == s.name,
                                    onClick = {
                                        selectedServiceName = s.name
                                        billedStr = s.standardPrice.toString()
                                        val c = if (s.defaultStaffCommissionType == "PERCENTAGE") {
                                            s.standardPrice * (s.defaultStaffCommissionValue / 100.0)
                                        } else s.defaultStaffCommissionValue
                                        commissionStr = String.format("%.1f", c)
                                    },
                                    label = { Text(s.name.take(18)) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Nombre del Paciente / Mascota") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = billedStr,
                                onValueChange = { billedStr = it },
                                label = { Text("Monto Cobrado ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = commissionStr,
                                onValueChange = { commissionStr = it },
                                label = { Text("Comisión / Bono ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notas de la consulta/estética/cirugía") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val billed = billedStr.toDoubleOrNull() ?: 450.0
                        val comm = commissionStr.toDoubleOrNull() ?: 150.0
                        viewModel.recordStaffServiceCommission(
                            staffName = staffName,
                            serviceName = selectedServiceName,
                            serviceCategory = services.find { it.name == selectedServiceName }?.category ?: "CONSULTA",
                            patientName = patientName.ifBlank { "Paciente General" },
                            totalBilled = billed,
                            commissionEarned = comm,
                            notes = notes
                        )
                        showRecordCommissionDialog = false
                    }
                ) {
                    Text("Acreditar Bono")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordCommissionDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
