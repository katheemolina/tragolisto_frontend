package com.example.tragolisto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.repository.TragosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class TragosUiState {
    object Loading : TragosUiState()
    data class Success(val tragos: List<Trago>) : TragosUiState()
    data class Error(val message: String) : TragosUiState()
}

sealed class TragoDetalleUiState {
    object Loading : TragoDetalleUiState()
    data class Success(val trago: Trago) : TragoDetalleUiState()
    data class Error(val message: String) : TragoDetalleUiState()
}

class TragosViewModel(
    private val repository: TragosRepository = TragosRepository()
) : ViewModel() {
    private val TAG = "TragosViewModel"
    private val _uiState = MutableStateFlow<TragosUiState>(TragosUiState.Loading)
    val uiState: StateFlow<TragosUiState> = _uiState.asStateFlow()

    private val _tragoDetalleState = MutableStateFlow<TragoDetalleUiState?>(null)
    val tragoDetalleState: StateFlow<TragoDetalleUiState?> = _tragoDetalleState.asStateFlow()

    init {
        Log.d(TAG, "Initializing TragosViewModel")
        cargarTragos()
    }

    fun cargarTragos() {
        viewModelScope.launch {
            _uiState.value = TragosUiState.Loading
            try {
                Log.d(TAG, "Intentando cargar tragos...")
                val tragosResponse = repository.getTragos()
                Log.d(TAG, "Tragos cargados exitosamente: ${tragosResponse.total} tragos")
                _uiState.value = TragosUiState.Success(tragosResponse.tragos)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "No se pudo conectar al servidor. Verifica tu conexión a internet.", e)
                _uiState.value = TragosUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "La conexión al servidor tardó demasiado. Intenta de nuevo.", e)
                _uiState.value = TragosUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar tragos", e)
                _uiState.value = TragosUiState.Error("Error al cargar los tragos: ${e.message}")
            }
        }
    }

    fun cargarTragoDetalle(id: Int) {
        viewModelScope.launch {
            _tragoDetalleState.value = TragoDetalleUiState.Loading
            try {
                val trago = repository.getTragoDetalle(id)
                _tragoDetalleState.value = TragoDetalleUiState.Success(trago)
            } catch (e: UnknownHostException) {
                _tragoDetalleState.value = TragoDetalleUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                _tragoDetalleState.value = TragoDetalleUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                _tragoDetalleState.value = TragoDetalleUiState.Error("Error al cargar los detalles del trago: ${e.message}")
            }
        }
    }

    fun limpiarTragoDetalle() {
        _tragoDetalleState.value = null
    }
} 