package com.example.tragolisto.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.model.Trago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavoritosUiState {
    data object Loading : FavoritosUiState()
    data class Success(val favoritos: List<Trago>) : FavoritosUiState()
    data class Error(val message: String) : FavoritosUiState()
}

sealed class TragoDetalleUiState {
    data object Loading : TragoDetalleUiState()
    data class Success(val trago: Trago) : TragoDetalleUiState()
    data class Error(val message: String) : TragoDetalleUiState()
}

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritosUiState>(FavoritosUiState.Loading)
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    private val _tragoDetalleState = MutableStateFlow<TragoDetalleUiState?>(null)
    val tragoDetalleState: StateFlow<TragoDetalleUiState?> = _tragoDetalleState.asStateFlow()

    init {
        cargarFavoritos()
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            _uiState.value = FavoritosUiState.Loading
            try {
                val userId = usuarioglobal?.id_usuario
                if (userId == null) {
                    _uiState.value = FavoritosUiState.Error("No se pudo obtener el ID del usuario")
                    return@launch
                }

                ClientApi.obtenerFavoritos(userId) { favoritos, error ->
                    if (favoritos != null) {
                        _uiState.value = FavoritosUiState.Success(favoritos)
                    } else {
                        _uiState.value = FavoritosUiState.Error(error ?: "Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FavoritosUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarTragoDetalle(tragoId: Int) {
        viewModelScope.launch {
            _tragoDetalleState.value = TragoDetalleUiState.Loading
            try {
                ClientApi.obtenerTragoDetalle(tragoId) { trago, error ->
                    if (trago != null) {
                        _tragoDetalleState.value = TragoDetalleUiState.Success(trago)
                    } else {
                        _tragoDetalleState.value = TragoDetalleUiState.Error(error ?: "Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _tragoDetalleState.value = TragoDetalleUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun limpiarTragoDetalle() {
        _tragoDetalleState.value = null
    }
} 