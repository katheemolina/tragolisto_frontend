package com.example.tragolisto.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tragolisto.R
import com.example.tragolisto.data.model.Trago
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TragoDialog(
    trago: Trago,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trago.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trago.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Chips de info
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(stringResource(R.string.difficulty), trago.dificultad)
                    InfoChip(stringResource(R.string.time), stringResource(R.string.minutes_format, trago.tiempoPreparacionMinutos))
                    InfoChip(stringResource(R.string.alcohol), stringResource(if (trago.esAlcoholico) R.string.yes else R.string.no))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Ingredientes
                SectionTitle(stringResource(R.string.ingredients))
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    trago.ingredientes?.forEach { ingrediente ->
                        Text(
                            text = stringResource(
                                R.string.ingredient_line_format,
                                ingrediente.nombre,
                                ingrediente.pivot.cantidad,
                                ingrediente.pivot.unidad
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(stringResource(R.string.instructions))
                Spacer(modifier = Modifier.height(8.dp))

                trago.instrucciones
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .forEachIndexed { index, paso ->
                        Text(
                            text = "${index + 1}. $paso",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                if (trago.tips.isNotBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle(stringResource(R.string.tips))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trago.tips,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (trago.historia.isNotBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle(stringResource(R.string.history))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trago.historia,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    AssistChip(
        onClick = {},
        label = {
            Text("$label: $value")
        },
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    )
}
