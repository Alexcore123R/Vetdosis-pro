package com.example.ui.screens.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.MedicationAnalysisResult
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun MedicationScannerScreen(
    viewModel: MainViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val analyzedResult by viewModel.analyzedMedication.collectAsState()
    val isAnalyzing by viewModel.isImageAnalyzing.collectAsState()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var barcodeInput by remember { mutableStateOf("") }
    var barcodeProductResult by remember { mutableStateOf<String?>(null) }
    val products by viewModel.inventoryProducts.collectAsState()

    // Photo picker launcher (complies with Google Play Photo Picker policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    capturedBitmap = bmp
                    bmp?.let { b -> viewModel.analyzeMedicationPhoto(b) }
                }
            } catch (e: Exception) {
                // error handling
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            capturedBitmap = bmp
            viewModel.analyzeMedicationPhoto(bmp)
        }
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
                text = "Escaneo de Medicamentos y Código de Barras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Identificación de principios activos, dosis y concentraciones vía Gemini Pro Vision.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action Buttons: Camera & Gallery & Demo Sample
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Captura de Frasco, Etiqueta o Receta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_camera_button")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }

                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_gallery_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Demo synthetic sample generator for immediate testing without a physical camera
                FilledTonalButton(
                    onClick = {
                        val sampleBmp = createDemoMedicationLabelBitmap("MELOXIVET 5%", "Meloxicam 5mg/ml - Uso Veterinario")
                        capturedBitmap = sampleBmp
                        viewModel.analyzeMedicationPhoto(sampleBmp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_demo_sample_button")
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Probar Escáner con Muestra de Meloxivet 5%")
                }
            }
        }

        // Preview of captured image & progress
        item {
            capturedBitmap?.let { bmp ->
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Imagen Capturada:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Etiqueta analizada",
                            modifier = Modifier.fillMaxHeight()
                        )
                    }

                    if (isAnalyzing) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analizando con Gemini Pro Vision...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // AI Analysis Results Card
        item {
            analyzedResult?.let { res ->
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FÁRMACO RECONOCIDO CON IA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = res.drugName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Principio Activo: ${res.activeIngredient}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Concentración: ${res.concentration} • Vía: ${res.route}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Especies indicadas: ${res.indicatedSpecies}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Dosis sugerida: ${res.suggestedDoseMgKg} mg/kg",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (res.observations.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Observaciones: ${res.observations}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToCalculator,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_to_calculator_button")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transferir al Calculador de Dosis")
                    }
                }
            }
        }

        // Barcode Integration Section
        item {
            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Búsqueda por Código de Barras",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        placeholder = { Text("ej. 750100223401") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                    )

                    Button(
                        onClick = {
                            val match = products.find { it.barcode == barcodeInput.trim() }
                            barcodeProductResult = if (match != null) {
                                "Encontrado: ${match.name} • Stock: ${match.stock} unidades • Precio: $${match.unitPrice}"
                            } else {
                                "Código no registrado en inventario local."
                            }
                        }
                    ) {
                        Text("Buscar")
                    }
                }

                barcodeProductResult?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper to create a test synthetic medication label bitmap
fun createDemoMedicationLabelBitmap(title: String, subtitle: String): Bitmap {
    val width = 480
    val height = 320
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply { isAntiAlias = true }
    paint.color = AndroidColor.rgb(13, 92, 88)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    paint.color = AndroidColor.WHITE
    paint.textSize = 28f
    paint.isFakeBoldText = true
    canvas.drawText(title, 30f, 80f, paint)

    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText(subtitle, 30f, 130f, paint)
    canvas.drawText("Dosis: 0.2 mg/kg Caninos | 0.05 mg/kg Felinos", 30f, 170f, paint)
    canvas.drawText("Uso exclusivo veterinario en México", 30f, 210f, paint)

    paint.color = AndroidColor.rgb(255, 215, 0)
    canvas.drawRoundRect(RectF(30f, 240f, 250f, 290f), 8f, 8f, paint)

    paint.color = AndroidColor.BLACK
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("Reg. SAGARPA Q-0021-045", 40f, 272f, paint)

    return bitmap
}
