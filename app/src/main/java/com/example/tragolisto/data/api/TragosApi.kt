package com.example.tragolisto.data.api

import android.util.Log
import com.example.tragolisto.data.model.BooleanDeserializer
import com.example.tragolisto.data.model.JuegoFiesta
import com.example.tragolisto.data.model.JuegosFiestaResponse
import com.example.tragolisto.data.model.Trago
import com.example.tragolisto.data.model.TragosResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface TragosApi {
    @GET("api/tragos")
    suspend fun getTragos(): Response<TragosResponse>

    @GET("api/tragos/{id}")
    suspend fun getTragoPorId(@Path("id") id: Int): Response<Trago>
}

interface JuegosFiestaApi {
    @GET("api/modofiesta")
    suspend fun getJuegos(): Response<List<JuegoFiesta>>

    @GET("api/modofiesta/{id}")
    suspend fun getJuegoPorId(@Path("id") id: Int): Response<JuegoFiesta>
}

object ApiService {
    private const val TAG = "ApiService"
    private const val BASE_URL = "http://10.0.2.2:8000/"

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

    private val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanDeserializer())
        .registerTypeAdapter(Boolean::class.javaObjectType, BooleanDeserializer())
        .create()

    init {
        Log.d(TAG, "Initializing ApiService with BASE_URL: $BASE_URL")
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val tragosApi: TragosApi = retrofit.create(TragosApi::class.java)
    val juegosFiestaApi: JuegosFiestaApi = retrofit.create(JuegosFiestaApi::class.java)
} 