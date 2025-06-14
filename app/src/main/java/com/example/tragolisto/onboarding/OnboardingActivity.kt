// Si el archivo está en app/src/main/java/com/example/tragolisto/onboarding/OnboardingActivity.kt
package com.example.tragolisto.onboarding // ESTO ES CLAVE

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tragolisto.ui.theme.TragoListoTheme // Asegúrate de que esta importación sea correcta para tu tema

class OnboardingActivity : ComponentActivity() { // ASEGÚRATE DE QUE SE LLAMA OnboardingActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge() // Si no lo necesitas, puedes comentarlo o quitarlo
        setContent {
            TragoListoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OnboardingScreenContent()
                }
            }
        }
    }
}

@Composable
fun OnboardingScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido! Completa tu perfil.",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Esta es tu pantalla de onboarding.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    TragoListoTheme {
        OnboardingScreenContent()
    }
}