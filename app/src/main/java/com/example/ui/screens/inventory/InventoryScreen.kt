package com.example.ui.screens.inventory

import android.content.Context
import androidx.compose.foundation.background
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
import com.example.data.local.entities.ProductEntity
import com.example.service.WhatsAppHelper
import com.example.ui.MainViewModel
import com.example.ui.components.NeumorphicCard

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.inventoryProducts.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var showCashClosureDialog by remember { mutableStateOf(false) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("TODOS") }

    val filterOptions = listOf("TODOS", "ALERTAS VENCIMIENTO (<60d)", "STOCK BAJO", "CONTROLADOS COFEPRIS")

    val filteredProducts = remember(searchQuery, products, filterCategory) {
        var list = if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true) ||
                    it.batchNumber.contains(searchQuery, ignoreCase = true)
        }

        when (filterCategory) {
            "ALERTAS VENCIMIENTO (<60d)" -> list.filter { it.daysUntilExpiry in 0..60 }
            "STOCK BAJO" -> list.filter { it.stock <= it.minStockAlert }
            "CONTROLADOS COFEPRIS" -> list.filter { it.isControlled || it.category.contains("Controlado", ignoreCase = true) }
            else -> list
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inventario, Lotes y Caducidades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${products.size} productos • Control estricto de lotes a 30/60 días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = { showAddProductDialog = true },
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = "Agregar Producto", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { showSupplierDialog = true },
                        modifier = Modifier.testTag("suppliers_button")
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Distribuidores", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // Stats & POS Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showSaleDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("register_sale_button")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cobro POS")
                }

                OutlinedButton(
                    onClick = { showCashClosureDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cash_closure_button")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cierre de Caja")
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filterOptions) { opt ->
                    FilterChip(
                        selected = opt == filterCategory,
                        onClick = { filterCategory = opt },
                        label = { Text(opt, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, lote, código de barras...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        // Products List
        items(filteredProducts) { prod ->
            ProductItemCardEnhanced(
                product = prod,
                onQuickSale = {
                    viewModel.registerSale(
                        itemsSummary = "${prod.name} (Lote: ${prod.batchNumber}) x 1",
                        totalAmount = prod.unitPrice,
                        totalCost = prod.unitCost,
                        paymentMethod = "Efectivo",
                        clientName = "Venta mostrador"
                    )
                }
            )
        }
    }

    if (showAddProductDialog) {
        AddProductDialogEnhanced(
            onDismiss = { showAddProductDialog = false },
            onSave = { newProd ->
                viewModel.addProduct(newProd)
                showAddProductDialog = false
            }
        )
    }

    if (showSaleDialog) {
        RegisterSaleDialog(
            products = products,
            onDismiss = { showSaleDialog = false },
            onConfirm = { summary, total, cost, method, client ->
                viewModel.registerSale(summary, total, cost, method, client)
                showSaleDialog = false
            }
        )
    }

    if (showCashClosureDialog) {
        CashClosureDialog(
            sales = sales,
            onDismiss = { showCashClosureDialog = false }
        )
    }

    if (showSupplierDialog) {
        SupplierOrderDialog(
            viewModel = viewModel,
            lowStockProducts = lowStock,
            onDismiss = { showSupplierDialog = false }
        )
    }
}

@Composable
fun ProductItemCardEnhanced(
    product: ProductEntity,
    onQuickSale: () -> Unit
) {
    val isLowStock = product.stock <= product.minStockAlert

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (product.daysUntilExpiry in 1..30) Color(0xFFEF4444)
        else if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.outlineVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "STOCK BAJO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Batch & Expiry Badges
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Lote: ${product.batchNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // 30/60 days alert badge
                    when {
                        product.daysUntilExpiry <= 0 -> {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF991B1B)) {
                                Text("LOTE VENCIDO", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        product.daysUntilExpiry <= 30 -> {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEF4444)) {
                                Text("VENCE EN <30 DÍAS (${product.daysUntilExpiry}d)", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        product.daysUntilExpiry <= 60 -> {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B)) {
                                Text("VENCE EN <60 DÍAS (${product.daysUntilExpiry}d)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        else -> {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                Text("Cad: ${product.expiryDate}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF047857), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    if (product.isControlled) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF7C3AED).copy(alpha = 0.15f)) {
                            Text("COFEPRIS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Text(
                    text = "${product.category} • Costo: $${product.unitCost} • Venta: $${product.unitPrice} • Stock: ${product.stock} pzas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${product.unitPrice}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onQuickSale, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Venta Rápida", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun AddProductDialogEnhanced(
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fármaco") }
    var batchNumber by remember { mutableStateOf("LOT-2026-B") }
    var expiryDate by remember { mutableStateOf("2027-11-30") }
    var daysUntilExpiryStr by remember { mutableStateOf("420") }
    var stockStr by remember { mutableStateOf("12") }
    var costStr by remember { mutableStateOf("120.0") }
    var priceStr by remember { mutableStateOf("220.0") }
    var isControlled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Lote / Producto en Farmacia") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Producto") }) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Número de Lote *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Caducidad (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = daysUntilExpiryStr,
                            onValueChange = { daysUntilExpiryStr = it },
                            label = { Text("Días restantes vigencia") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Stock inicial") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costStr,
                            onValueChange = { costStr = it },
                            label = { Text("Costo") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Precio Venta") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isControlled, onCheckedChange = { isControlled = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sustancia Controlada (COFEPRIS/SENASICA Grupo I/II/III)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ProductEntity(
                                name = name,
                                category = if (isControlled) "Psicotrópico Controlado" else category,
                                batchNumber = batchNumber,
                                expiryDate = expiryDate,
                                daysUntilExpiry = daysUntilExpiryStr.toIntOrNull() ?: 365,
                                stock = stockStr.toIntOrNull() ?: 1,
                                unitCost = costStr.toDoubleOrNull() ?: 0.0,
                                unitPrice = priceStr.toDoubleOrNull() ?: 0.0,
                                isControlled = isControlled,
                                controlledGroup = if (isControlled) "GRUPO_III_PSICOTROPICO" else "NORMAL"
                            )
                        )
                    }
                }
            ) {
                Text("Guardar Lote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun RegisterSaleDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (summary: String, total: Double, cost: Double, method: String, client: String) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityStr by remember { mutableStateOf("1") }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var clientName by remember { mutableStateOf("Mostrador") }

    val qty = quantityStr.toIntOrNull() ?: 1
    val total = (selectedProduct?.unitPrice ?: 0.0) * qty
    val cost = (selectedProduct?.unitCost ?: 0.0) * qty

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cobro y Registro de Venta (POS)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Producto:", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedProduct?.let { "${it.name} (Lote: ${it.batchNumber})" } ?: "Seleccionar...")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} ($${p.unitPrice})") },
                                onClick = {
                                    selectedProduct = p
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Método de Pago:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Efectivo", "Tarjeta", "Transferencia").forEach { m ->
                        FilterChip(
                            selected = m == paymentMethod,
                            onClick = { paymentMethod = m },
                            label = { Text(m) }
                        )
                    }
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Cliente / Paciente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total a cobrar: $${String.format("%.2f", total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Ganancia estimada: $${String.format("%.2f", total - cost)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProduct?.let { p ->
                        onConfirm("${p.name} (Lote ${p.batchNumber}) x $qty", total, cost, paymentMethod, clientName)
                    }
                }
            ) {
                Text("Confirmar Cobro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CashClosureDialog(
    sales: List<com.example.data.local.entities.SaleEntity>,
    onDismiss: () -> Unit
) {
    val totalRevenue = sales.sumOf { it.totalAmount }
    val totalCost = sales.sumOf { it.totalCost }
    val netProfit = totalRevenue - totalCost

    val cashTotal = sales.filter { it.paymentMethod.equals("Efectivo", ignoreCase = true) }.sumOf { it.totalAmount }
    val cardTotal = sales.filter { it.paymentMethod.equals("Tarjeta", ignoreCase = true) }.sumOf { it.totalAmount }
    val transTotal = sales.filter { it.paymentMethod.equals("Transferencia", ignoreCase = true) }.sumOf { it.totalAmount }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cierre de Caja y Ganancias Netas") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text("Ventas Totales Brutas", style = MaterialTheme.typography.labelSmall)
                        Text("$${String.format("%.2f", totalRevenue)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ganancia Neta: $${String.format("%.2f", netProfit)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Costo de insumos: $${String.format("%.2f", totalCost)}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    Text("Desglose por Método de Pago:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("• Efectivo en caja: $${String.format("%.2f", cashTotal)}")
                    Text("• Tarjeta TPV: $${String.format("%.2f", cardTotal)}")
                    Text("• Transferencias: $${String.format("%.2f", transTotal)}")
                    Text("• Transacciones registradas: ${sales.size}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Aceptar") }
        }
    )
}

@Composable
fun SupplierOrderDialog(
    viewModel: MainViewModel,
    lowStockProducts: List<ProductEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var supplierName by remember { mutableStateOf("Distribuidora Veterinaria Azteca") }
    var supplierPhone by remember { mutableStateOf("+52 55 1234 5678") }
    val draft by viewModel.supplierOrderDraft.collectAsState()
    val isDrafting by viewModel.isDraftingOrder.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pedidos a Distribuidores con IA") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Genera y aprueba el mensaje de pedido antes de enviar por WhatsApp:", style = MaterialTheme.typography.bodySmall)
                }
                item {
                    OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = supplierPhone, onValueChange = { supplierPhone = it }, label = { Text("WhatsApp Proveedor") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    if (isDrafting) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        Button(
                            onClick = {
                                val list = if (lowStockProducts.isNotEmpty()) lowStockProducts.map { "${it.name} (Lote: ${it.batchNumber})" }
                                else listOf("Meloxivet 5% 50ml x 10", "Baytril 5% 100ml x 5", "Solución Hartmann 1000ml x 20")
                                viewModel.draftSupplierOrder(supplierName, supplierPhone, list)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Redactar Pedido con IA")
                        }
                    }
                }
                if (draft.isNotBlank()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = draft,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                WhatsAppHelper.sendSupplierOrder(context, supplierPhone, draft)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aprobar y Enviar por WhatsApp")
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
