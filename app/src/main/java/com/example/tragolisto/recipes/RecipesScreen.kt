package com.example.tragolisto.recipes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.ui.viewmodel.FavoritoUiState
import com.example.tragolisto.ui.viewmodel.TragoDetalleUiState
import com.example.tragolisto.ui.viewmodel.TragosUiState
import com.example.tragolisto.ui.viewmodel.TragosViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.utils.cargarRecetasOffline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onBackClick: () -> Unit,
    viewModel: TragosViewModel = viewModel()
) {
    val context = LocalContext.current

    // Cargar tragos offline desde JSON y pasarlos al ViewModel
    LaunchedEffect(Unit) {
        val recetasOffline = cargarRecetasOffline(context)
        viewModel.setTragos(recetasOffline)  // función que deberás agregar en tu ViewModel para setear tragos
        viewModel.cargarFavoritos()
    }

    val uiState by viewModel.uiState.collectAsState()
    val tragoDetalleState by viewModel.tragoDetalleState.collectAsState()
    val favoritoState by viewModel.favoritoState.collectAsState()
    val tragosFavoritos by viewModel.tragosFavoritos.collectAsState()

    var dificultadSeleccionada by rememberSaveable { mutableStateOf("Todas") }
    var soloSinAlcohol by rememberSaveable { mutableStateOf(false) }
    var busqueda by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val dificultadesConEmojis = mapOf(
        "Todas" to "✨ Todas",
        "Fácil" to "👶 Fácil",
        "Media" to "🧑‍🔧 Media",
        "Difícil" to "🤯 Difícil"
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(favoritoState) {
        when (favoritoState) {
            is FavoritoUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = (favoritoState as FavoritoUiState.Success).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.limpiarFavoritoState()
            }
            is FavoritoUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (favoritoState as FavoritoUiState.Error).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.limpiarFavoritoState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Recetas",
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TragosUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TragosUiState.Success -> {
                    var tragos = (uiState as TragosUiState.Success).tragos

                    tragos = tragos.filter {
                        (dificultadSeleccionada == "Todas" || it.dificultad.equals(dificultadSeleccionada, true)) &&
                                (!soloSinAlcohol || !it.esAlcoholico) &&
                                (busqueda.text.isBlank() || it.nombre.contains(busqueda.text, ignoreCase = true))
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = busqueda,
                                onValueChange = { busqueda = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar trago...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                val scrollState = rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(scrollState)
                                        .padding(end = 32.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    dificultadesConEmojis.forEach { (dificultad, emojiText) ->
                                        FilterToggleButton(
                                            text = emojiText,
                                            selected = dificultadSeleccionada == dificultad,
                                            onClick = { dificultadSeleccionada = dificultad }
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                                startX = 200f,
                                                endX = Float.POSITIVE_INFINITY
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                FilterToggleButton(
                                    text = "🚫 Sin alcohol",
                                    selected = soloSinAlcohol,
                                    onClick = { soloSinAlcohol = !soloSinAlcohol }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tragos) { trago ->
                                val esFavorito = tragosFavoritos.contains(trago.id)
                                TragoCard(
                                    trago = trago,
                                    esFavorito = esFavorito,
                                    onFavoritoClick = { viewModel.toggleFavorito(trago.id) },
                                    onClick = { viewModel.cargarTragoDetalle(trago.id) }
                                )
                            }
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
                            onClick = { /* Podrías implementar recarga si quieres */ },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            if (tragoDetalleState is TragoDetalleUiState.Success) {
                val trago = (tragoDetalleState as TragoDetalleUiState.Success).trago
                Dialog(onDismissRequest = { viewModel.limpiarTragoDetalle() }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 6.dp
                    ) {
                        TragoDialog(trago, { viewModel.limpiarTragoDetalle() })
                    }
                }
            }

            if (tragoDetalleState is TragoDetalleUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (tragoDetalleState is TragoDetalleUiState.Error) {
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
        }
    }
}


@Composable
fun FilterToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.filledTonalButtonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    val border = if (selected) null else ButtonDefaults.outlinedButtonBorder

    Button(
        onClick = onClick,
        colors = colors,
        border = border,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}

@Composable
fun TragoCard(
    trago: Trago,
    esFavorito: Boolean,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val esModoOffline = usuarioglobal?.idToken == "offline"
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Botón de estrella en la esquina superior derecha
            if (!esModoOffline) {
                IconButton(
                    onClick = onFavoritoClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (esFavorito) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp) // Espacio para el botón de estrella
            ) {
                Text(
                    text = trago.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trago.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(label = "Dificultad", value = trago.dificultad)
                    AssistChip(label = "Tiempo", value = "${trago.tiempoPreparacionMinutos} min")
                    AssistChip(label = "Alcohol", value = if (trago.esAlcoholico) "Sí" else "No")
                }
            }
        }
    }
}

@Composable
fun AssistChip(label: String, value: String) {
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