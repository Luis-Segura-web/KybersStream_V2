package com.kybers.stream.presentation.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SyncDataDialog(
    syncUiState: SyncUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (syncUiState.isVisible) {
        Dialog(
            onDismissRequest = { 
                if (syncUiState.currentStep == SyncStep.ERROR) {
                    onCancel()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = syncUiState.currentStep == SyncStep.ERROR,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono según el estado
                    val icon = when (syncUiState.currentStep) {
                        SyncStep.ERROR -> Icons.Default.Error
                        SyncStep.COMPLETED -> Icons.Default.CheckCircle
                        else -> Icons.Default.Sync
                    }
                    
                    val iconColor = when (syncUiState.currentStep) {
                        SyncStep.ERROR -> MaterialTheme.colorScheme.error
                        SyncStep.COMPLETED -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Título
                    Text(
                        text = when (syncUiState.currentStep) {
                            SyncStep.ERROR -> "Error de Sincronización"
                            SyncStep.COMPLETED -> "Sincronización Completada"
                            else -> "Cargando datos..."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Descripción del paso actual
                    if (syncUiState.errorMessage != null) {
                        Text(
                            text = syncUiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = syncUiState.progressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Barra de progreso
                    if (syncUiState.currentStep != SyncStep.ERROR && syncUiState.currentStep != SyncStep.COMPLETED) {
                        Column {
                            LinearProgressIndicator(
                                progress = { syncUiState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${(syncUiState.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Botones de acción para errores
                    if (syncUiState.currentStep == SyncStep.ERROR) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }
                            
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                    
                    // Indicador de pasos
                    if (syncUiState.currentStep != SyncStep.ERROR) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(syncUiState.totalSteps) { index ->
                                val stepCompleted = index < syncUiState.currentStep.stepNumber
                                val isCurrentStep = index == syncUiState.currentStep.stepNumber
                                
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when {
                                                stepCompleted -> MaterialTheme.colorScheme.primary
                                                isCurrentStep -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(50)
                                        )
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
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}
