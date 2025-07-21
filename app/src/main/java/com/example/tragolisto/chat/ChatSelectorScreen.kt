package com.example.tragolisto.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ChatMetadata
import com.example.tragolisto.data.api.FerniApiService
import kotlinx.coroutines.launch
import com.example.tragolisto.data.global.*

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
    viewModel: ChatSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedChatTitle by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Mis Chats",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Divider()

                NavigationDrawerItem(
                    label = { Text("➕ Nuevo Chat") },
                    selected = selectedChatId == null && selectedChatTitle == "Nuevo Chat",
                    onClick = {
                        selectedChatId = null
                        selectedChatTitle = "Nuevo Chat"
                        scope.launch { drawerState.close() }
                    }
                )

                if (viewModel.isLoading) {
                    Text("Cargando chats...", modifier = Modifier.padding(16.dp))
                } else if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    viewModel.chats.forEach { chat ->
                        NavigationDrawerItem(
                            label = { Text(chat.title) }, // Podés usar otro campo para mostrar título
                            selected = chat.id == selectedChatId,
                            onClick = {
                                selectedChatId = chat.id
                                selectedChatTitle = chat.title
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Chats",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
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
                        text = "Selecciona o crea un chat para comenzar 🍸",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
