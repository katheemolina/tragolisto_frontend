package com.example.tragolisto.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Added for context access in Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tragolisto.MainActivity // Keep the import, as MainActivity is still the entry point
import com.example.tragolisto.R
import com.example.tragolisto.data.api.ClientApi
import com.example.tragolisto.data.global.UserGlobal
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.ui.theme.TragoListoTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush


class LoginScreen : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializar el lanzador de inicio de sesión
        // El resultado de este lanzador manejará la autenticación con Firebase
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Cuando se obtiene una cuenta con éxito, autenticar con Firebase
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginScreen", "Google sign in failed", e)
                // Usar setContent aquí para actualizar la UI con el error de forma segura.
                // Es importante que los estados `isLoading` y `errorMessage` se manejen dentro del `setContent`
                // para que Compose pueda reaccionar a los cambios.
                setContent {
                    TragoListoTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            // Los estados aquí deben ser `remember`ed y `mutableStateOf`
                            val isLoadingState by remember { mutableStateOf(false) }
                            val errorMessageState by remember { mutableStateOf<String?>(
                                "Error al iniciar sesión con Google: ${e.localizedMessage}"
                            ) }
                            LoginScreenContent(
                                isLoading = isLoadingState,
                                errorMessage = errorMessageState,
                                onGoogleSignIn = { signInWithGoogle() }
                            )
                        }
                    }
                }
            }
        }

        // Establecer el contenido inicial de Compose para la pantalla de Login
        setContent {
            TragoListoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Estos estados son para la UI del LoginScreen.
                    var isLoading by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf<String?>(null) }

                    LoginScreenContent(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onGoogleSignIn = {
                            isLoading = true
                            errorMessage = null
                            signInWithGoogle()
                        }
                    )
                }
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val email = user?.email
                    val nombre = user?.displayName
                    Log.d("LoginScreen", "Sign-in success: ${user?.email}, UID: $uid")

                    // Almacenar datos del usuario globalmente (si `usuarioglobal` es accesible)
                    usuarioglobal = UserGlobal(
                        uid = uid,
                        email = email,
                        nombre = nombre,
                        idToken = idToken
                    )

                    // Enviar los datos iniciales de Google al backend
                    ClientApi.sendGoogleLoginData(idToken, uid, email, nombre ?: "") { success, responseData ->
                        if (success) {
                            Log.d("LoginScreen", "Datos iniciales de usuario enviados al backend con éxito. Finalizando LoginScreen.")
                            // --- LA CORRECCIÓN CLAVE AQUÍ ---
                            // Inicia MainActivity y luego cierra esta actividad de LoginScreen.
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            // Estas flags aseguran que MainActivity sea la única actividad en la pila
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish() // Ahora sí, cierra LoginScreen después de lanzar MainActivity
                        } else {
                            // Si falla el envío al backend, actualiza la UI de LoginScreen para mostrar el error.
                            // Esto DEBE hacerse en el hilo principal.
                            runOnUiThread {
                                setContent { // Re-establecer el contenido con el mensaje de error
                                    TragoListoTheme {
                                        Surface(modifier = Modifier.fillMaxSize()) {
                                            val isLoadingState by remember { mutableStateOf(false) }
                                            val errorMessageState by remember { mutableStateOf<String?>(
                                                "Error al enviar datos al backend: $responseData"
                                            ) }
                                            LoginScreenContent(
                                                isLoading = isLoadingState,
                                                errorMessage = errorMessageState,
                                                onGoogleSignIn = { signInWithGoogle() }
                                            )
                                        }
                                    }
                                }
                                Log.e("LoginScreen", "Error al enviar datos de usuario al backend: $responseData")
                            }
                        }
                    }

                } else {
                    // Manejar fallo de autenticación de Firebase
                    Log.w("LoginScreen", "Sign-in failed", task.exception)
                    runOnUiThread {
                        setContent { // Re-establecer el contenido con el mensaje de error de Firebase
                            TragoListoTheme {
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    val isLoadingState by remember { mutableStateOf(false) }
                                    val errorMessageState by remember { mutableStateOf<String?>(
                                        "Error de autenticación: ${task.exception?.localizedMessage ?: "Error desconocido"}"
                                    ) }
                                    LoginScreenContent(
                                        isLoading = isLoadingState,
                                        errorMessage = errorMessageState,
                                        onGoogleSignIn = { signInWithGoogle() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignIn: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // ✅ Logo o imagen superior
        Image(
            painter = painterResource(id = R.drawable.test),
            contentDescription = "Logo TragoListo",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // alto fijo
                .padding(bottom = 24.dp)
        )


        // ✅ Texto con nueva tipografía y peso
        //Text(
        //  text = "Bienvenido a TragoListo",
        //  fontSize = 28.sp,
        //  fontWeight = FontWeight.Bold,
        //  textAlign = TextAlign.Center,
        //  modifier = Modifier.padding(bottom = 16.dp)
        //)

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // ✅ Botón personalizado con ícono de Google
        Button(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Icon",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Iniciar sesión con Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TragoListoTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = null,
            onGoogleSignIn = {}
        )
    }
}