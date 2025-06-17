package com.example.tragolisto.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy


import kotlinx.coroutines.flow.Flow

@Dao
interface TragoDao {

    @Query("SELECT * FROM tragos")
    fun obtenerTodos(): Flow<List<TragoLocal>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(trago: TragoLocal)

    @Delete
    suspend fun eliminar(trago: TragoLocal)

}