package com.example.tragolisto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tragolisto.data.repository.JuegosFiestaRepository

class JuegosFiestaViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JuegosFiestaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JuegosFiestaViewModel(JuegosFiestaRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 