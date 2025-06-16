package com.example.tragolisto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tragolisto.auth.LoginScreen
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.UserGlobal
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.navigation.AppNavigation
import com.example.tragolisto.ui.theme.TragoListoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await // ¡Importante para usar .await()!

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            TragoListoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var requiresOnboarding: Boolean? by remember { mutableStateOf(null) }
                    var isLoadingInitialCheck by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        val firebaseUser = auth.currentUser

                        if (firebaseUser == null) {
                            Log.d("MainActivity", "No hay usuario autenticado. Redirigiendo a LoginScreen.")
                            val intent = Intent(this@MainActivity, LoginScreen::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.d("MainActivity", "Usuario Firebase autenticado: ${firebaseUser.email}.")

                            // *** CAMBIO CLAVE AQUÍ: Intentar obtener el token de usuarioglobal o de Firebase ***
                            var currentIdToken: String? = usuarioglobal?.idToken

                            if (currentIdToken == null) {
                                // Si usuarioglobal no tiene el token (ej. app reabierta), obtenerlo de Firebase
                                Log.d("MainActivity", "Token no encontrado en usuarioglobal. Intentando obtenerlo de Firebase.")
                                val idTokenResult = try {
                                    firebaseUser.getIdToken(true).await() // Obtener token fresco de Firebase de forma asíncrona
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error obteniendo ID Token de Firebase: ${e.message}")
                                    null
                                }
                                currentIdToken = idTokenResult?.token

                                if (currentIdToken != null) {
                                    // Actualizar usuarioglobal con el token recién obtenido
                                    usuarioglobal = usuarioglobal?.copy(
                                        uid = firebaseUser.uid,
                                        email = firebaseUser.email,
                                        nombre = firebaseUser.displayName,
                                        idToken = currentIdToken
                                    ) ?: UserGlobal(
                                        uid = firebaseUser.uid,
                                        email = firebaseUser.email,
                                        nombre = firebaseUser.displayName,
                                        idToken = currentIdToken
                                    )
                                    Log.d("MainActivity", "usuarioglobal actualizado con nuevo token de Firebase.")
                                }
                            }

                            if (currentIdToken != null) {
                                Log.d("MainActivity", "ID Token (desde global o Firebase) obtenido. Verificando estado de onboarding.")

                                ClientApi.verificarOnboarding(currentIdToken) { onboardingResponse, errorMessage ->
                                    if (onboardingResponse != null) {
                                        requiresOnboarding = onboardingResponse.requiere_onboarding
                                        // Actualizar usuarioglobal con el id_usuario del backend
                                        usuarioglobal = usuarioglobal?.copy(
                                            id_usuario = onboardingResponse.id_usuario
                                        )
                                        Log.d("MainActivity", "Estado de onboarding recibido: ${requiresOnboarding}, ID Usuario: ${onboardingResponse.id_usuario}")
                                    } else {
                                        Log.e("MainActivity", "Error al verificar onboarding: $errorMessage. Asumiendo que requiere onboarding por seguridad.")
                                        requiresOnboarding = true
                                    }
                                    isLoadingInitialCheck = false
                                }
                            } else {
                                // Si incluso después de intentar de Firebase el token es nulo
                                Log.e("MainActivity", "No se pudo obtener el ID Token para la verificación de onboarding. Redirigiendo a LoginScreen.")
                                val intent = Intent(this@MainActivity, LoginScreen::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    if (isLoadingInitialCheck) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        AppNavigation(requiresOnboarding = requiresOnboarding!!)
                    }
                }
            }
        }
    }
}