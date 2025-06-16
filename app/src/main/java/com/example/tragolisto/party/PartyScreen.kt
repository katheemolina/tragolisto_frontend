package com.example.tragolisto.party

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Modo Fiesta",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is JuegosFiestaUiState.Success -> {
                    val juegos = (uiState as JuegosFiestaUiState.Success).juegos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp), // Padding horizontal para las tarjetas
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 16.dp) // Padding superior para la lista
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
                            Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Mostrar el diálogo de detalles del juego
            if (juegoDetalleState is JuegoDetalleUiState.Success) {
                val juego = (juegoDetalleState as JuegoDetalleUiState.Success).juego
                Dialog(
                    onDismissRequest = { viewModel.limpiarJuegoDetalle() }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 6.dp
                    ) {
                        JuegoDialog(juego, { viewModel.limpiarJuegoDetalle() })
                    }
                }
            }

            if (juegoDetalleState is JuegoDetalleUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (juegoDetalleState is JuegoDetalleUiState.Error) {
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // Aumenta el padding para que coincida con TragoCard
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = juego.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // Estilo consistente con TragoCard
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (juego.esParaBeber) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Con bebidas") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Usar un color más distintivo
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(50) // Forma de pastilla para el chip
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = juego.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp)) // Espaciado consistente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Usar el mismo componente AssistChip de TragoCard para consistencia
                AssistChipCompact(label = "Categoría", value = juego.categoria)
                AssistChipCompact(
                    label = "Jugadores",
                    value = "${juego.minJugadores}${juego.maxJugadores?.let { " - $it" } ?: "+"} "
                )
            }
        }
    }
}

// Nuevo composable para chips compactos, similar a AssistChip de RecipesScreen
@Composable
fun AssistChipCompact(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}