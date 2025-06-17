package com.example.tragolisto.creations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tragolisto.data.local.AppDatabase
import com.example.tragolisto.data.local.TragoLocal
import com.example.tragolisto.recipes.InfoChip
import com.example.tragolisto.recipes.SectionTitle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.DatabaseProvider.getDatabase(context) }
    val viewModel: TragoLocalViewModel = viewModel(
        factory = TragoLocalViewModelFactory(db.dao)
    )

    var showCreateDialog by remember { mutableStateOf(false) }
    val creations by viewModel.tragos.collectAsState()
    var selectedTrago by remember { mutableStateOf<TragoLocal?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis Creaciones",
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear nuevo trago")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (creations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no has creado ningún trago.\n¡Toca el botón '+' para empezar!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(creations) { trago ->
                        TragoCreationCard(
                            trago = trago,
                            onClick = { selectedTrago = trago }
                        )
                    }
                }
            }

            if (showCreateDialog) {
                CreateDrinkDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { nombre, descripcion, ingredientes ->
                        viewModel.agregarTrago(
                            TragoLocal(0, nombre, descripcion, ingredientes)
                        )
                        showCreateDialog = false
                    }
                )
            }

            AnimatedVisibility(
                visible = selectedTrago != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedTrago?.let { trago ->
                    TragoDetailDialog(
                        trago = trago,
                        onDismiss = { selectedTrago = null },
                        onDelete = {
                            viewModel.eliminarTrago(trago)
                            selectedTrago = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TragoCreationCard(
    trago: TragoLocal,
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
                .padding(20.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ingredientes: ${trago.ingredientes.split(",").joinToString { it.trim() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateDrinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, descripcion: String, ingredientes: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ingredientesList by remember { mutableStateOf(listOf<String>()) }

    val areFieldsValid = remember(nombre, descripcion, ingredientesList) {
        nombre.isNotBlank() && descripcion.isNotBlank() && ingredientesList.isNotEmpty()
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
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Crear Trago",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 40) nombre = it },
                    label = { Text("Nombre del trago") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 225) descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 150.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Ingredientes")
                Spacer(modifier = Modifier.height(8.dp))

                IngredienteInput(
                    onAddIngredient = { newIngredient ->
                        ingredientesList = ingredientesList + newIngredient
                    }
                )

                if (ingredientesList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ingredientesList.forEach { ingredient ->
                                FilterChip(
                                    selected = false, // Chips here are just for display, not selection
                                    onClick = {
                                        ingredientesList = ingredientesList - ingredient
                                    },
                                    label = { Text(ingredient) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove ingredient",
                                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(nombre, descripcion, ingredientesList.joinToString(", ")) },
                        enabled = areFieldsValid
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun IngredienteInput(onAddIngredient: (String) -> Unit) {
    var ingredienteActual by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = ingredienteActual,
            onValueChange = { ingredienteActual = it },
            label = { Text("Nuevo ingrediente") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (ingredienteActual.isNotBlank()) {
                    onAddIngredient(ingredienteActual.trim())
                    ingredienteActual = ""
                }
            },
            enabled = ingredienteActual.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar ingrediente")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TragoDetailDialog(
    trago: TragoLocal,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trago.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = trago.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Assuming you might add difficulty, time, alcohol to TragoLocal in the future
                    // For now, these are placeholders or can be removed if not applicable
                    InfoChip("Tipo", "Creación Propia") // Example
                    // InfoChip("Dificultad", "N/A")
                    // InfoChip("Tiempo", "N/A")
                    // InfoChip("Alcohol", "N/A")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Ingredientes")
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    trago.ingredientes.split(",").filter { it.isNotBlank() }.forEach { ingrediente ->
                        Text(
                            text = "• ${ingrediente.trim()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.padding(end = 4.dp))
                        Text("Eliminar")
                    }
                    Button(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }

                if (showDeleteConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text("Confirmar Eliminación") },
                        text = { Text("¿Estás seguro de que quieres eliminar '${trago.nombre}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onDelete()
                                    showDeleteConfirmation = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmation = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}