package com.ahorro.familiar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ahorro.familiar.data.model.Miembro
import com.ahorro.familiar.data.model.Pago
import com.ahorro.familiar.ui.viewmodel.MetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMetaScreen(
    viewModel: MetasViewModel,
    metaId: Int,
    onRegistrarPago: () -> Unit,
    onVolver: () -> Unit
) {
    val detalleMeta by viewModel.detalleMeta.collectAsState()
    val pagos by viewModel.pagos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var mostrarDialogoMiembro by remember { mutableStateOf(false) }
    var nombreMiembro by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(metaId) {
        viewModel.cargarDetalleMeta(metaId)
        viewModel.cargarPagosDeMeta(metaId)
    }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarError() }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
    }

    // Diálogo para agregar miembro
    if (mostrarDialogoMiembro) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoMiembro = false },
            title = { Text("Agregar miembro") },
            text = {
                OutlinedTextField(
                    value = nombreMiembro,
                    onValueChange = { nombreMiembro = it },
                    label = { Text("Nombre del miembro") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.agregarMiembro(metaId, nombreMiembro)
                    nombreMiembro = ""
                    mostrarDialogoMiembro = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoMiembro = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detalleMeta?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoMiembro = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Agregar miembro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (cargando && detalleMeta == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            detalleMeta?.let { meta ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Foto
                    AsyncImage(
                        model = meta.foto,
                        contentDescription = meta.nombre,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(16.dp)) {

                        // Nombre y descripción
                        Text(meta.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (meta.descripcion.isNotBlank()) {
                            Text(meta.descripcion, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resumen financiero
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Valor total: $${String.format("%,.0f", meta.valorTotal)}",
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Recaudado: $${String.format("%,.0f", meta.totalRecaudado)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { meta.porcentajeCumplido / 100f },
                                    modifier = Modifier.fillMaxWidth().height(12.dp)
                                )
                                Text("${meta.porcentajeCumplido}% completado | Falta: ${meta.porcentajeFaltante}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón registrar pago
                        Button(
                            onClick = onRegistrarPago,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💰 Registrar Pago")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Miembros y aportes
                        Text("👥 Miembros y aportes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val miembrosConAportes = meta.aportesPorMiembro.ifEmpty { meta.miembros }
                        miembrosConAportes.forEach { miembro ->
                            MiembroItem(miembro)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Historial de pagos
                        Text("📋 Historial de pagos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pagos.isEmpty()) {
                            Text("Aún no hay pagos registrados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                        } else {
                            pagos.forEach { pago -> PagoItem(pago) }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MiembroItem(miembro: Miembro) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👤", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(miembro.nombre, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge)
                Text("Aportado: $${String.format("%,.0f", miembro.totalAportado)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PagoItem(pago: Pago) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pago.nombreMiembro.ifBlank { "Miembro #${pago.miembroId}" },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(pago.descripcion, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
                Text(pago.fecha, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Text(
                "$${String.format("%,.0f", pago.monto)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
