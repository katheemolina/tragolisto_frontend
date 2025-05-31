package com.example.tragolisto.data.repository

import com.example.tragolisto.data.api.ApiService
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.model.TragosResponse
import retrofit2.Response

class TragosRepository {
    suspend fun getTragos(): TragosResponse {
        val response = ApiService.tragosApi.getTragos()
        if (!response.isSuccessful) {
            throw Exception("Error al cargar los tragos: ${response.code()}")
        }
        return response.body() ?: throw Exception("No se encontraron tragos")
    }

    suspend fun getTragoDetalle(id: Int): Trago {
        val response = ApiService.tragosApi.getTragoPorId(id)
        if (!response.isSuccessful) {
            throw Exception("Error al cargar el trago: ${response.code()}")
        }
        return response.body() ?: throw Exception("No se encontró el trago")
    }
} 