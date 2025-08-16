package com.kybers.stream.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.kybers.stream.domain.manager.PlaybackManager
import com.kybers.stream.domain.model.*

@Composable
fun VideoPlayer(
    mediaInfo: MediaInfo?,
    playbackManager: PlaybackManager,
    modifier: Modifier = Modifier,
    showControls: Boolean = true
) {
    val context = LocalContext.current
    val playbackState by playbackManager.playbackState.collectAsStateWithLifecycle()
    val playbackPosition by playbackManager.playbackPosition.collectAsStateWithLifecycle()
    
    var isControlsVisible by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (mediaInfo != null) {
            // Reproductor Media3
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playbackManager.exoPlayer
                        useController = showControls
                        controllerShowTimeoutMs = 3000
                        setShowRewindButton(false)
                        setShowFastForwardButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay de estado de error
            val currentState = playbackState
            if (currentState is PlaybackState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error de reproducción",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Overlay de buffering
            if (playbackState is PlaybackState.Buffering) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

        } else {
            // Placeholder cuando no hay contenido
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Sin contenido",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecciona contenido para reproducir",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPlayerControls(
    playbackState: PlaybackState,
    playbackPosition: PlaybackPosition,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Barra de progreso
            Slider(
                value = playbackPosition.progressPercentage,
                onValueChange = { progress ->
                    val newPosition = (progress * playbackPosition.duration).toLong()
                    onSeek(newPosition)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiempo actual
                Text(
                    text = formatTime(playbackPosition.currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                
                // Botón play/pause
                IconButton(
                    onClick = onPlayPause
                ) {
                    Icon(
                        imageVector = if (playbackState is PlaybackState.Playing) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = if (playbackState is PlaybackState.Playing) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Duración total
                Text(
                    text = formatTime(playbackPosition.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}