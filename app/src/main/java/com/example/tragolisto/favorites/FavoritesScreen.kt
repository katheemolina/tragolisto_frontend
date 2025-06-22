package com.example.tragolisto.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tragolisto.data.model.FavoritoResponse
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.recipes.TragoCard
import com.example.tragolisto.recipes.TragoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tragoDetalleState by viewModel.tragoDetalleState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var busqueda by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var dificultadSeleccionada by rememberSaveable { mutableStateOf("Todas") }
    var soloSinAlcohol by rememberSaveable { mutableStateOf(false) }

    val dificultadesConEmojis = mapOf(
        "Todas" to "✨ Todas",
        "Fácil" to "👶 Fácil",
        "Media" to "🧑‍🔧 Media",
        "Difícil" to "🤯 Difícil"
    )

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is FavoritoActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.limpiarActionState()
            }
            is FavoritoActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.limpiarActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis Favoritos",
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
                is FavoritosUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is FavoritosUiState.Success -> {
                    val favoritosOriginales = (uiState as FavoritosUiState.Success).favoritos
                    val favoritosFiltrados = favoritosOriginales.filter { favorito ->
                        val trago = favorito.trago
                        val coincideBusqueda = busqueda.text.isBlank() || trago.nombre.contains(busqueda.text, ignoreCase = true)
                        val coincideDificultad = dificultadSeleccionada == "Todas" || trago.dificultad.equals(dificultadSeleccionada, ignoreCase = true)
                        val coincideAlcohol = !soloSinAlcohol || !trago.esAlcoholico
                        coincideBusqueda && coincideDificultad && coincideAlcohol
                    }

                    if (favoritosOriginales.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No tienes favoritos guardados",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            OutlinedTextField(
                                value = busqueda,
                                onValueChange = { busqueda = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Buscar en favoritos...") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // FILTROS DE DIFICULTAD EN SCROLL HORIZONTAL
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
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // FILTRO "SIN ALCOHOL"
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

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(favoritosFiltrados) { favorito ->
                                    FavoritoCard(
                                        favorito = favorito,
                                        onDeleteClick = {
                                            viewModel.eliminarFavorito(favorito.id, favorito.trago.id)
                                        },
                                        onClick = { viewModel.cargarTragoDetalle(favorito.trago.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                is FavoritosUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as FavoritosUiState.Error).message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.cargarFavoritos() },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Dialog para mostrar el detalle del trago
            if (tragoDetalleState is TragoDetalleUiState.Success) {
                val trago = (tragoDetalleState as TragoDetalleUiState.Success).trago
                Dialog(
                    onDismissRequest = { viewModel.limpiarTragoDetalle() }
                ) {
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
fun FavoritoCard(
    favorito: FavoritoResponse,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorito.trago.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = favorito.trago.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(label = "Dificultad", value = favorito.trago.dificultad)
                    AssistChip(label = "Tiempo", value = "${favorito.trago.tiempoPreparacionMinutos} min")
                    AssistChip(label = "Alcohol", value = if (favorito.trago.esAlcoholico) "Sí" else "No")
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar Favorito",
                    tint = MaterialTheme.colorScheme.error
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