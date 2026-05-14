package com.ahorro.familiar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ahorro.familiar.data.model.Meta
import com.ahorro.familiar.ui.viewmodel.MetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaMetasScreen(
    viewModel: MetasViewModel,
    onMetaClick: (Int) -> Unit,
    onCrearMeta: () -> Unit
) {
    // La pantalla SOLO observa el StateFlow del ViewModel
    val metas by viewModel.metas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    // Cargar metas al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarMetas()
    }

    // Snackbar host para mostrar mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarError() }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐷 Ahorro Familiar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearMeta) {
                Icon(Icons.Default.Add, contentDescription = "Crear meta")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (metas.isEmpty()) {
                Text(
                    text = "No hay metas aún.\n¡Crea una con el botón +",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(metas) { meta ->
                        MetaCard(meta = meta, onClick = { onMetaClick(meta.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun MetaCard(meta: Meta, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Imagen de la meta
            AsyncImage(
                model = meta.foto,
                contentDescription = meta.nombre,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = meta.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Meta: $${String.format("%,.0f", meta.valorTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "Recaudado: $${String.format("%,.0f", meta.totalRecaudado)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${meta.miembros.size} miembro(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso
                LinearProgressIndicator(
                    progress = { meta.porcentajeCumplido / 100f },
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                )

                Text(
                    text = "${meta.porcentajeCumplido}% completado",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
