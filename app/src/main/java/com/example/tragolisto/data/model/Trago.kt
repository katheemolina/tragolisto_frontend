package com.example.tragolisto.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class Ingrediente(
    val id: Int,
    val nombre: String,
    @SerializedName("es_alcohol")
    val esAlcohol: Boolean,
    val categoria: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val pivot: PivotIngrediente
)

data class PivotIngrediente(
    @SerializedName("trago_id")
    val tragoId: Int,
    @SerializedName("ingrediente_id")
    val ingredienteId: Int,
    val cantidad: String,
    val unidad: String,
    val notas: String?
)

data class Trago(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val instrucciones: String,
    val tips: String,
    val historia: String,
    @SerializedName("es_alcoholico")
    val esAlcoholico: Boolean,
    @SerializedName("imagen_url")
    val imagenUrl: String?,
    val dificultad: String,
    @SerializedName("tiempo_preparacion_minutos")
    val tiempoPreparacionMinutos: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val ingredientes: List<Ingrediente>? = null
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

class BooleanDeserializer : JsonDeserializer<Boolean> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Boolean {
        return when (json) {
            is JsonPrimitive -> {
                when {
                    json.isNumber -> json.asInt == 1
                    json.isBoolean -> json.asBoolean
                    else -> false
                }
            }
            else -> false
        }
    }
}

data class TragosResponse(
    val total: Int,
    val tragos: List<Trago>
) 