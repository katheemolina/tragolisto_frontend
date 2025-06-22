package com.example.tragolisto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.usuarioglobal
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

    // Set para mantener track de los tragos favoritos
    private val _tragosFavoritos = MutableStateFlow<Set<Int>>(emptySet())
    val tragosFavoritos: StateFlow<Set<Int>> = _tragosFavoritos.asStateFlow()

    init {
        cargarTragos()
        cargarFavoritos()
    }

    fun cargarTragos() {
        viewModelScope.launch {
            _uiState.value = TragosUiState.Loading
            try {
                val tragosResponse = repository.getTragos()
                _uiState.value = TragosUiState.Success(tragosResponse.tragos)
            } catch (e: UnknownHostException) {
                _uiState.value = TragosUiState.Error("No se pudo conectar al servidor. Verifica tu conexión a internet.")
            } catch (e: SocketTimeoutException) {
                _uiState.value = TragosUiState.Error("La conexión al servidor tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
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

    fun cargarFavoritos() {
        val userId = usuarioglobal?.id_usuario
        if (userId == null) {
            return
        }

        ClientApi.obtenerFavoritos(userId) { favoritos, error ->
            if (favoritos != null) {
                val favoritosIds = favoritos.map { it.trago_id }.toSet()
                _tragosFavoritos.value = favoritosIds
            }
        }
    }

    fun agregarFavorito(tragoId: Int) {
        val userId = usuarioglobal?.id_usuario
        if (userId == null) {
            _favoritoState.value = FavoritoUiState.Error("No se pudo obtener el ID del usuario")
            return
        }

        _favoritoState.value = FavoritoUiState.Loading

        ClientApi.agregarFavorito(userId, tragoId) { success, message ->
            if (success) {
                _favoritoState.value = FavoritoUiState.Success("Se agregó correctamente a favoritos")
                _tragosFavoritos.value = _tragosFavoritos.value + tragoId
            } else {
                _favoritoState.value = FavoritoUiState.Error(message ?: "Error al agregar favorito")
            }
        }
    }

    fun limpiarFavoritoState() {
        _favoritoState.value = FavoritoUiState.Idle
    }
} 