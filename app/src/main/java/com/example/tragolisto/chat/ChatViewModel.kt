package com.example.tragolisto.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.*
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.local.TragoDao
import com.example.tragolisto.data.local.TragoLocal
import com.example.tragolisto.data.model.RecetaChat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Message(
    val id: Int,
    val text: String,
    val role: String,
    val isRecipe: Boolean = false,
    val recipeData: RecetaChat? = null
)

class ChatViewModel(private val tragoDao: TragoDao) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isCargandoMensajes = MutableStateFlow(false)
    val isCargandoMensajes: StateFlow<Boolean> = _isCargandoMensajes

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private var messageId = 0
    private val gson = Gson()

    private var chatId: Int? = null
    private val userId: Int? = usuarioglobal?.id_usuario

    fun limpiarMensajes() {
        _messages.value = listOf(
            Message(
                id = 0,
                text = "¡Hola! Soy Ferni, tu bartender virtual 🍸 ¿Qué ingredientes tenés hoy?",
                role = "assistant"
            )
        )
        messageId = 1
        chatId = null
    }

    fun cargarMensajesDeChat(chatTitle: String) {
        viewModelScope.launch {
            _isCargandoMensajes.value = true
            try {
                val chatsResponse = FerniApiService.api.obtenerChats(userId)
                if (chatsResponse.isSuccessful) {
                    val chatMetadata = chatsResponse.body()
                        ?.find { it.title.equals(chatTitle, ignoreCase = true) }
                    chatMetadata?.let { meta ->
                        chatId = meta.id
                        val mensajesResponse = FerniApiService.api.obtenerMensajes(meta.id)
                        if (mensajesResponse.isSuccessful) {
                            val historial = mensajesResponse.body()?.mapIndexed { index, msg ->
                                val (isRecipe, recipeData) = if (msg.sender == "bot") {
                                    parseRecipeResponse(msg.content)
                                } else {
                                    false to null
                                }

                                Message(
                                    id = index,
                                    text = msg.content,
                                    role = msg.sender,
                                    isRecipe = isRecipe,
                                    recipeData = recipeData
                                )
                            } ?: emptyList()
                            _messages.value = historial
                            messageId = historial.size
                        }
                    }
                }

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
            } finally {
                _isCargandoMensajes.value = false
            }
        }
    }


    fun enviarMensajeAlChat(texto: String) {
        if (texto.isBlank()) return

        val nuevoMensajeUsuario = Message(id = messageId++, text = texto, role = "user")
        _messages.value = _messages.value + nuevoMensajeUsuario

        viewModelScope.launch {
            try {
                val historial = _messages.value.map {
                    FerniMessage(role = it.role, text = it.text)
                }

                val request = ChatRequest(
                    user_id = userId,
                    chat_id = chatId,
                    messages = listOf(FerniMessage(role = "user", text = texto))
                )

                val response = FerniApiService.api.manejarChat(request)

                if (response.isSuccessful) {
                    chatId = response.body()?.chat_id // si es nuevo, se guarda el id
                    val reply = response.body()?.reply ?: "Sin respuesta."
                    agregarRespuestaFerni(reply)
                } else {
                    agregarMensajeDeError("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                agregarMensajeDeError("Error: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }

    private fun agregarRespuestaFerni(reply: String) {
        var finalReply = reply

        val (isRecipe, recipeData) = parseRecipeResponse(reply)
        Log.d("DEBUG", "Respuesta recibida: $reply")
        Log.d("DEBUG", "¿Es receta? $isRecipe - ¿Data? ${recipeData != null}")

        val nuevoMensajeFerni = Message(
            id = messageId++,
            text = reply,
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
            val regex = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(response)
            val cleanedJson = match?.groups?.get(1)?.value?.trim() ?: response.trim()

            val reader = JsonReader(cleanedJson.reader())
            reader.isLenient = true

            val receta = gson.fromJson<RecetaChat>(reader, RecetaChat::class.java)
            val esReceta = receta?.type == "recipe" && receta.data != null
            Pair(esReceta, if (esReceta) receta else null)
        } catch (e: Exception) {
            Log.e("ParseFerni", "Error al parsear receta: ${e.message}")
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
