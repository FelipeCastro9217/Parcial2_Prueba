package com.ahorro.familiar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahorro.familiar.ui.viewmodel.MetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPagoScreen(
    viewModel: MetasViewModel,
    metaId: Int,
    onVolver: () -> Unit
) {
    val detalleMeta by viewModel.detalleMeta.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var miembroSeleccionadoIndex by remember { mutableStateOf(0) }
    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val miembros = detalleMeta?.miembros ?: emptyList()

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarError() }
    }

    // Al registrar exitosamente, volver a la pantalla anterior
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Pago") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de miembro con dropdown
            Text("Selecciona el miembro:", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                OutlinedTextField(
                    value = miembros.getOrNull(miembroSeleccionadoIndex)?.nombre ?: "Sin miembros",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Miembro") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    miembros.forEachIndexed { index, miembro ->
                        DropdownMenuItem(
                            text = { Text(miembro.nombre) },
                            onClick = {
                                miembroSeleccionadoIndex = index
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            // Campo monto
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón guardar
            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull() ?: 0.0
                    val miembroId = miembros.getOrNull(miembroSeleccionadoIndex)?.id ?: return@Button
                    viewModel.registrarPago(metaId, miembroId, montoDouble, descripcion)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💰 Guardar Pago")
            }
        }
    }
}
