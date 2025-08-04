package com.example.tragolisto.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.example.tragolisto.R
import androidx.core.content.ContextCompat.startActivity

@Composable
fun HomeScreen(
    userName: String,
    esModoOffline: Boolean,
    onChatClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onPartyClick: () -> Unit,
    onRecipesClick: () -> Unit,
    onCreationsClick: () -> Unit
) {
    val context = LocalContext.current

    val welcomePart1 = stringResource(id = R.string.welcome_message_part1)
    val appNameItalic = stringResource(id = R.string.app_name_italic)
    val welcomePart2 = stringResource(id = R.string.welcome_message_part2)

    val chatTitle = stringResource(id = R.string.chat_with_ferni_title)
    val chatDescription = stringResource(id = R.string.chat_with_ferni_description)

    val recipesTitle = stringResource(id = R.string.recipes_title)
    val recipesDescription = stringResource(id = R.string.recipes_description)

    val partyTitle = stringResource(id = R.string.party_mode_title)
    val partyDescription = stringResource(id = R.string.party_mode_description)

    val favoritesTitle = stringResource(id = R.string.favorites_title)
    val favoritesDescription = stringResource(id = R.string.favorites_description)

    val creationsTitle = stringResource(id = R.string.creations_title)
    val creationsDescription = stringResource(id = R.string.creations_description)

    val responsibleTitle = stringResource(id = R.string.responsible_consumption_title)
    val responsibleDescription = stringResource(id = R.string.responsible_consumption_description)

    val callAaButton = stringResource(id = R.string.call_aa_button)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Text(
            text = buildAnnotatedString {
                append(welcomePart1)
                withStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFFEA28E)
                    )
                ) {
                    append(appNameItalic)
                }
                append(" $userName$welcomePart2")
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (!esModoOffline) {
            HomeCard(
                title = chatTitle,
                description = chatDescription,
                icon = painterResource(id = R.drawable.ic_ferni),
                onClick = onChatClick,
                backgroundColor = Color(0xFFEDF6FF)
            )
        }

        HomeCard(
            title = recipesTitle,
            description = recipesDescription,
            icon = painterResource(id = R.drawable.ic_recetas),
            onClick = onRecipesClick,
            backgroundColor = Color(0xFFE8F5E9)
        )

        HomeCard(
            title = partyTitle,
            description = partyDescription,
            icon = painterResource(id = R.drawable.ic_fiesta),
            onClick = onPartyClick,
            backgroundColor = Color(0xFFFFF8E1)
        )

        if (!esModoOffline) {
            HomeCard(
                title = favoritesTitle,
                description = favoritesDescription,
                icon = painterResource(id = R.drawable.ic_favoritos),
                onClick = onFavoritesClick,
                backgroundColor = Color(0xFFFFEBEE)
            )
        }

        HomeCard(
            title = creationsTitle,
            description = creationsDescription,
            icon = painterResource(id = R.drawable.ic_creaciones),
            onClick = onCreationsClick,
            backgroundColor = Color(0xFFF3E5F5)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Alcohol awareness section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = responsibleTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = responsibleDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:11111")
                        }
                        startActivity(context, intent, null)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(callAaButton)
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        color = backgroundColor,
        shadowElevation = 6.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
