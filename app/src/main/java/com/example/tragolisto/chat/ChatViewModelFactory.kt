package com.example.tragolisto.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tragolisto.data.local.TragoDao

class ChatViewModelFactory(private val tragoDao: TragoDao) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(tragoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 