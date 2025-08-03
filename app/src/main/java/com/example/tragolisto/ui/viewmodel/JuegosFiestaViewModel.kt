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
import com.example.tragolisto.data.local.JuegoFiestaLocal
import com.example.tragolisto.data.local.JuegosFiestaData
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.TragoListoApplication

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
    private val repository: JuegosFiestaRepository = JuegosFiestaRepository()
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

    fun setJuegos(juegosOffline: List<JuegoFiesta>) {
        _uiState.value = JuegosFiestaUiState.Success(juegosOffline)
    }

    fun cargarJuegos() {
        viewModelScope.launch {
            _uiState.value = JuegosFiestaUiState.Loading
            try {
                val esModoOffline = usuarioglobal?.idToken == "offline"
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao

                val juegos = if (esModoOffline) {
                    val lista = juegoFiestaDao.obtenerTodos()
                    if (lista.isEmpty() || lista.size < JuegosFiestaData.juegosFiestaOffline.size) {
                        juegoFiestaDao.limpiarTodos()
                        juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                    }
                    juegoFiestaDao.obtenerTodos()
                } else {
                    repository.getJuegos()
                }

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
                val esModoOffline = usuarioglobal?.idToken == "offline"
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao

                val juego = if (esModoOffline) {
                    val juegoLocal = juegoFiestaDao.obtenerPorId(id) ?: run {
                        juegoFiestaDao.limpiarTodos()
                        juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                        juegoFiestaDao.obtenerPorId(id)
                    } ?: throw Exception("Juego con ID $id no encontrado")

                    juegoLocal.toJuegoFiesta()
                } else {
                    repository.getJuegoDetalle(id)
                }

                _juegoDetalleState.value = JuegoDetalleUiState.Success(juego)
            } catch (e: UnknownHostException) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("No se pudo conectar al servidor.")
            } catch (e: SocketTimeoutException) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("Conexión tardó demasiado.")
            } catch (e: Exception) {
                _juegoDetalleState.value = JuegoDetalleUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun limpiarJuegoDetalle() {
        _juegoDetalleState.value = null
    }

    fun repoblarBaseDeDatos() {
        viewModelScope.launch {
            try {
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                juegoFiestaDao.limpiarTodos()
                juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                cargarJuegos()
            } catch (e: Exception) {
                Log.e(TAG, "Error al repoblar la base de datos: ${e.message}")
            }
        }
    }

    fun verificarJuegosDisponibles() {
        viewModelScope.launch {
            try {
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                val juegos = juegoFiestaDao.obtenerTodos()
                Log.d(TAG, "Juegos disponibles en BD: ${juegos.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar juegos: ${e.message}")
            }
        }
    }
}

fun JuegoFiestaLocal.toJuegoFiesta(): JuegoFiesta = JuegoFiesta(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    materiales = materiales,
    minJugadores = min_jugadores,
    maxJugadores = max_jugadores,
    esParaBeber = es_para_beber,
    video = "",
    createdAt = "1970-01-01T00:00:00Z",
    updatedAt = "1970-01-01T00:00:00Z"
)
