package com.example.tragolisto.party

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
import com.example.tragolisto.data.model.JuegoFiesta
import com.example.tragolisto.ui.viewmodel.JuegoDetalleUiState
import com.example.tragolisto.ui.viewmodel.JuegosFiestaUiState
import com.example.tragolisto.ui.viewmodel.JuegosFiestaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyScreen(
    onBackClick: () -> Unit,
    viewModel: JuegosFiestaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val juegoDetalleState by viewModel.juegoDetalleState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Modo Fiesta",
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
                is JuegosFiestaUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is JuegosFiestaUiState.Success -> {
                    val juegos = (uiState as JuegosFiestaUiState.Success).juegos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(juegos) { juego ->
                            JuegoCard(
                                juego = juego,
                                onClick = { viewModel.cargarJuegoDetalle(juego.id) }
                            )
                        }
                    }
                }
                is JuegosFiestaUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as JuegosFiestaUiState.Error).message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.cargarJuegos() },
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

            // Mostrar el diálogo de detalles del juego
            when (juegoDetalleState) {
                is JuegoDetalleUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is JuegoDetalleUiState.Success -> {
                    val juego = (juegoDetalleState as JuegoDetalleUiState.Success).juego
                    JuegoDialog(
                        juego = juego,
                        onDismiss = { viewModel.limpiarJuegoDetalle() }
                    )
                }
                is JuegoDetalleUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.limpiarJuegoDetalle() },
                        title = { Text("Error") },
                        text = { Text((juegoDetalleState as JuegoDetalleUiState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.limpiarJuegoDetalle() }) {
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
fun JuegoCard(
    juego: JuegoFiesta,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = juego.nombre,
                    style = MaterialTheme.typography.titleLarge
                )
                if (juego.esParaBeber) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Con bebidas") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = juego.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Categoría: ${juego.categoria}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${juego.minJugadores}${juego.maxJugadores?.let { " - $it" } ?: "+"} jugadores",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 