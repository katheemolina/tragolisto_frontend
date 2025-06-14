package com.example.tragolisto.auth

import android.content.Intent
import android.util.Log
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tragolisto.MainActivity
import com.example.tragolisto.R
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

class LoginScreen : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the sign-in launcher
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginScreen", "Google sign in failed", e)
                updateUIState(isLoading = false, errorMessage = "Error al iniciar sesión con Google: ${e.localizedMessage}")
            }
        }

        setContent {
            TragoListoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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

                    usuarioglobal = UserGlobal(
                        uid = uid,
                        email = email,
                        nombre = nombre
                    )

                    updateUIState(isLoading = false, errorMessage = null)
                    goToMain()
                } else {
                    Log.w("LoginScreen", "Sign-in failed", task.exception)
                    updateUIState(
                        isLoading = false,
                        errorMessage = "Error de autenticación: ${task.exception?.localizedMessage ?: "Error desconocido"}"
                    )
                }
            }
    }

    private fun updateUIState(isLoading: Boolean, errorMessage: String?) {
        // Update the UI state using Compose state
        setContent {
            TragoListoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreenContent(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onGoogleSignIn = { signInWithGoogle() }
                    )
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a TragoListo",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión con Google")
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
