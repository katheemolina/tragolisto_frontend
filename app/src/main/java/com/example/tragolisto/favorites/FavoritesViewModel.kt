package com.example.tragolisto.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.model.FavoritoResponse
import com.example.tragolisto.data.model.Trago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavoritosUiState {
    data object Loading : FavoritosUiState()
    data class Success(val favoritos: List<FavoritoResponse>) : FavoritosUiState()
    data class Error(val message: String) : FavoritosUiState()
}

sealed class FavoritoActionState {
    data object Idle : FavoritoActionState()
    data class Success(val message: String) : FavoritoActionState()
    data class Error(val message: String) : FavoritoActionState()
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

    private val _actionState = MutableStateFlow<FavoritoActionState>(FavoritoActionState.Idle)
    val actionState: StateFlow<FavoritoActionState> = _actionState.asStateFlow()

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

                ClientApi.obtenerFavoritos(userId) { favoritosResponse, error ->
                    if (favoritosResponse != null) {
                        _uiState.value = FavoritosUiState.Success(favoritosResponse)
                    } else {
                        _uiState.value = FavoritosUiState.Error(error ?: "Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FavoritosUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarFavorito(favoritoId: Int, tragoId: Int) {
        ClientApi.eliminarFavorito(favoritoId) { success, message ->
            if (success) {
                // Si se elimina correctamente, actualizamos la lista en la UI
                val currentState = _uiState.value
                if (currentState is FavoritosUiState.Success) {
                    val nuevaLista = currentState.favoritos.filterNot { it.id == favoritoId }
                    _uiState.value = FavoritosUiState.Success(nuevaLista)
                }
                _actionState.value = FavoritoActionState.Success(message ?: "Eliminado correctamente")
            } else {
                _actionState.value = FavoritoActionState.Error(message ?: "Error al eliminar")
            }
        }
    }

    fun limpiarActionState() {
        _actionState.value = FavoritoActionState.Idle
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