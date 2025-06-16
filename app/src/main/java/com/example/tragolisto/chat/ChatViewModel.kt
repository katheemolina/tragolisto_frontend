package com.example.tragolisto.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.FerniApiService
import com.example.tragolisto.data.api.FerniMessage
import com.example.tragolisto.data.api.FerniRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Message(
    val id: Int,
    val text: String,
    val role: String // "user" o "assistant"
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var messageId = 0

    init {
        // Mensaje inicial del bot
        val mensajeBienvenida = Message(
            id = messageId++,
            text = "¡Hola! Soy Ferni, tu bartender virtual 🍸 ¿Qué ingredientes tenés hoy?",
            role = "assistant"
        )
        _messages.value = listOf(mensajeBienvenida)
    }

    fun enviarMensajeAlChat(texto: String) {
        if (texto.isBlank()) return

        val nuevoMensajeUsuario = Message(id = messageId++, text = texto, role = "user")
        _messages.value = _messages.value + nuevoMensajeUsuario

        viewModelScope.launch {
            try {
                val historialFerni = _messages.value.map {
                    FerniMessage(role = it.role, text = it.text)
                }

                val response = FerniApiService.api.enviarMensaje(FerniRequest(history = historialFerni))

                if (response.isSuccessful) {
                    val reply = response.body()?.reply ?: "No hay respuesta."
                    val nuevoMensajeFerni = Message(id = messageId++, text = reply, role = "assistant")
                    _messages.value = _messages.value + nuevoMensajeFerni
                } else {
                    val errorMsg = "Error: ${response.code()} - ${response.message()}"
                    val nuevoMensajeError = Message(id = messageId++, text = errorMsg, role = "assistant")
                    _messages.value = _messages.value + nuevoMensajeError
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage ?: "Error desconocido"}"
                val nuevoMensajeError = Message(id = messageId++, text = errorMsg, role = "assistant")
                _messages.value = _messages.value + nuevoMensajeError
            }
        }
    }
}
