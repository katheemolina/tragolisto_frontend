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
                Log.d(TAG, "Intentando cargar juegos...")
                val juegos = repository.getJuegos()
                Log.d(TAG, "Juegos cargados exitosamente: ${juegos.size} juegos")
                _uiState.value = JuegosFiestaUiState.Success(juegos)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "No se pudo conectar al servidor. Verifica tu conexión a internet.", e)
                _uiState.value = JuegosFiestaUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "La conexión al servidor tardó demasiado. Intenta de nuevo.", e)
                _uiState.value = JuegosFiestaUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar juegos", e)
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