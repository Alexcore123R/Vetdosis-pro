package com.example.ui.screens.staff

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entities.StaffEmergencyProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffEmergencyProfilesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val staffList by viewModel.staffProfiles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<StaffEmergencyProfileEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_staff_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Miembro de Equipo")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ficha Médica de Emergencia del Personal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "En clínicas veterinarias, el personal está expuesto a mordeduras, rasguños con riesgo de zoonosis, pinchazos accidentales de agujas con anestésicos y reacciones alérgicas. Estos datos permiten actuar de inmediato.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(staffList) { staff ->
                StaffProfileCard(
                    staff = staff,
                    onEdit = { editingProfile = staff },
                    onDelete = { viewModel.deleteStaffProfile(staff) },
                    onCallContact = { phone ->
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir el marcador telefónico", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        StaffEditDialog(
            staff = null,
            onDismiss = { showAddDialog = false },
            onSave = { newProfile ->
                viewModel.addStaffProfile(newProfile)
                showAddDialog = false
            }
        )
    }

    editingProfile?.let { staffToEdit ->
        StaffEditDialog(
            staff = staffToEdit,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                viewModel.updateStaffProfile(updated)
                editingProfile = null
            }
        )
    }
}

@Composable
fun StaffProfileCard(
    staff: StaffEmergencyProfileEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCallContact: (String) -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = staff.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${staff.role} • ${staff.age} años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Blood Type Badge
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🩸 ${staff.bloodType}",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Allergies Alert Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Alergias: ${staff.allergies}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (staff.preexistingConditions.isNotBlank() && staff.preexistingConditions != "Ninguna") {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Padecimientos: ${staff.preexistingConditions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "📞 Teléfono personal: ${staff.phone}", style = MaterialTheme.typography.bodySmall)
        Text(text = "🏠 Domicilio: ${staff.address}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Emergency Contact Box
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚨 Contacto de Emergencia (${staff.emergencyContactRelationship}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${staff.emergencyContactName} • ${staff.emergencyContactPhone}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { onCallContact(staff.emergencyContactPhone) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Llamar emergencia",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (staff.medicalInsuranceOrNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Seguro / Notas: ${staff.medicalInsuranceOrNotes}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Eliminar")
            }
        }
    }
}

@Composable
fun StaffEditDialog(
    staff: StaffEmergencyProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (StaffEmergencyProfileEntity) -> Unit
) {
    var fullName by remember { mutableStateOf(staff?.fullName ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "Médico Veterinario") }
    var ageStr by remember { mutableStateOf(staff?.age?.toString() ?: "30") }
    var phone by remember { mutableStateOf(staff?.phone ?: "") }
    var address by remember { mutableStateOf(staff?.address ?: "") }
    var bloodType by remember { mutableStateOf(staff?.bloodType ?: "O+") }
    var allergies by remember { mutableStateOf(staff?.allergies ?: "Ninguna") }
    var conditions by remember { mutableStateOf(staff?.preexistingConditions ?: "Ninguna") }
    var emergencyContactName by remember { mutableStateOf(staff?.emergencyContactName ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(staff?.emergencyContactPhone ?: "") }
    var emergencyRelationship by remember { mutableStateOf(staff?.emergencyContactRelationship ?: "Familiar") }
    var insurance by remember { mutableStateOf(staff?.medicalInsuranceOrNotes ?: "") }

    val bloodTypes = listOf("O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (staff == null) "Nuevo Perfil Médico de Personal" else "Editar Perfil de Personal") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Puesto / Especialidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("Edad") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }

                item {
                    Text("Tipo de Sangre:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        bloodTypes.take(4).forEach { bType ->
                            FilterChip(
                                selected = bloodType == bType,
                                onClick = { bloodType = bType },
                                label = { Text(bType) }
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        bloodTypes.takeLast(4).forEach { bType ->
                            FilterChip(
                                selected = bloodType == bType,
                                onClick = { bloodType = bType },
                                label = { Text(bType) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Alergias (Fármacos, Látex, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = conditions,
                        onValueChange = { conditions = it },
                        label = { Text("Padecimientos Crónicos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Domicilio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Contacto para Emergencias:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = { Text("Nombre del Contacto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = { Text("Teléfono Emergencia") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = emergencyRelationship,
                            onValueChange = { emergencyRelationship = it },
                            label = { Text("Parentesco") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = insurance,
                        onValueChange = { insurance = it },
                        label = { Text("Seguro Médico / Vacuna Antirrábica / Notas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        val age = ageStr.toIntOrNull() ?: 30
                        val entity = staff?.copy(
                            fullName = fullName,
                            role = role,
                            age = age,
                            phone = phone,
                            address = address,
                            bloodType = bloodType,
                            allergies = allergies,
                            preexistingConditions = conditions,
                            emergencyContactName = emergencyContactName,
                            emergencyContactPhone = emergencyContactPhone,
                            emergencyContactRelationship = emergencyRelationship,
                            medicalInsuranceOrNotes = insurance
                        ) ?: StaffEmergencyProfileEntity(
                            fullName = fullName,
                            role = role,
                            age = age,
                            phone = phone,
                            address = address,
                            bloodType = bloodType,
                            allergies = allergies,
                            preexistingConditions = conditions,
                            emergencyContactName = emergencyContactName,
                            emergencyContactPhone = emergencyContactPhone,
                            emergencyContactRelationship = emergencyRelationship,
                            medicalInsuranceOrNotes = insurance
                        )
                        onSave(entity)
                    }
                }
            ) {
                Text("Guardar Ficha Médica")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
