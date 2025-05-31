package com.example.tragolisto.data.repository

import com.example.tragolisto.data.api.ApiService
import com.example.tragolisto.data.model.JuegoFiesta

class JuegosFiestaRepository {
    suspend fun getJuegos(): List<JuegoFiesta> {
        val response = ApiService.juegosFiestaApi.getJuegos()
        if (!response.isSuccessful) {
            throw Exception("Error al cargar los juegos: ${response.code()}")
        }
        return response.body() ?: throw Exception("No se encontraron juegos")
    }

    suspend fun getJuegoDetalle(id: Int): JuegoFiesta {
        val response = ApiService.juegosFiestaApi.getJuegoPorId(id)
        if (!response.isSuccessful) {
            throw Exception("Error al cargar el juego: ${response.code()}")
        }
        return response.body() ?: throw Exception("No se encontró el juego")
    }
} 