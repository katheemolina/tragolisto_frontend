package com.example.tragolisto.chat

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ChatMetadata
import com.example.tragolisto.data.api.FerniApiService
import kotlinx.coroutines.launch
import com.example.tragolisto.data.global.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.tragolisto.R

class ChatSelectorViewModel : ViewModel() {

    private val _chats = mutableStateListOf<ChatMetadata>()
    val chats: List<ChatMetadata> = _chats

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val userId = usuarioglobal?.id_usuario

    init {
        cargarChats()
    }

    fun cargarChats() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = FerniApiService.api.obtenerChats(usuarioglobal?.id_usuario)
                if (response.isSuccessful) {
                    _chats.clear()
                    response.body()?.let { lista ->
                        _chats.addAll(lista)
                    }
                } else {
                    errorMessage = "Error al obtener chats: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage ?: "desconocido"}"
            } finally {
                isLoading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectorScreen(
    onBackClick: () -> Unit,
    viewModel: ChatSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val nuevoChatLabel = stringResource(R.string.new_chat_title)
    val misChatsLabel = stringResource(R.string.my_chats)
    val crearNuevoChatLabel = stringResource(R.string.create_new_chat)
    val seleccionaChatLabel = stringResource(R.string.select_or_create_chat)
    val volverLabel = stringResource(R.string.go_back)
    val abrirMenuLabel = stringResource(R.string.open_menu)
    val cargandoChatsLabel = stringResource(R.string.loading_chats)
    val cerrarMenuLabel = stringResource(R.string.close_menu)

    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedChatTitle by remember { mutableStateOf<String?>(nuevoChatLabel) }
    var isMenuVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = misChatsLabel,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = volverLabel)
                    }
                },
                actions = {
                    IconButton(onClick = { isMenuVisible = true }) {
                        Icon(Icons.Default.Menu, contentDescription = abrirMenuLabel)
                    }
                }
            )

            if (selectedChatTitle != null) {
                ChatScreen(
                    chatTitle = selectedChatTitle!!,
                    onBackClick = {
                        selectedChatId = null
                        selectedChatTitle = null
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seleccionaChatLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        if (isMenuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isMenuVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isMenuVisible,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    tonalElevation = 8.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                misChatsLabel,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Divider()
                            TextButton(
                                onClick = {
                                    selectedChatId = null
                                    selectedChatTitle = nuevoChatLabel
                                    isMenuVisible = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(crearNuevoChatLabel)
                            }
                        }

                        if (viewModel.isLoading) {
                            item {
                                Text(cargandoChatsLabel, modifier = Modifier.padding(16.dp))
                            }
                        } else if (viewModel.errorMessage != null) {
                            item {
                                Text(
                                    text = viewModel.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(viewModel.chats) { chat ->
                                TextButton(
                                    onClick = {
                                        selectedChatId = chat.id
                                        selectedChatTitle = chat.title
                                        isMenuVisible = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(chat.title)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(
                                onClick = { isMenuVisible = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(cerrarMenuLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}
