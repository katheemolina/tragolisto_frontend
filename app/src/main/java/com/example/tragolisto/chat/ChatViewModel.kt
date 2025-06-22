package com.example.tragolisto.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.FerniApiService
import com.example.tragolisto.data.api.FerniMessage
import com.example.tragolisto.data.api.FerniRequest
import com.example.tragolisto.data.local.TragoLocal
import com.example.tragolisto.data.local.TragoDao
import com.example.tragolisto.data.model.RecetaChat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.annotations.SerializedName

data class Message(
    val id: Int,
    val text: String,
    val role: String, // "user" o "assistant"
    val isRecipe: Boolean = false,
    val recipeData: RecetaChat? = null
)

// Nueva clase para mapear la respuesta completa del backend
data class FerniBackendResponse(
    @SerializedName("respuesta_ferni")
    val respuestaFerni: String? // Hacemos que sea nulable para evitar crashes
)

class ChatViewModel(private val tragoDao: TragoDao) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private var messageId = 0
    private val gson = Gson()

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
                    
                    var finalReply = reply
                    try {
                        // Intenta "desenvolver" la respuesta
                        val backendResponse = gson.fromJson(reply, FerniBackendResponse::class.java)
                        // Si respuestaFerni es null, usamos el 'reply' original como respaldo
                        finalReply = backendResponse.respuestaFerni ?: reply
                    } catch (e: Exception) {
                        // Si falla, significa que la respuesta no estaba envuelta. No hacemos nada.
                    }

                    // Intentar parsear como receta estructurada
                    val (isRecipe, recipeData) = parseRecipeResponse(finalReply)
                    
                    val nuevoMensajeFerni = Message(
                        id = messageId++, 
                        text = finalReply, 
                        role = "assistant",
                        isRecipe = isRecipe,
                        recipeData = recipeData
                    )
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

    private fun parseRecipeResponse(response: String): Pair<Boolean, RecetaChat?> {
        try {
            // Limpiar el JSON de los ``` y espacios que agrega Gemini
            val cleanedJson = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val receta = gson.fromJson(cleanedJson, RecetaChat::class.java)
            if (receta.type == "recipe") {
                return Pair(true, receta)
            }
            return Pair(false, null)
        } catch (e: JsonSyntaxException) {
            // No es un JSON válido o no tiene el formato esperado, es texto plano.
            return Pair(false, null)
        } catch (e: Exception) {
            return Pair(false, null)
        }
    }

    fun guardarReceta(recetaChat: RecetaChat) {
        viewModelScope.launch {
            try {
                val tragoLocal = TragoLocal(
                    id = 0, // Room generará el ID automáticamente
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
