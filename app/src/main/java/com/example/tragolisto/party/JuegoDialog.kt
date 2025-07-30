package com.example.tragolisto.party

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.tragolisto.data.model.JuegoFiesta
import kotlin.text.toRegex

// Función para extraer el ID del video y limpiar la descripción (definida anteriormente por la base de datos)
fun extractYouTubeVideoIdAndCleanDescription(description: String): Pair<String?, String> {
    val youtubeRegex = """(?:https?://)?(?:www\.)?(?:youtube\.com/(?:watch\?v=|embed/|v/|shorts/)|youtu\.be/)([a-zA-Z0-9_-]{11})(?:\?[^\s]*)?""".toRegex()
    val matchResult = youtubeRegex.find(description)

    return if (matchResult != null) {
        val videoId = matchResult.groupValues[1]
        val cleanedDescription = description.replace(matchResult.value, "").trim()
        Pair(videoId, cleanedDescription)
    } else {
        Pair(null, description)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JuegoDialog(
    juego: JuegoFiesta,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // Procesamos la descripción para extraer el ID del video y el texto limpio
    // Usamos remember para que esto solo se calcule una vez o cuando 'juego.descripcion' cambie
    val (videoId, cleanedDescription) = remember(juego.descripcion) {
        extractYouTubeVideoIdAndCleanDescription(juego.descripcion)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = juego.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar la miniatura del video si existe un ID
                videoId?.let { id ->
                    val thumbnailUrl = "https://img.youtube.com/vi/$id/0.jpg" // URL de la miniatura estándar
                    Image(
                        painter = rememberAsyncImagePainter(model = thumbnailUrl),
                        contentDescription = "Miniatura del video de YouTube",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f) // Proporción común para videos
                            .clickable {
                                // Abrir el video en la app de YouTube o en el navegador
                                val appIntent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=$id")
                                )
                                try {
                                    context.startActivity(appIntent)
                                } catch (ex: Exception) {
                                    context.startActivity(webIntent)
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Mostrar la descripción limpia (sin el enlace de YouTube)
                Text(
                    text = cleanedDescription, // Usar la descripción limpia aquí
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Chips de info
                FlowRow(
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
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Materiales
                SectionTitle("Materiales")
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
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "¡Juego con bebidas!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
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