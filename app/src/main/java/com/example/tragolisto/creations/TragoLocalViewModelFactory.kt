package com.example.tragolisto.creations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tragolisto.data.local.TragoDao

class TragoLocalViewModelFactory(private val dao: TragoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TragoLocalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TragoLocalViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}