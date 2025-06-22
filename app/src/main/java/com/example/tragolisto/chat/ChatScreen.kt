package com.example.tragolisto.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tragolisto.data.local.AppDatabase
import com.example.tragolisto.data.local.TragoDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            AppDatabase.DatabaseProvider.getDatabase(LocalContext.current).dao
        )
    )
) {
    val messages by viewModel.messages.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar cuando hay un mensaje
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.hideSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(), // 👈 Importante para evitar que el teclado empuje elementos
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ferni",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Bartender Virtual",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Historial de chats"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // 👈 Agrega espacio si hay gesture navigation
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier
                        .weight(1f),
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Escribe un mensaje...") },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.enviarMensajeAlChat(inputText.trim())
                                inputText = ""
                            }
                        }
                    )
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.enviarMensajeAlChat(inputText.trim())
                            inputText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { message ->
                MessageItem(
                    message = message,
                    onSaveRecipe = { recipeData ->
                        viewModel.guardarReceta(recipeData)
                    }
                )
            }
        }
    }
}


@Composable
fun MessageItem(
    message: Message,
    onSaveRecipe: (com.example.tragolisto.data.model.RecetaChat) -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp
                ),
                color = if (isUser) Color(0xFFDCF8C6) else Color(0xFFEDEDED), // Verde claro y gris
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .defaultMinSize(minWidth = 60.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = 280.dp)
                ) {
                    if (message.isRecipe && message.recipeData != null) {
                        // Display formatted recipe
                        val recipe = message.recipeData.data
                        Text(
                            text = recipe.nombre,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recipe.descripcion,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ingredientes:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        recipe.ingredientes.forEach { ingredient ->
                            Text(
                                text = "• ${ingredient.trim()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    } else {
                        // Display plain text for user or non-recipe bot messages
                        Text(
                            text = message.text,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Botón de guardar con mejor estética
            if (!isUser && message.isRecipe && message.recipeData != null) {
                OutlinedButton(
                    onClick = { onSaveRecipe(message.recipeData) },
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(text = "💾", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = "Guardar Receta",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
