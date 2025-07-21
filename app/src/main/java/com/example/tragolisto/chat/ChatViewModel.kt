package com.example.tragolisto.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.*
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.local.TragoDao
import com.example.tragolisto.data.local.TragoLocal
import com.example.tragolisto.data.model.RecetaChat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Representación de un mensaje en el chat
data class Message(
    val id: Int,
    val text: String,
    val role: String, // "user" o "assistant"
    val isRecipe: Boolean = false,
    val recipeData: RecetaChat? = null
)

class ChatViewModel(private val tragoDao: TragoDao) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private var messageId = 0
    private val gson = Gson()

    private var chatId: Int? = null
    private val userId: Int? = usuarioglobal?.id_usuario

    fun cargarMensajesDeChat(chatTitle: String) {
        viewModelScope.launch {
            try {
                val chatsResponse = FerniApiService.api.obtenerChats(userId)
                if (chatsResponse.isSuccessful) {
                    val chatMetadata = chatsResponse.body()?.find { it.created_at.contains(chatTitle, ignoreCase = true) }
                    chatMetadata?.let { meta ->
                        chatId = meta.id
                        val mensajesResponse = FerniApiService.api.obtenerMensajes(meta.id)
                        if (mensajesResponse.isSuccessful) {
                            val historial = mensajesResponse.body()?.mapIndexed { index, msg ->
                                Message(
                                    id = index,
                                    text = msg.content,
                                    role = msg.sender
                                )
                            } ?: emptyList()
                            _messages.value = historial
                            messageId = historial.size
                        }
                    }
                }
                // Si no hay historial, iniciar con saludo
                if (_messages.value.isEmpty()) {
                    _messages.value = listOf(
                        Message(
                            id = 0,
                            text = "¡Hola! Soy Ferni, tu bartender virtual 🍸 ¿Qué ingredientes tenés hoy?",
                            role = "assistant"
                        )
                    )
                    messageId = 1
                }
            } catch (e: Exception) {
                _messages.value = listOf(
                    Message(
                        id = 0,
                        text = "¡Hola! Soy Ferni, tu bartender virtual 🍸 ¿Qué ingredientes tenés hoy?",
                        role = "assistant"
                    )
                )
                messageId = 1
            }
        }
    }

    fun enviarMensajeAlChat(texto: String) {
        if (texto.isBlank()) return

        val nuevoMensajeUsuario = Message(id = messageId++, text = texto, role = "user")
        _messages.value = _messages.value + nuevoMensajeUsuario

        viewModelScope.launch {
            try {
                // Si el chat no existe aún, crearlo
                if (chatId == null) {
                    val newChatResponse = FerniApiService.api.crearNuevoChat(NewChatRequest(user_id = userId, message = texto))
                    if (newChatResponse.isSuccessful) {
                        chatId = newChatResponse.body()?.chat_id
                        val reply = newChatResponse.body()?.reply ?: "Sin respuesta."
                        agregarRespuestaFerni(reply)
                    } else {
                        agregarMensajeDeError("No se pudo crear el chat")
                    }
                } else {
                    val sendResponse = FerniApiService.api.enviarMensajeAChat(
                        SendMessageRequest(chat_id = chatId!!, message = texto)
                    )
                    if (sendResponse.isSuccessful) {
                        val reply = sendResponse.body()?.reply ?: "Sin respuesta."
                        agregarRespuestaFerni(reply)
                    } else {
                        agregarMensajeDeError("Error: ${sendResponse.code()} - ${sendResponse.message()}")
                    }
                }
            } catch (e: Exception) {
                agregarMensajeDeError("Error: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }

    private fun agregarRespuestaFerni(reply: String) {
        var finalReply = reply
        if (reply.trim().startsWith("{") && reply.trim().contains("respuesta_ferni")) {
            try {
                val backendResponse = gson.fromJson(reply, Map::class.java)
                finalReply = backendResponse["respuesta_ferni"]?.toString() ?: reply
            } catch (_: Exception) {}
        }

        val (isRecipe, recipeData) = parseRecipeResponse(finalReply)

        val nuevoMensajeFerni = Message(
            id = messageId++,
            text = finalReply,
            role = "assistant",
            isRecipe = isRecipe,
            recipeData = recipeData
        )
        _messages.value = _messages.value + nuevoMensajeFerni
    }

    private fun agregarMensajeDeError(error: String) {
        val errorMsg = Message(id = messageId++, text = error, role = "assistant")
        _messages.value = _messages.value + errorMsg
    }

    private fun parseRecipeResponse(response: String): Pair<Boolean, RecetaChat?> {
        return try {
            val cleanedJson = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val receta = gson.fromJson(cleanedJson, RecetaChat::class.java)
            if (receta.type == "recipe") {
                Pair(true, receta)
            } else {
                Pair(false, null)
            }
        } catch (_: JsonSyntaxException) {
            Pair(false, null)
        } catch (_: Exception) {
            Pair(false, null)
        }
    }

    fun guardarReceta(recetaChat: RecetaChat) {
        viewModelScope.launch {
            try {
                val tragoLocal = TragoLocal(
                    id = 0,
                    nombre = recetaChat.data.nombre,
                    descripcion = recetaChat.data.descripcion,
                    ingredientes = recetaChat.data.ingredientes.joinToString(separator = ", ")
                )
                tragoDao.insertar(tragoLocal)
                _snackbarMessage.value = "¡Receta guardada en Mis Creaciones!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error al guardar la receta"
            }
        }
    }

    fun hideSnackbar() {
        _snackbarMessage.value = null
    }
}
