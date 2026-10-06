package com.example.ui.screens.consent

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.InformedConsentEntity
import com.example.data.local.entities.PatientEntity
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InformedConsentScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()

    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var procedureType by remember { mutableStateOf("CIRUGIA_ANESTESIA") }
    var ownerNameInput by remember { mutableStateOf("") }
    var ownerIdInput by remember { mutableStateOf("") }
    var diagnosisInput by remember { mutableStateOf("Ovariohisterectomía profiláctica (Esterilización)") }

    // Signature path tracking
    var points by remember { mutableStateOf(listOf<Offset>()) }
    var hasSigned by remember { mutableStateOf(false) }

    LaunchedEffect(patients) {
        if (selectedPatient == null && patients.isNotEmpty()) {
            selectedPatient = patients.first()
            ownerNameInput = selectedPatient?.ownerName.orEmpty()
        }
    }

    val consentLegalText = when (procedureType) {
        "CIRUGIA_ANESTESIA" -> "Yo, como propietario o tutor legal del paciente mencionado, autorizo expresamente al equipo médico de $clinicName, encabezado por el MVZ. $doctorName, para realizar los procedimientos quirúrgicos y la administración de sedación/anestesia requeridos. He sido plenamente informado(a) sobre los riesgos inherentes, complicaciones postquirúrgicas posibles y medidas de emergencia."
        "EUTANASIA_HUMANITARIA" -> "Yo, en pleno uso de mis facultades mentales y en calidad de responsable directo del paciente, declaro que debido al estado terminal, dolor intratable y pérdida irreversible de la calidad de vida, AUTORIZO de manera libre, consciente y voluntaria la aplicación del protocolo de EUTANASIA HUMANITARIA INDOLORA mediante sedación profunda y sobredosis anestésica controlada."
        else -> "Yo autorizo el internamiento de mi mascota en el área de Hospitalización y Cuidados Críticos de $clinicName, aceptando la administración de fármacos intravenosos, monitoreo continuo y maniobras de soporte vital."
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Consentimientos Informados Legales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Documentación legal para cirugías, anestesia y eutanasia con firma autógrafa digital.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Procedure Selector
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("CIRUGIA_ANESTESIA", "Cirugía / Anestesia"),
                    Pair("EUTANASIA_HUMANITARIA", "Eutanasia"),
                    Pair("HOSPITALIZACION_CRITICA", "Hospitalización")
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = procedureType == code,
                        onClick = { procedureType = code },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Patient & Owner Data Card
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Datos del Paciente y Propietario:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedPatient?.let { "${it.name} (${it.species}, ${it.weightKg}kg)" } ?: "Seleccionar...")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.species}) - Prop: ${p.ownerName}") },
                                onClick = {
                                    selectedPatient = p
                                    ownerNameInput = p.ownerName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = { ownerNameInput = it },
                        label = { Text("Nombre del Propietario") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ownerIdInput,
                        onValueChange = { ownerIdInput = it },
                        label = { Text("INE / Identificación") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = diagnosisInput,
                    onValueChange = { diagnosisInput = it },
                    label = { Text("Procedimiento / Diagnóstico") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Terms Text Card
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Términos del Consentimiento:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = consentLegalText,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }

        // Signature Pad (Canvas on-screen signature)
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Firma del Propietario o Tutor Legal:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(
                        onClick = {
                            points = emptyList()
                            hasSigned = false
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar Firma")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    points = points + offset
                                    hasSigned = true
                                },
                                onDrag = { change, _ ->
                                    points = points + change.position
                                    hasSigned = true
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (points.size > 1) {
                            val path = Path()
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val current = points[i]
                                val prev = points[i - 1]
                                // Reset path if jump detected
                                if ((current - prev).getDistance() < 80f) {
                                    path.lineTo(current.x, current.y)
                                } else {
                                    path.moveTo(current.x, current.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    if (!hasSigned) {
                        Text(
                            text = "Firme aquí con su dedo o lápiz táctil",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (!hasSigned) {
                            Toast.makeText(context, "Por favor firme en el recuadro antes de guardar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        selectedPatient?.let { p ->
                            viewModel.saveConsent(
                                InformedConsentEntity(
                                    patientId = p.id,
                                    patientName = p.name,
                                    species = p.species,
                                    ownerName = ownerNameInput.ifBlank { p.ownerName },
                                    ownerIdCard = ownerIdInput,
                                    procedureType = procedureType,
                                    diagnosisDescription = diagnosisInput,
                                    doctorName = doctorName
                                )
                            )
                            Toast.makeText(context, "Consentimiento informado firmado y guardado en expediente", Toast.LENGTH_LONG).show()
                            points = emptyList()
                            hasSigned = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_consent_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aprobar y Registrar Consentimiento")
                }
            }
        }
    }
}
