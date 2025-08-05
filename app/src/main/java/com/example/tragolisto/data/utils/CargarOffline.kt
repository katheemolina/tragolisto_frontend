package com.example.tragolisto.data.utils

import android.content.Context
import com.example.tragolisto.R
import com.example.tragolisto.data.model.BooleanDeserializer
import com.example.tragolisto.data.model.JuegoFiesta
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.model.TragosResponse
import com.example.tragolisto.data.model.JuegosResponse
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.example.tragolisto.data.global.usuarioglobal

fun cargarRecetasOffline(context: Context): List<Trago> {
    val inputStream = context.resources.openRawResource(R.raw.default_recetas)
    val json = inputStream.bufferedReader().use { it.readText() }
    val gson = GsonBuilder().registerTypeAdapter(Boolean::class.java, BooleanDeserializer()).create()
    val type = object : TypeToken<TragosResponse>() {}.type
    val response: TragosResponse = gson.fromJson(json, type)

    return if (usuarioglobal?.esMayor == true) {
        response.tragos
    } else {
        response.tragos.filter { it.esAlcoholico == false }
    }
}


fun cargarJuegosOffline(context: Context): List<JuegoFiesta> {
    val inputStream = context.resources.openRawResource(R.raw.default_juegos)
    val json = inputStream.bufferedReader().use { it.readText() }

    val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanDeserializer())
        .create()

    val type = object : TypeToken<List<JuegoFiesta>>() {}.type
    return gson.fromJson(json, type)
}