package com.kybers.stream.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Establecer PIN",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Ingresa un PIN de 4 dígitos para el control parental",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            error = null
                        }
                    },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                            error = null
                        }
                    },
                    label = { Text("Confirmar PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Arrangement.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            when {
                                pin.length != 4 -> error = "El PIN debe tener 4 dígitos"
                                pin != confirmPin -> error = "Los PINs no coinciden"
                                else -> {
                                    onConfirm(pin)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = pin.isNotEmpty() && confirmPin.isNotEmpty()
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Acerca de KybersStream",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Versión: 1.0.0",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "KybersStream es una aplicación de streaming IPTV moderna construida con Jetpack Compose y las mejores prácticas de desarrollo de Android.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Características:",
                    style = MaterialTheme.typography.titleSmall
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "• Soporte completo para Xtream Codes API",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Reproductor optimizado con ExoPlayer",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Interfaz moderna con Material 3",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Control parental y favoritos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Multi-perfil de usuario",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}