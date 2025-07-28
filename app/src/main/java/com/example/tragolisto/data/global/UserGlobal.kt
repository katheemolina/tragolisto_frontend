package com.example.tragolisto.data.global

data class UserGlobal(
    val uid: String?,
    val email: String?,
    val nombre: String?,
    val idToken: String?,
    val id_usuario: Int? = null, // ID del usuario en el backend
    var esMayor: Boolean
)
