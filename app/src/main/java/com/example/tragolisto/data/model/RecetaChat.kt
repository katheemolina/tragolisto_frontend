package com.example.tragolisto.data.model

data class RecetaChat(
    val type: String,
    val data: RecetaData
)

data class RecetaData(
    val nombre: String,
    val descripcion: String,
    val ingredientes: List<String>
) 