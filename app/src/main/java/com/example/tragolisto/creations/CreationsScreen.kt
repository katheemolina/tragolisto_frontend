package com.example.tragolisto.creations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Delete

data class Trago(val nombre: String, val descripcion: String, val ingredientes: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationsScreen(
    onBackClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var creaciones by remember { mutableStateOf(listOf<Trago>()) }
    var tragoSeleccionado by remember { mutableStateOf<Trago?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis creaciones",
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear trago")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (creaciones.isEmpty()) {
                Text(
                    text = "Aún no has creado ningún trago.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(creaciones) { trago ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tragoSeleccionado = trago }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(trago.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Ingredientes: ${trago.ingredientes}")
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            if (showDialog) {
                CreateDrinkDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { nombre, descripcion, ingredientes ->
                        creaciones = creaciones + Trago(nombre, descripcion, ingredientes)
                        showDialog = false
                    }
                )
            }

            if (tragoSeleccionado != null) {
                TragoDetailDialog(
                    trago = tragoSeleccionado!!,
                    onDismiss = { tragoSeleccionado = null },
                    onDelete = {
                        creaciones = creaciones - tragoSeleccionado!!
                        tragoSeleccionado = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDrinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, descripcion: String, ingredientes: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ingredienteActual by remember { mutableStateOf("") }
    var ingredientesList by remember { mutableStateOf(listOf<String>()) }

    val camposValidos = nombre.isNotBlank() && descripcion.isNotBlank() && ingredientesList.isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Crear nuevo trago") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 40) nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 225) descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = ingredienteActual,
                        onValueChange = { ingredienteActual = it },
                        label = { Text("Ingrediente") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (ingredienteActual.isNotBlank()) {
                                ingredientesList = ingredientesList + ingredienteActual.trim()
                                ingredienteActual = ""
                            }
                        },
                        modifier = Modifier.size(56.dp) // para alinear con el TextField
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar ingrediente"
                        )
                    }
                }

                if (ingredientesList.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Ingredientes agregados:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp) // Altura máxima con scroll
                        ) {
                            LazyColumn {
                                items(ingredientesList) { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "- $item",
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                ingredientesList = ingredientesList - item
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar ingrediente"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    onConfirm(nombre, descripcion, ingredientesList.joinToString(", "))
                },
                enabled = camposValidos
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun TragoDetailDialog(
    trago: Trago,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmarEliminacion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = trago.nombre) },
        text = {
            Column {
                Text("Descripción: ${trago.descripcion}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ingredientes: ${trago.ingredientes}")

                if (confirmarEliminacion) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Seguro que deseas eliminar este trago?",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            if (confirmarEliminacion) {
                                onDelete()
                            } else {
                                confirmarEliminacion = true
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(if (confirmarEliminacion) "Eliminar" else "")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        },
        // Desactivar confirm/dismissButton para que no se agreguen por defecto
        confirmButton = {},
        dismissButton = {}
    )
}
