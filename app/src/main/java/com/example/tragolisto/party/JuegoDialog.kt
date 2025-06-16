package com.example.tragolisto.party

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tragolisto.data.model.JuegoFiesta

@OptIn(ExperimentalLayoutApi::class) // Necesario para FlowRow
@Composable
fun JuegoDialog(
    juego: JuegoFiesta,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge, // Forma grande para el diálogo
            tonalElevation = 6.dp, // Elevación para dar profundidad
            color = MaterialTheme.colorScheme.background // Color de fondo del tema
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp) // Padding consistente con TragoDialog
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = juego.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), // Estilo de título fuerte
                        modifier = Modifier.weight(1f) // Ocupa el espacio disponible
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = juego.descripcion,
                    style = MaterialTheme.typography.bodyLarge, // Texto de cuerpo más grande
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp)) // Espaciado consistente

                // Chips de info
                FlowRow( // Usar FlowRow para un layout flexible de chips
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(label = "Categoría", value = juego.categoria)
                    InfoChip(
                        label = "Jugadores",
                        value = "${juego.minJugadores}${juego.maxJugadores?.let { " - $it" } ?: "+"} "
                    )
                    InfoChip(label = "Bebidas", value = if (juego.esParaBeber) "Sí" else "No")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider() // Separador para secciones
                Spacer(modifier = Modifier.height(16.dp))

                // Materiales
                SectionTitle("Materiales") // Título de sección usando el composable SectionTitle
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = juego.materiales,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mensaje de bebidas
                if (juego.esParaBeber) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer // Color coherente con el chip en la tarjeta
                        ),
                        shape = MaterialTheme.shapes.medium, // Forma del Card
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "¡Juego con bebidas!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold, // Semibold para consistencia
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Recuerda beber con responsabilidad y respetar los límites de cada persona.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// Reutilizamos InfoChip y SectionTitle de recipes para consistencia
@Composable
private fun InfoChip(label: String, value: String) {
    AssistChip(
        onClick = {},
        label = {
            Text("$label: $value")
        },
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    )
}