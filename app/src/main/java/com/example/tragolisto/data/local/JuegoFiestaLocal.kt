package com.example.tragolisto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "juegos_fiesta")
data class JuegoFiestaLocal(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val materiales: String,
    val min_jugadores: Int,
    val max_jugadores: Int?,
    val es_para_beber: Boolean
) 