package com.example.tragolisto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.model.JuegoFiesta
import com.example.tragolisto.data.repository.JuegosFiestaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.tragolisto.data.local.JuegoFiestaDao
import com.example.tragolisto.data.local.JuegoFiestaLocal
import com.example.tragolisto.data.global.usuarioglobal

sealed class JuegosFiestaUiState {
    object Loading : JuegosFiestaUiState()
    data class Success(val juegos: List<JuegoFiesta>) : JuegosFiestaUiState()
    data class Error(val message: String) : JuegosFiestaUiState()
}

sealed class JuegoDetalleUiState {
    object Loading : JuegoDetalleUiState()
    data class Success(val juego: JuegoFiesta) : JuegoDetalleUiState()
    data class Error(val message: String) : JuegoDetalleUiState()
}

class JuegosFiestaViewModel(
    private val repository: JuegosFiestaRepository = JuegosFiestaRepository(),
    private val juegoFiestaDao: JuegoFiestaDao? = null // Nuevo parámetro opcional
) : ViewModel() {
    private val TAG = "JuegosFiestaViewModel"
    private val _uiState = MutableStateFlow<JuegosFiestaUiState>(JuegosFiestaUiState.Loading)
    val uiState: StateFlow<JuegosFiestaUiState> = _uiState.asStateFlow()

    private val _juegoDetalleState = MutableStateFlow<JuegoDetalleUiState?>(null)
    val juegoDetalleState: StateFlow<JuegoDetalleUiState?> = _juegoDetalleState.asStateFlow()

    init {
        Log.d(TAG, "Initializing JuegosFiestaViewModel")
        cargarJuegos()
    }

    fun cargarJuegos() {
        viewModelScope.launch {
            _uiState.value = JuegosFiestaUiState.Loading
            try {
                val esModoOffline = usuarioglobal?.idToken == "offline"
                val juegos = if (esModoOffline && juegoFiestaDao != null) {
                    // Cargar desde Room
                    val lista = juegoFiestaDao.obtenerTodos()
                    if (lista.isEmpty()) {
                        // Poblar si está vacío
                        juegoFiestaDao.insertarTodos(juegosFiestaOffline)
                        juegoFiestaDao.obtenerTodos()
                    } else {
                        lista
                    }
                } else {
                    // Cargar desde la API
                    repository.getJuegos()
                }
                // Convertir a modelo de UI si es necesario
                val juegosFiesta = juegos.map {
                    when (it) {
                        is JuegoFiesta -> it
                        is JuegoFiestaLocal -> it.toJuegoFiesta()
                        else -> throw Exception("Tipo de juego desconocido")
                    }
                }
                _uiState.value = JuegosFiestaUiState.Success(juegosFiesta)
            } catch (e: Exception) {
                _uiState.value = JuegosFiestaUiState.Error("Error al cargar los juegos: ${e.message}")
            }
        }
    }

    fun cargarJuegoDetalle(id: Int) {
        viewModelScope.launch {
            _juegoDetalleState.value = JuegoDetalleUiState.Loading
            try {
                val juego = repository.getJuegoDetalle(id)
                _juegoDetalleState.value = JuegoDetalleUiState.Success(juego)
            } catch (e: UnknownHostException) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("Error al cargar los detalles del juego: ${e.message}")
            }
        }
    }

    fun limpiarJuegoDetalle() {
        _juegoDetalleState.value = null
    }
}

// Extension para convertir JuegoFiestaLocal a JuegoFiesta
fun JuegoFiestaLocal.toJuegoFiesta(): JuegoFiesta = JuegoFiesta(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    materiales = materiales,
    minJugadores = min_jugadores,
    maxJugadores = max_jugadores,
    esParaBeber = es_para_beber,
    createdAt = "1970-01-01T00:00:00Z", // Valor por defecto
    updatedAt = "1970-01-01T00:00:00Z"  // Valor por defecto
)

// Lista de juegos offline (puedes moverla a un archivo común si ya existe)
val juegosFiestaOffline = listOf(
    JuegoFiestaLocal(1, "Yo Nunca Nunca", "Un clásico juego de fiesta donde los participantes dicen algo que nunca han hecho, y si alguien sí lo ha hecho, debe tomar.", "De preguntas", "Ninguno", 3, null, true),
    JuegoFiestaLocal(2, "Adivina la Canción", "Reproduce el inicio de una canción y los demás deben adivinar el título o el artista. El primero en acertar gana un punto.", "Musical", "Dispositivo de audio, lista de canciones", 2, null, false),
    JuegoFiestaLocal(25, "Círculo de la Muerte (Kings Cup)", "Un juego de cartas donde cada carta tiene una regla asociada que los jugadores deben seguir, a menudo involucrando beber.", "Con elementos", "Baraja de cartas, vasos, bebida", 3, null, true)
    // ... agrega el resto de los juegos aquí ...
) 