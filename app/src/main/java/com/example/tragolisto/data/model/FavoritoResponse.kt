package com.example.tragolisto.data.model

import com.google.gson.annotations.SerializedName

// Este archivo ahora contiene la definición que antes estaba en ClientApi.kt
data class FavoritoResponse(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("trago_id")
    val trago_id: Int,
    val trago: Trago
) 