package com.kybers.stream.presentation.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    // Estados de animación
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var statusVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Animación secuencial del logo y texto
        delay(200)
        logoVisible = true
        delay(400)
        textVisible = true
        delay(300)
        statusVisible = true
    }
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.NavigateToLogin -> {
                delay(500) // Pequeña pausa antes de navegar
                onNavigateToLogin()
            }
            is SplashUiState.NavigateToHome -> {
                delay(500)
                onNavigateToHome()
            }
            is SplashUiState.Loading -> {
                // Mantener en pantalla de carga
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .semantics { contentDescription = "Pantalla de inicio de KybersStream" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Logo adaptativo
            AnimatedVisibility(
                visible = logoVisible,
                enter = scaleIn(
                    animationSpec = tween(600),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 140.dp else 120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Como no tenemos el logo real, usamos un ícono placeholder
                    Icon(
                        painter = painterResource(android.R.drawable.ic_media_play),
                        contentDescription = "Logo de KybersStream",
                        modifier = Modifier.size(if (isTablet) 80.dp else 64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Texto principal
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KybersStream",
                        style = if (isTablet) {
                            MaterialTheme.typography.displaySmall
                        } else {
                            MaterialTheme.typography.headlineLarge
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "IPTV Player",
                        style = if (isTablet) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Estado de carga
            AnimatedVisibility(
                visible = statusVisible,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val currentState = uiState) {
                        is SplashUiState.Loading -> {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val loadingText = when {
                                currentState.isMigrating -> "Actualizando datos..."
                                currentState.isValidatingSession -> "Validando sesión..."
                                else -> "Iniciando aplicación..."
                            }
                            
                            Text(
                                text = loadingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            
                            // Badge de migración si aplica
                            if (currentState.isMigrating) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "Actualizando...",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        else -> {
                            // Estado de navegación - mostrar sin indicador
                        }
                    }
                }
            }
        }
        
        // Información de versión en la parte inferior
        AnimatedVisibility(
            visible = statusVisible,
            enter = fadeIn(animationSpec = tween(600)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "v2.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}