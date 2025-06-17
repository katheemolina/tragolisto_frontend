package com.example.tragolisto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tragos")
data class TragoLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val ingredientes: String
)
