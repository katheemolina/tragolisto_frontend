package com.example.tragolisto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ApiService
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.repository.TragosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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

sealed class FavoritoUiState {
    object Idle : FavoritoUiState()
    object Loading : FavoritoUiState()
    data class Success(val message: String) : FavoritoUiState()
    data class Error(val message: String) : FavoritoUiState()
}

class TragosViewModel(
    private val repository: TragosRepository = TragosRepository()
) : ViewModel() {
    private val TAG = "TragosViewModel"
    private val _uiState = MutableStateFlow<TragosUiState>(TragosUiState.Loading)
    val uiState: StateFlow<TragosUiState> = _uiState.asStateFlow()

    private val _tragoDetalleState = MutableStateFlow<TragoDetalleUiState?>(null)
    val tragoDetalleState: StateFlow<TragoDetalleUiState?> = _tragoDetalleState.asStateFlow()

    private val _favoritoState = MutableStateFlow<FavoritoUiState>(FavoritoUiState.Idle)
    val favoritoState: StateFlow<FavoritoUiState> = _favoritoState.asStateFlow()

    // Mapa para mantener track de los tragos favoritos y sus IDs de favorito
    // K: tragoId, V: favoritoId
    private val _favoritosMap = MutableStateFlow<Map<Int, Int>>(emptyMap())

    // Set de tragos favoritos derivado del mapa para la UI
    val tragosFavoritos: StateFlow<Set<Int>> = _favoritosMap.map { it.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        cargarTragos()
        cargarFavoritos()
    }

    fun cargarTragos() {
        viewModelScope.launch {
            try {
                val userId = usuarioglobal?.id_usuario ?: return@launch
                val response = ApiService.tragosApi.getTragos(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TragosUiState.Success(response.body()!!.tragos)
                } else {
                    _uiState.value = TragosUiState.Error("Error al obtener los tragos")
                }
            } catch (e: Exception) {
                _uiState.value = TragosUiState.Error("Excepción: ${e.localizedMessage}")
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

    fun cargarFavoritos() {
        val userId = usuarioglobal?.id_usuario
        if (userId == null) {
            return
        }

        ClientApi.obtenerFavoritos(userId) { favoritos, error ->
            if (favoritos != null) {
                // Crear el mapa de tragoId a favoritoId
                _favoritosMap.value = favoritos.associateBy({ it.trago_id }, { it.id })
            }
        }
    }

    fun toggleFavorito(tragoId: Int) {
        val userId = usuarioglobal?.id_usuario
        if (userId == null) {
            _favoritoState.value = FavoritoUiState.Error("No se pudo obtener el ID del usuario")
            return
        }

        _favoritoState.value = FavoritoUiState.Loading

        val favoritoId = _favoritosMap.value[tragoId]

        if (favoritoId != null) {
            // El trago es un favorito, hay que eliminarlo
            ClientApi.eliminarFavorito(favoritoId) { success, message ->
                if (success) {
                    _favoritoState.value = FavoritoUiState.Success(message ?: "Eliminado correctamente")
                    _favoritosMap.value = _favoritosMap.value - tragoId
                } else {
                    _favoritoState.value = FavoritoUiState.Error(message ?: "Error al eliminar favorito")
                }
            }
        } else {
            // El trago no es un favorito, hay que agregarlo
            ClientApi.agregarFavorito(userId, tragoId) { success, message ->
                if (success) {
                    _favoritoState.value = FavoritoUiState.Success("Se agregó correctamente a favoritos")
                    // Recargamos todos los favoritos para obtener el nuevo ID
                    // Esto es necesario porque la respuesta de "add" no devuelve el ID
                    cargarFavoritos()
                } else {
                    _favoritoState.value = FavoritoUiState.Error(message ?: "Error al agregar favorito")
                }
            }
        }
    }

    fun limpiarFavoritoState() {
        _favoritoState.value = FavoritoUiState.Idle
    }
} 