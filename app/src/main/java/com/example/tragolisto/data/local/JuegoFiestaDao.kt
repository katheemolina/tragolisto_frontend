package com.example.tragolisto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JuegoFiestaDao {
    @Query("SELECT * FROM juegos_fiesta")
    suspend fun obtenerTodos(): List<JuegoFiestaLocal>

    @Query("SELECT * FROM juegos_fiesta WHERE id = :id")
    suspend fun obtenerPorId(id: Int): JuegoFiestaLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(juegos: List<JuegoFiestaLocal>)
    
    @Query("DELETE FROM juegos_fiesta")
    suspend fun limpiarTodos()
} 