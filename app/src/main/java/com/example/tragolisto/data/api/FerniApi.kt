package com.example.tragolisto.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// Modelos de mensajes
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

// Nuevas estructuras para nuevos endpoints
data class NewChatRequest(
    val user_id: Int?,
    val message: String
)

data class NewChatResponse(
    val chat_id: Int?,
    val reply: String
)

data class SendMessageRequest(
    val chat_id: Int?,
    val message: String
)

data class ChatMetadata(
    val id: Int,
    val user_id: Int,
    val created_at: String,
    val updated_at: String,
    val title: String
)

data class ChatMessage(
    val id: Int,
    val chat_id: Int,
    val sender: String,
    val content: String,
    val created_at: String,
    val updated_at: String
)

interface FerniApi {

    // Antiguo endpoint (opcional)
    @POST("/ferni")
    suspend fun enviarMensaje(@Body request: FerniRequest): Response<FerniResponse>

    @POST("/ferni/new-chat")
    suspend fun crearNuevoChat(@Body request: NewChatRequest): Response<NewChatResponse>

    @POST("/ferni/send-message")
    suspend fun enviarMensajeAChat(@Body request: SendMessageRequest): Response<FerniResponse>

    @GET("/ferni/chats/{userId}")
    suspend fun obtenerChats(@Path("userId") userId: Int?): Response<List<ChatMetadata>>

    @GET("/ferni/messages/{chatId}")
    suspend fun obtenerMensajes(@Path("chatId") chatId: Int?): Response<List<ChatMessage>>
}

object FerniApiService {
    private const val TAG = "FerniApiService"
    private const val BASE_URL = "http://10.0.2.2:8000/"  // Cambia según configuración local

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
