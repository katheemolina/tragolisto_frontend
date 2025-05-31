package com.example.tragolisto.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.ui.viewmodel.TragoDetalleUiState
import com.example.tragolisto.ui.viewmodel.TragosUiState
import com.example.tragolisto.ui.viewmodel.TragosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onBackClick: () -> Unit,
    viewModel: TragosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tragoDetalleState by viewModel.tragoDetalleState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Recetas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TragosUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TragosUiState.Success -> {
                    val tragos = (uiState as TragosUiState.Success).tragos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tragos) { trago ->
                            TragoCard(
                                trago = trago,
                                onClick = { viewModel.cargarTragoDetalle(trago.id) }
                            )
                        }
                    }
                }
                is TragosUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as TragosUiState.Error).message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.cargarTragos() },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reintentar"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Mostrar el diálogo de detalles del trago
            when (tragoDetalleState) {
                is TragoDetalleUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TragoDetalleUiState.Success -> {
                    val trago = (tragoDetalleState as TragoDetalleUiState.Success).trago
                    TragoDialog(
                        trago = trago,
                        onDismiss = { viewModel.limpiarTragoDetalle() }
                    )
                }
                is TragoDetalleUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.limpiarTragoDetalle() },
                        title = { Text("Error") },
                        text = { Text((tragoDetalleState as TragoDetalleUiState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.limpiarTragoDetalle() }) {
                                Text("OK")
                            }
                        }
                    )
                }
                null -> { /* No dialog to show */ }
            }
        }
    }
}

@Composable
fun TragoCard(
    trago: Trago,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = trago.nombre,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trago.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dificultad: ${trago.dificultad}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${trago.tiempoPreparacionMinutos} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 