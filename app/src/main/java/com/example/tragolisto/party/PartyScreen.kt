package com.example.tragolisto.party

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
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
import com.example.tragolisto.data.model.JuegoFiesta
import com.example.tragolisto.ui.viewmodel.JuegoDetalleUiState
import com.example.tragolisto.ui.viewmodel.JuegosFiestaUiState
import com.example.tragolisto.ui.viewmodel.JuegosFiestaViewModel
import com.example.tragolisto.ui.viewmodel.JuegosFiestaViewModelFactory
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tragolisto.R
import com.example.tragolisto.data.local.AppDatabase
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.utils.cargarJuegosOffline
import com.example.tragolisto.data.utils.cargarRecetasOffline
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: JuegosFiestaViewModel = viewModel(
        factory = JuegosFiestaViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState is JuegosFiestaUiState.Loading) {
            val juegosOffline = cargarJuegosOffline(context)
            viewModel.setJuegos(juegosOffline)
        }
    }

    val juegoDetalleState by viewModel.juegoDetalleState.collectAsState()

    var categoriaSeleccionada by rememberSaveable { mutableStateOf("Todas") }
    var soloParaBeber by rememberSaveable { mutableStateOf(false) }
    var busqueda by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val categoriasConEmojis = mapOf(
        "Todas" to stringResource(R.string.all_categories),
        "Al azar" to stringResource(R.string.random_category),
        "Con elementos" to stringResource(R.string.with_items_category),
        "Creativo" to stringResource(R.string.creative_category),
        "De adivinanzas" to stringResource(R.string.riddle_category),
        "De cata" to stringResource(R.string.tasting_category),
        "De comunicación" to stringResource(R.string.communication_category),
        "De desafío" to stringResource(R.string.challenge_category),
        "De preguntas" to stringResource(R.string.question_category),
        "De reglas" to stringResource(R.string.rules_category),
        "Físico" to stringResource(R.string.physical_category),
        "Musical" to stringResource(R.string.musical_category)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.party_mode),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {}
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
                    var juegos = (uiState as JuegosFiestaUiState.Success).juegos

                    juegos = juegos.filter {
                        val puedeVerEsteJuego = if (it.esParaBeber) (usuarioglobal?.esMayor != false) else true

                        puedeVerEsteJuego &&
                                (categoriaSeleccionada == "Todas" || it.categoria.equals(categoriaSeleccionada, ignoreCase = true)) &&
                                (!soloParaBeber || it.esParaBeber) &&
                                (busqueda.text.isBlank() || it.nombre.contains(busqueda.text, ignoreCase = true))
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = busqueda,
                                onValueChange = { busqueda = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(R.string.search_game)) },
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
                                    categoriasConEmojis.forEach { (categoria, emojiText) ->
                                        FilterToggleButton(
                                            text = emojiText,
                                            selected = categoriaSeleccionada == categoria,
                                            onClick = { categoriaSeleccionada = categoria }
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

                            if (usuarioglobal?.esMayor != false) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    FilterToggleButton(
                                        text = stringResource(R.string.drinking_games),
                                        selected = soloParaBeber,
                                        onClick = { soloParaBeber = !soloParaBeber }
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(juegos) { juego ->
                                JuegoCard(
                                    juego = juego,
                                    onClick = { viewModel.cargarJuegoDetalle(juego.id) },
                                    emojiCategoria = categoriasConEmojis[juego.categoria] ?: juego.categoria
                                )
                            }
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
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.retry))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            if (juegoDetalleState is JuegoDetalleUiState.Success) {
                val juego = (juegoDetalleState as JuegoDetalleUiState.Success).juego
                Dialog(onDismissRequest = { viewModel.limpiarJuegoDetalle() }) {
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
                    title = { Text(stringResource(R.string.error)) },
                    text = { Text((juegoDetalleState as JuegoDetalleUiState.Error).message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.limpiarJuegoDetalle() }) {
                            Text(stringResource(R.string.ok))
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
fun JuegoCard(juego: JuegoFiesta, onClick: () -> Unit, emojiCategoria: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = juego.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.category_prefix, emojiCategoria),
                style = MaterialTheme.typography.bodyMedium
            )
            if (juego.esParaBeber) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.drinking_game_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
