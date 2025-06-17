package com.example.tragolisto.creations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tragolisto.data.local.TragoDao
import com.example.tragolisto.data.local.TragoLocal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TragoLocalViewModel(private val dao: TragoDao) : ViewModel() {

    val tragos = dao.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarTrago(trago: TragoLocal) {
        viewModelScope.launch {
            dao.insertar(trago)
        }
    }

    fun eliminarTrago(trago: TragoLocal) {
        viewModelScope.launch {
            dao.eliminar(trago)
        }
    }
}