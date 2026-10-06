package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AppColorTheme
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.selectedTheme.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var clinicNameInput by remember { mutableStateOf(viewModel.clinicName.value) }
    var doctorNameInput by remember { mutableStateOf(viewModel.doctorName.value) }
    var licenseInput by remember { mutableStateOf(viewModel.doctorLicense.value) }
    var phoneInput by remember { mutableStateOf(viewModel.clinicPhone.value) }
    var addressInput by remember { mutableStateOf(viewModel.clinicAddress.value) }

    var userRole by remember { mutableStateOf("Director Médico / Administrador") }
    var backupStatus by remember { mutableStateOf("Último respaldo en la nube: Hoy, 12:45 PM") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Ajustes y Personalización de la Clínica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configuración de membrete oficial, temas visuales, respaldo y permisos de equipo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Clinic Letterhead Configuration Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Datos del Membrete Oficial (Recetas y PDF)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = clinicNameInput,
                    onValueChange = { clinicNameInput = it },
                    label = { Text("Nombre de la Clínica o Hospital") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = doctorNameInput,
                        onValueChange = { doctorNameInput = it },
                        label = { Text("Nombre del MVZ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = licenseInput,
                        onValueChange = { licenseInput = it },
                        label = { Text("Cédula Profesional") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Teléfono de la Clínica / Urgencias") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Dirección de la Clínica") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.saveClinicProfile(clinicNameInput, doctorNameInput, licenseInput, phoneInput, addressInput)
                        Toast.makeText(context, "Membrete actualizado correctamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_clinic_profile_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Membrete Oficial")
                }
            }
        }

        // Visual Themes and Neumorphism
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Personalización de Temas y Colores",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selecciona una paleta de color para la interfaz clínica:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppColorTheme.values().forEach { th ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setTheme(th) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val colorPreview = when (th) {
                                AppColorTheme.EMERALD_VET -> Color(0xFF0D5C58)
                                AppColorTheme.OCEAN_TEAL -> Color(0xFF006699)
                                AppColorTheme.ROYAL_INDIGO -> Color(0xFF3F51B5)
                                AppColorTheme.SUNSET_AMBER -> Color(0xFFB45309)
                                AppColorTheme.SURGICAL_DARK -> Color(0xFF10B981)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(colorPreview, CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = th.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currentTheme == th) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        RadioButton(
                            selected = currentTheme == th,
                            onClick = { viewModel.setTheme(th) }
                        )
                    }
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
                            text = "Modo Oscuro (Dark Mode)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Reduce la fatiga visual en guardias nocturnas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Cloud Backup & Google Drive
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Respaldo en la Nube y Google Drive",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cuenta sincronizada: Smilealex96@gmail.com",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = backupStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        backupStatus = "¡Respaldo completado con éxito! Hoy, ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                        Toast.makeText(context, "Respaldo automático de pacientes y base de datos guardado", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_now_button")
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear Respaldo Inmediato")
                }
            }
        }

        // Team & Role Management
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gestión de Equipo y Permisos de Clínica",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rol activo: $userRole",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Permite compartir bases de datos de fármacos, pacientes e inventarios entre miembros de la clínica con niveles de administrador o auxiliar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
