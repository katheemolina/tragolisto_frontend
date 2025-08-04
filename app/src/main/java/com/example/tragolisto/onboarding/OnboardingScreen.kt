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
import androidx.compose.ui.res.stringResource
import com.example.tragolisto.R
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import java.time.Period

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
    esModoInvitado: Boolean,
    onFinish: () -> Unit
) {
    val error_no_token = stringResource(id = R.string.error_no_token)
    val error_token_failed = stringResource(id = R.string.error_token_failed)

    var currentPage by remember { mutableStateOf(0) }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = birthDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
            ?: System.currentTimeMillis()
    )
    var isSavingBirthDate by remember { mutableStateOf(false) }
    var errorMessage: String? by remember { mutableStateOf(null) }

    val auth = FirebaseAuth.getInstance()

    val pages = listOf(
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_ferni),
            description = stringResource(id = R.string.onboarding_desc_ferni),
            emoji = "🍸"
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_chat),
            description = stringResource(id = R.string.onboarding_desc_chat),
            emoji = "💬"
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_favorites),
            description = stringResource(id = R.string.onboarding_desc_favorites),
            emoji = "⭐"
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_party),
            description = stringResource(id = R.string.onboarding_desc_party),
            emoji = "🎉"
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_recipes),
            description = stringResource(id = R.string.onboarding_desc_recipes),
            emoji = "📖"
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_learn),
            description = stringResource(id = R.string.onboarding_desc_learn),
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
                                contentDescription = stringResource(id = R.string.cd_previous),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

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
                                contentDescription = stringResource(id = R.string.cd_next),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (esModoInvitado) {
                                    onFinish()
                                } else {
                                    currentPage++
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                stringResource(id = R.string.onboarding_lets_start),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                DotIndicator(
                    totalDots = pages.size,
                    selectedPage = currentPage,
                    modifier = Modifier.padding(top = 48.dp)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.onboarding_hello, usuarioglobal?.nombre ?: "default"),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = stringResource(id = R.string.onboarding_birthdate_prompt),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )

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
                            ?: stringResource(id = R.string.onboarding_select_birthdate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (esModoInvitado) {
                            onFinish()
                        } else if (birthDate != null && !isSavingBirthDate) {
                            isSavingBirthDate = true
                            errorMessage = null

                            val edad = Period.between(birthDate, LocalDate.now()).years
                            usuarioglobal?.esMayor = edad >= 18

                            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val idToken = usuarioglobal?.idToken
                                    if (idToken != null) {
                                        ClientApi.completarOnboarding(idToken, birthDate!!) { success, message ->
                                            isSavingBirthDate = false
                                            if (success) {
                                                onFinish()
                                            } else {
                                                errorMessage = "Error: $message"
                                            }
                                        }
                                    } else {
                                        isSavingBirthDate = false
                                        errorMessage = error_no_token
                                    }
                                } else {
                                    isSavingBirthDate = false
                                    errorMessage = error_token_failed
                                }
                            }
                        }
                    },
                    enabled = esModoInvitado || (birthDate != null && !isSavingBirthDate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavingBirthDate && !esModoInvitado) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(id = R.string.onboarding_finish_button))
                    }
                }

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
                                    }
                                }
                            ) {
                                Text(
                                    stringResource(id = R.string.onboarding_datepicker_ok),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(
                                    stringResource(id = R.string.onboarding_datepicker_cancel),
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
                                    stringResource(id = R.string.onboarding_datepicker_title),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            headline = {
                                Text(
                                    stringResource(id = R.string.onboarding_datepicker_headline),
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
