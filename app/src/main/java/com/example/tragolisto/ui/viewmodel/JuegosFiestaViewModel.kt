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

    fun cargarJuegos() {
        viewModelScope.launch {
            _uiState.value = JuegosFiestaUiState.Loading
            try {
                Log.d(TAG, "usuarioglobal: $usuarioglobal")
                Log.d(TAG, "idToken: ${usuarioglobal?.idToken}")
                val esModoOffline = usuarioglobal?.idToken == "offline"
                Log.d(TAG, "Modo offline: $esModoOffline")
                val juegos = if (esModoOffline) {
                    // Cargar desde Room
                    val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                    val lista = juegoFiestaDao.obtenerTodos()
                    Log.d(TAG, "Juegos en Room: ${lista.size}")
                    
                    if (lista.isEmpty() || lista.size < JuegosFiestaData.juegosFiestaOffline.size) {
                        // Poblar si está vacío o tiene menos juegos de los esperados
                        Log.d(TAG, "Repoblando base de datos con ${JuegosFiestaData.juegosFiestaOffline.size} juegos (tenía ${lista.size})")
                        juegoFiestaDao.limpiarTodos()
                        juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                        val nuevaLista = juegoFiestaDao.obtenerTodos()
                        Log.d(TAG, "Después de poblar: ${nuevaLista.size} juegos")
                        nuevaLista
                    } else {
                        Log.d(TAG, "Usando ${lista.size} juegos existentes en Room")
                        lista
                    }
                } else {
                    // Cargar desde la API
                    Log.d(TAG, "Cargando desde API")
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
                val esModoOffline = usuarioglobal?.idToken == "offline"
                Log.d(TAG, "Cargando detalle del juego $id, modo offline: $esModoOffline")
                
                val juego = if (esModoOffline) {
                    // Cargar desde Room
                    val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                    val juegoLocal = juegoFiestaDao.obtenerPorId(id)
                    if (juegoLocal != null) {
                        Log.d(TAG, "Juego $id encontrado en base de datos local")
                        juegoLocal.toJuegoFiesta()
                    } else {
                        Log.e(TAG, "Juego $id no encontrado en base de datos local")
                        // Intentar repoblar la base de datos y buscar de nuevo
                        Log.d(TAG, "Intentando repoblar base de datos y buscar de nuevo")
                        juegoFiestaDao.limpiarTodos()
                        juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                        val juegoRepoblado = juegoFiestaDao.obtenerPorId(id)
                        if (juegoRepoblado != null) {
                            Log.d(TAG, "Juego $id encontrado después de repoblar")
                            juegoRepoblado.toJuegoFiesta()
                        } else {
                            throw Exception("Juego con ID $id no encontrado en la base de datos local")
                        }
                    }
                } else {
                    // Cargar desde la API
                    repository.getJuegoDetalle(id)
                }
                
                _juegoDetalleState.value = JuegoDetalleUiState.Success(juego)
                Log.d(TAG, "Detalle del juego $id cargado exitosamente")
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error de conexión al cargar detalle del juego $id: ${e.message}")
                _juegoDetalleState.value = JuegoDetalleUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Timeout al cargar detalle del juego $id: ${e.message}")
                _juegoDetalleState.value = JuegoDetalleUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar detalle del juego $id: ${e.message}")
                _juegoDetalleState.value = JuegoDetalleUiState.Error("Error al cargar los detalles del juego: ${e.message}")
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
                Log.d(TAG, "Limpiando y repoblando base de datos")
                // Limpiar todos los datos existentes
                juegoFiestaDao.limpiarTodos()
                // Insertar todos los juegos
                juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                Log.d(TAG, "Base de datos repoblada con ${JuegosFiestaData.juegosFiestaOffline.size} juegos")
                // Recargar los juegos
                cargarJuegos()
            } catch (e: Exception) {
                Log.e(TAG, "Error al repoblar la base de datos: ${e.message}")
            }
        }
    }

    fun forzarModoOffline() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Forzando modo offline")
                // Forzar modo offline temporalmente
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                val lista = juegoFiestaDao.obtenerTodos()
                Log.d(TAG, "Juegos en Room: ${lista.size}")
                
                if (lista.size < JuegosFiestaData.juegosFiestaOffline.size) {
                    Log.d(TAG, "Repoblando base de datos")
                    juegoFiestaDao.limpiarTodos()
                    juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                }
                
                // Cargar directamente desde Room
                val juegos = juegoFiestaDao.obtenerTodos()
                val juegosFiesta = juegos.map { it.toJuegoFiesta() }
                _uiState.value = JuegosFiestaUiState.Success(juegosFiesta)
                Log.d(TAG, "Cargados ${juegosFiesta.size} juegos en modo offline")
                
                // Debug: mostrar todos los IDs disponibles
                Log.d(TAG, "IDs disponibles: ${juegos.map { it.id }}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al forzar modo offline: ${e.message}")
                _uiState.value = JuegosFiestaUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun verificarJuegosDisponibles() {
        viewModelScope.launch {
            try {
                val juegoFiestaDao = TragoListoApplication.database.juegoFiestaDao
                val juegos = juegoFiestaDao.obtenerTodos()
                Log.d(TAG, "=== VERIFICACIÓN DE JUEGOS ===")
                Log.d(TAG, "Total de juegos en BD: ${juegos.size}")
                juegos.forEach { juego ->
                    Log.d(TAG, "ID: ${juego.id}, Nombre: ${juego.nombre}")
                }
                Log.d(TAG, "=== FIN VERIFICACIÓN ===")
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar juegos: ${e.message}")
            }
        }
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

 