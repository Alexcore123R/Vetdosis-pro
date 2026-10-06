package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.AppointmentEntity
import com.example.service.WhatsAppHelper
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun CalendarAppointmentsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appointments by viewModel.appointments.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showWhatsAppBookingHelper by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agenda y Citas Veterinarias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${appointments.size} citas programadas • Recordatorios automáticos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_appointment_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agendar")
                }
            }
        }

        // WhatsApp Business Auto-Booking Helper Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Integración con WhatsApp Business",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Permite a tus clientes consultar horarios y agendar citas automáticamente vía chat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showWhatsAppBookingHelper = true },
                        modifier = Modifier.testTag("whatsapp_business_helper_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Flujo")
                    }
                }
            }
        }

        if (appointments.isEmpty()) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay citas agendadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(appointments) { appt ->
                AppointmentItemCard(
                    appointment = appt,
                    onSendReminder = {
                        WhatsAppHelper.sendAppointmentReminder(
                            context = context,
                            ownerPhone = appt.ownerPhone,
                            patientName = appt.patientName,
                            dateString = appt.dateString,
                            timeString = appt.timeString,
                            reason = appt.reason,
                            clinicName = clinicName
                        )
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newAppt ->
                viewModel.addAppointment(newAppt)
                showAddDialog = false
            }
        )
    }

    if (showWhatsAppBookingHelper) {
        WhatsAppBookingHelperDialog(
            clinicName = clinicName,
            onDismiss = { showWhatsAppBookingHelper = false }
        )
    }
}

@Composable
fun AppointmentItemCard(
    appointment: AppointmentEntity,
    onSendReminder: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appointment.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = appointment.species,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Motivo: ${appointment.reason}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Propietario: ${appointment.ownerName} (${appointment.ownerPhone})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = appointment.dateString,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = appointment.timeString,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onSendReminder,
                modifier = Modifier.testTag("send_appointment_reminder_button")
            ) {
                Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recordatorio WhatsApp")
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Canino") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("Mañana") }
    var timeString by remember { mutableStateOf("11:00") }
    var reason by remember { mutableStateOf("Consulta de rutina") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Nombre del Paciente") })
                OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Especie") })
                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Propietario") })
                OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("WhatsApp Propietario") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dateString, onValueChange = { dateString = it }, label = { Text("Fecha") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = timeString, onValueChange = { timeString = it }, label = { Text("Hora") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo / Procedimiento") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isNotBlank()) {
                        onSave(
                            AppointmentEntity(
                                patientName = patientName,
                                species = species,
                                ownerName = ownerName,
                                ownerPhone = ownerPhone,
                                dateString = dateString,
                                timeString = timeString,
                                reason = reason
                            )
                        )
                    }
                }
            ) {
                Text("Confirmar Cita")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun WhatsAppBookingHelperDialog(
    clinicName: String,
    onDismiss: () -> Unit
) {
    val template = "¡Hola! Gracias por comunicarte a *$clinicName* 🐾.\n\n" +
            "Para agendar una cita para tu mascota, por favor selecciona el servicio:\n" +
            "1️⃣ Consulta Médica General\n" +
            "2️⃣ Vacunación o Desparasitación\n" +
            "3️⃣ Cirugía / Esterilización\n" +
            "4️⃣ Estética Canina / Baño\n\n" +
            "Nuestros horarios disponibles hoy: 10:00, 12:30, 16:00, 17:30.\n" +
            "Escribe el nombre de tu mascota y el horario de tu preferencia para apartarlo."

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Flujo de Auto-Agendado WhatsApp") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mensaje automatizado configurado para clientes:", style = MaterialTheme.typography.bodySmall)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = template,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Text("Al responder el cliente, las citas quedan integradas en tu calendario de VetDosis Pro.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Entendido") }
        }
    )
}
