package com.example.tragolisto.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class JuegoFiesta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val materiales: String,
    @SerializedName("min_jugadores")
    val minJugadores: Int,
    @SerializedName("max_jugadores")
    val maxJugadores: Int?,
    @SerializedName("es_para_beber")
    val esParaBeber: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) {
    // Función de extensión para convertir createdAt a LocalDateTime
    fun getCreatedAtDateTime(): LocalDateTime? {
        return try {
            LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // Función de extensión para convertir updatedAt a LocalDateTime
    fun getUpdatedAtDateTime(): LocalDateTime? {
        return try {
            LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

data class JuegosFiestaResponse(
    val total: Int,
    val juegos: List<JuegoFiesta>
) 