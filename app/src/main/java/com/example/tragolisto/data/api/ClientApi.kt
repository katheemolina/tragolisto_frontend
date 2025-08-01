package com.example.tragolisto.data.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.tragolisto.data.global.usuarioglobal
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.example.tragolisto.data.model.FavoritoResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import com.example.tragolisto.data.model.Trago
import java.time.Period

object ClientApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "http://10.0.2.2:8000"
    private val gson = Gson()
    private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    data class OnboardingCheckResponse(
        val existe: Boolean,
        val id_usuario: Int,
        val fecha_nacimiento: String?,
        val requiere_onboarding: Boolean,
        val es_mayor: Boolean
    )

    fun sendGoogleLoginData(idToken: String, uid: String?, email: String?, name: String, callback: (Boolean, String?) -> Unit) {
        val backendUrl = "$BASE_URL/login-google"
        Log.w("LoginScreen", idToken)
        val jsonBody = """
            {
                "id_token": "$idToken",
                "uid": "$uid",
                "email": "$email",
                "name": "$name"
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody(MEDIA_TYPE_JSON)

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al enviar datos de login a backend", e)
                callback(false, "Error de red: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de login del backend: $responseData")

                if (response.isSuccessful) {
                    callback(true, responseData)
                } else {
                    callback(false, "Error de servidor: ${response.code} - $responseData")
                }
            }
        })
    }

    fun verificarOnboarding(idToken: String, callback: (OnboardingCheckResponse?, String?) -> Unit) {
        val backendUrl = "$BASE_URL/verificar-onboarding"
        Log.w("LoginScreen", idToken)
        val jsonBody = """
            {
                "id_token": "$idToken"
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody(MEDIA_TYPE_JSON)

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al verificar onboarding", e)
                callback(null, "Error de red: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de verificación de onboarding: $responseData")

                if (response.isSuccessful && responseData != null) {
                    try {
                        val onboardingResponse = gson.fromJson(responseData, OnboardingCheckResponse::class.java)
                        callback(onboardingResponse, null)
                    } catch (e: Exception) {
                        Log.e("ClientApi", "Error al parsear respuesta de verificación de onboarding", e)
                        callback(null, "Error al parsear respuesta: ${e.localizedMessage}")
                    }
                } else {
                    callback(null, "Error de servidor: ${response.code} - $responseData")
                }
            }
        })
    }

    fun completarOnboarding(idToken: String, birthDate: LocalDate, callback: (Boolean, String?) -> Unit) {
        val backendUrl = "$BASE_URL/completar-onboarding"
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = birthDate.format(dateFormat)

        Log.w("LoginScreen", idToken)
        Log.w("LoginScreen", formattedDate)

        val jsonBody = JSONObject().apply {
            put("id_token", idToken)
            put("fecha_nacimiento", formattedDate)
        }

        val requestBody = jsonBody.toString().toRequestBody(MEDIA_TYPE_JSON)

        val currentDate = LocalDate.now()
        val age = Period.between(birthDate, currentDate).years
        if (age < 18) {
            usuarioglobal?.esMayor?: false
        } else {
            usuarioglobal?.esMayor?: true
        }

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al completar onboarding", e)
                // *** CORRECCIÓN: Ejecutar callback en el hilo principal ***
                Handler(Looper.getMainLooper()).post {
                    callback(false, "Error de red: ${e.localizedMessage}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de completar onboarding: $responseData")

                // *** CORRECCIÓN: Ejecutar callback en el hilo principal ***
                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            val success = jsonResponse.optBoolean("success", true)
                            val message = jsonResponse.optString("message", "Onboarding completado con éxito.")
                            callback(success, message)
                        } catch (e: JSONException) {
                            Log.e("ClientApi", "Error parseando respuesta de completar onboarding: ${e.message}")
                            callback(false, "Respuesta inválida del servidor al completar onboarding.")
                        }
                    } else {
                        callback(false, "Error de servidor al completar onboarding: ${response.code} - $responseData")
                    }
                }
            }
        })
    }

    fun obtenerFavoritos(userId: Int, callback: (List<FavoritoResponse>?, String?) -> Unit) {
        val backendUrl = "$BASE_URL/api/favoritos/$userId"
        
        val request = Request.Builder()
            .url(backendUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al obtener favoritos", e)
                callback(null, "Error de red: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    try {
                        val favoritosResponse = gson.fromJson(responseData, Array<FavoritoResponse>::class.java)
                        callback(favoritosResponse.toList(), null)
                    } catch (e: Exception) {
                        Log.e("ClientApi", "Error al parsear respuesta de favoritos", e)
                        callback(null, "Error al parsear respuesta: ${e.localizedMessage}")
                    }
                } else {
                    callback(null, "Error de servidor: ${response.code} - $responseData")
                }
            }
        })
    }

    fun obtenerTragoDetalle(tragoId: Int, callback: (Trago?, String?) -> Unit) {
        val backendUrl = "$BASE_URL/api/tragos/$tragoId"
        
        val request = Request.Builder()
            .url(backendUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al obtener detalle del trago", e)
                callback(null, "Error de red: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de detalle del trago: $responseData")

                if (response.isSuccessful && responseData != null) {
                    try {
                        val trago = gson.fromJson(responseData, Trago::class.java)
                        callback(trago, null)
                    } catch (e: Exception) {
                        Log.e("ClientApi", "Error al parsear respuesta del trago", e)
                        callback(null, "Error al parsear respuesta: ${e.localizedMessage}")
                    }
                } else {
                    callback(null, "Error de servidor: ${response.code} - $responseData")
                }
            }
        })
    }

    fun agregarFavorito(userId: Int, tragoId: Int, callback: (Boolean, String?) -> Unit) {
        val backendUrl = "$BASE_URL/api/favoritos"
        
        val jsonBody = """
            {
                "user_id": $userId,
                "trago_id": $tragoId
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody(MEDIA_TYPE_JSON)

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al agregar favorito", e)
                Handler(Looper.getMainLooper()).post {
                    callback(false, "Error de red: ${e.localizedMessage}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de agregar favorito: $responseData")

                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful && responseData != null) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            if (jsonResponse.has("status") && jsonResponse.getBoolean("status")) {
                                callback(true, jsonResponse.optString("message", "Guardado correctamente"))
                            } else if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val message = error.optString("message", "Error al agregar favorito")
                                callback(false, message)
                            } else {
                                callback(false, "Respuesta inesperada del servidor")
                            }
                        } catch (e: JSONException) {
                            Log.e("ClientApi", "Error parseando respuesta de agregar favorito: ${e.message}")
                            callback(false, "Error al procesar respuesta del servidor")
                        }
                    } else {
                        callback(false, "Error de servidor: ${response.code} - $responseData")
                    }
                }
            }
        })
    }

    fun eliminarFavorito(favoritoId: Int, callback: (Boolean, String?) -> Unit) {
        val backendUrl = "$BASE_URL/api/favoritos/$favoritoId"

        val request = Request.Builder()
            .url(backendUrl)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClientApi", "Error al eliminar favorito", e)
                Handler(Looper.getMainLooper()).post {
                    callback(false, "Error de red: ${e.localizedMessage}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("ClientApi", "Respuesta de eliminar favorito: $responseData")

                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) {
                        callback(true, "Eliminado de favoritos")
                    } else {
                        // Aquí podrías intentar parsear un JSON de error si tu API lo envía
                        callback(false, "Error al eliminar: ${response.code}")
                    }
                }
            }
        })
    }
}