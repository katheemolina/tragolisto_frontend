package com.example.tragolisto.data.utils

import android.content.Context
import com.example.tragolisto.R
import com.example.tragolisto.data.model.BooleanDeserializer
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.model.TragosResponse
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

fun cargarRecetasOffline(context: Context): List<Trago> {
    val inputStream = context.resources.openRawResource(R.raw.default_recetas)
    val json = inputStream.bufferedReader().use { it.readText() }
    val gson = GsonBuilder().registerTypeAdapter(Boolean::class.java, BooleanDeserializer()).create()
    val type = object : TypeToken<TragosResponse>() {}.type
    val response: TragosResponse = gson.fromJson(json, type)
    return response.tragos
}

