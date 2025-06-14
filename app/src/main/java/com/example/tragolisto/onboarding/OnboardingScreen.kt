package com.example.tragolisto.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.tragolisto.ui.theme.*
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.data.api.ClientApi
import android.util.Log
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String? = null
)

@Composable
fun DotIndicator(
    totalDots: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(totalDots) { page ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (page == selectedPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = birthDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
            ?: System.currentTimeMillis()
    )
    var isSavingBirthDate by remember { mutableStateOf(false) }
    var errorMessage: String? by remember { mutableStateOf(null) } // To display error messages to the user

    val auth = FirebaseAuth.getInstance() // Get FirebaseAuth instance

    val pages = listOf(
        OnboardingPage(
            title = "Hola soy Ferni",
            description = "Tu bartender virtual que te ayudara a preparar deliciosos tragos con lo que tenes en casa.",
            emoji = "🍸"
        ),
        OnboardingPage(
            title = "Chatea con Ferni",
            description = "Contame que ingredientes tenes disponibles y tus preferencias. Te recomendare los mejores tragos que podes preparar sin salir a comprar nada extra",
            emoji = "💬"
        ),
        OnboardingPage(
            title = "Guarda tus favoritos",
            description = "Marca tus recetas preferidas para acceder rapidamente a ellas cuando quieras",
            emoji = "⭐"
        ),
        OnboardingPage(
            title = "Modo fiesta",
            description = "¿Reunion con amigos? Juegos para que las reuniones sean mas divertidas",
            emoji = "🎉"
        ),
        OnboardingPage(
            title = "Explora recetas",
            description = "Descubri nuestra coleccion de recetas clasicas y creativas, con y sin alcohol",
            emoji = "📖"
        ),
        OnboardingPage(
            title = "Aprende mientras disfrutas",
            description = "Cada receta incluye instrucciones paso a paso, tips y datos curiosos sobre la historia y origen de cada trago",
            emoji = "ℹ️"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentPage < pages.size) {
                // Regular onboarding pages
                val page = pages[currentPage]

                page.emoji?.let { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 64.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Navigation arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        IconButton(
                            onClick = { currentPage-- },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Anterior",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Next / Let's Start Button
                    if (currentPage < pages.size - 1) {
                        IconButton(
                            onClick = { currentPage++ },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Siguiente",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // Last intro page, button to move to the date of birth collection page
                        Button(
                            onClick = { currentPage++ }, // Advance to the data collection page
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                "¿Comenzamos?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                // Dot indicators
                DotIndicator(
                    totalDots = pages.size,
                    selectedPage = currentPage,
                    modifier = Modifier.padding(top = 48.dp)
                )
            } else {
                // User info collection page (after all intro pages)
                Text(
                    text = "Hola " + (usuarioglobal?.nombre ?: "default"),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Antes de comenzar, necesitamos tu fecha de nacimiento",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Date picker button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = birthDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            ?: "Seleccionar fecha de nacimiento",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Button to finalize onboarding
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (birthDate != null && !isSavingBirthDate) {
                            isSavingBirthDate = true
                            errorMessage = null // Clear previous error messages

                                                        // Get the current user's ID Token from Firebase
                            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val idToken = usuarioglobal?.idToken
                                    if (idToken != null) {
                                        // Call the new `completarOnboarding` function in ClientApi
                                        Log.d("OnboardingScreen", "ID Token a enviar: ${idToken.take(20)}... (truncado)")
                                        Log.d("OnboardingScreen", "Fecha de nacimiento a enviar: ${birthDate!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")

                                        ClientApi.completarOnboarding(idToken, birthDate!!) { success, message ->
                                            isSavingBirthDate = false
                                            if (success) {
                                                Log.d("OnboardingScreen", "Onboarding completed successfully: $message")
                                                onFinish() // Call onFinish to navigate to Home
                                            } else {
                                                Log.e("OnboardingScreen", "Error completing onboarding: $message")
                                                errorMessage = "Error: $message" // Display error in UI
                                            }
                                        }
                                    } else {
                                        isSavingBirthDate = false
                                        errorMessage = "Error: Couldn't get user ID Token."
                                        Log.e("OnboardingScreen", "ID Token is null.")
                                    }
                                } else {
                                    isSavingBirthDate = false
                                    errorMessage = "Error getting ID Token: ${task.exception?.localizedMessage}"
                                    Log.e("OnboardingScreen", "Failed to get ID Token: ${task.exception}")
                                }
                            }
                        }
                    },
                    enabled = birthDate != null && !isSavingBirthDate, // Enabled if date selected and not saving
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavingBirthDate) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("¡Listo, comencemos!")
                    }
                }

                // Display error message if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        birthDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                        showDatePicker = false
                                        // NOTE: Do not call onFinish here, only when the "Let's start!" button is pressed
                                    }
                                }
                            ) {
                                Text(
                                    "OK",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDatePicker = false }
                            ) {
                                Text(
                                    "Cancelar",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            title = {
                                Text(
                                    "Selecciona tu fecha de nacimiento",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            headline = {
                                Text(
                                    "Fecha de nacimiento",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            showModeToggle = false
                        )
                    }
                }
            }
        }
    }
}