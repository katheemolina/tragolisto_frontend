package com.example.tragolisto.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Modelos para la API Ferni
data class FerniMessage(
    val role: String,  // "user" o "assistant"
    val text: String
)

data class FerniRequest(
    val history: List<FerniMessage>
)

data class FerniResponse(
    val reply: String
)

interface FerniApi {
    @POST("/ferni")
    suspend fun enviarMensaje(@Body request: FerniRequest): Response<FerniResponse>
}

object FerniApiService {
    private const val TAG = "FerniApiService"
    private const val BASE_URL = "http://10.0.2.2:8000/"  // Cambia a tu URL base

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "OkHttp: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: FerniApi = retrofit.create(FerniApi::class.java)
}
