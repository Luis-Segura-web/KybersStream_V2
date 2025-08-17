package com.kybers.stream.presentation.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.*
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.viewmodels.FullscreenPlayerViewModel
import com.kybers.stream.presentation.viewmodels.PlaybackState
import com.kybers.stream.presentation.viewmodels.MediaInfo
import com.kybers.stream.presentation.viewmodels.Quality
import com.kybers.stream.presentation.viewmodels.Subtitle
import com.kybers.stream.presentation.viewmodels.PlaylistItem
import com.kybers.stream.presentation.viewmodels.PlayerSettings
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenPlayerScreen(
    contentId: String,
    contentType: ContentType,
    onNavigateBack: () -> Unit,
    viewModel: FullscreenPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    var showControls by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var showQualitySelector by remember { mutableStateOf(false) }
    var showSubtitles by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(false) }
    
    // Auto-hide controls after 5 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }
    
    // Handle back button
    BackHandler {
        if (showSettings || showQualitySelector || showSubtitles || showPlaylist) {
            showSettings = false
            showQualitySelector = false
            showSubtitles = false
            showPlaylist = false
        } else {
            onNavigateBack()
        }
    }
    
    LaunchedEffect(contentId) {
        viewModel.loadContent(contentId, contentType)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics { contentDescription = "Reproductor a pantalla completa con controles adaptativos" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        when {
            uiState.isLoading -> {
                PlayerLoadingState(
                    onNavigateBack = onNavigateBack
                )
            }
            
            uiState.error != null -> {
                PlayerErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.retryPlayback() },
                    onNavigateBack = onNavigateBack
                )
            }
            
            currentMedia != null -> {
                // Main player view
                PlayerVideoContent(
                    viewModel = viewModel,
                    currentMedia = currentMedia,
                    playbackState = playbackState
                )
                
                // Overlay controls
                PlayerOverlayControls(
                    visible = showControls,
                    currentMedia = currentMedia,
                    playbackState = playbackState,
                    isTablet = isTablet,
                    isLandscape = isLandscape,
                    onNavigateBack = onNavigateBack,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { position -> viewModel.seekTo(position) },
                    onShowSettings = { showSettings = true },
                    onShowQualitySelector = { showQualitySelector = true },
                    onShowSubtitles = { showSubtitles = true },
                    onShowPlaylist = { 
                        if (contentType == ContentType.LIVE_TV || contentType == ContentType.SERIES) {
                            showPlaylist = true 
                        }
                    },
                    onFullscreenToggle = { /* Already fullscreen */ },
                    contentType = contentType
                )
                
                // Side panels
                PlayerSidePanels(
                    showSettings = showSettings,
                    showQualitySelector = showQualitySelector,
                    showSubtitles = showSubtitles,
                    showPlaylist = showPlaylist,
                    availableQualities = uiState.availableQualities,
                    availableSubtitles = uiState.availableSubtitles,
                    playlistItems = uiState.playlistItems,
                    currentQuality = uiState.currentQuality,
                    currentSubtitle = uiState.currentSubtitle,
                    onCloseSettings = { showSettings = false },
                    onCloseQualitySelector = { showQualitySelector = false },
                    onCloseSubtitles = { showSubtitles = false },
                    onClosePlaylist = { showPlaylist = false },
                    onQualitySelected = { quality -> viewModel.selectQuality(quality) },
                    onSubtitleSelected = { subtitle -> viewModel.selectSubtitle(subtitle) },
                    onPlaylistItemSelected = { item -> viewModel.playItem(item) },
                    onPlayerSettingsChanged = { settings -> viewModel.updatePlayerSettings(settings) },
                    contentType = contentType,
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun PlayerVideoContent(
    viewModel: FullscreenPlayerViewModel,
    currentMedia: MediaInfo,
    playbackState: PlaybackState?
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = viewModel.getExoPlayer()
                useController = false // We'll use custom controls
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setKeepContentOnPlayerReset(true)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PlayerLoadingState(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            AdaptiveText(
                text = "Cargando contenido...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PlayerErrorState(
    error: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            AdaptiveText(
                text = "Error de reproducción",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            AdaptiveText(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Volver")
                }
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reintentar")
                }
            }
        }
        
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PlayerOverlayControls(
    visible: Boolean,
    currentMedia: MediaInfo,
    playbackState: PlaybackState?,
    isTablet: Boolean,
    isLandscape: Boolean,
    onNavigateBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onShowSettings: () -> Unit,
    onShowQualitySelector: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowPlaylist: () -> Unit,
    onFullscreenToggle: () -> Unit,
    contentType: ContentType
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "controlsAlpha"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            // Top controls
            PlayerTopControls(
                currentMedia = currentMedia,
                onNavigateBack = onNavigateBack,
                onShowSettings = onShowSettings,
                onShowQualitySelector = onShowQualitySelector,
                onShowSubtitles = onShowSubtitles,
                onShowPlaylist = if (contentType == ContentType.LIVE_TV || contentType == ContentType.SERIES) onShowPlaylist else null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .alpha(alpha)
            )
            
            // Center play/pause button
            PlayerCenterControls(
                playbackState = playbackState,
                onTogglePlayPause = onTogglePlayPause,
                onSeek = onSeek,
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(alpha)
            )
            
            // Bottom controls
            PlayerBottomControls(
                playbackState = playbackState,
                currentMedia = currentMedia,
                isTablet = isTablet,
                onSeek = onSeek,
                onTogglePlayPause = onTogglePlayPause,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(alpha)
            )
        }
    }
}

@Composable
fun PlayerTopControls(
    currentMedia: MediaInfo,
    onNavigateBack: () -> Unit,
    onShowSettings: () -> Unit,
    onShowQualitySelector: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowPlaylist: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                AdaptiveText(
                    text = currentMedia.title ?: "Reproduciendo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                currentMedia.subtitle?.let { subtitle ->
                    AdaptiveText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            onShowPlaylist?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = "Lista de reproducción",
                        tint = Color.White
                    )
                }
            }
            
            IconButton(onClick = onShowSubtitles) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = "Subtítulos",
                    tint = Color.White
                )
            }
            
            IconButton(onClick = onShowQualitySelector) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = "Calidad",
                    tint = Color.White
                )
            }
            
            IconButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PlayerCenterControls(
    playbackState: PlaybackState?,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewind 10s
        IconButton(
            onClick = { 
                playbackState?.currentPosition?.let { pos ->
                    onSeek((pos - 10000).coerceAtLeast(0))
                }
            },
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Retroceder 10 segundos",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Play/Pause
        IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier
                .size(72.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (playbackState?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playbackState?.isPlaying == true) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // Forward 10s
        IconButton(
            onClick = { 
                playbackState?.let { state ->
                    val newPos = (state.currentPosition + 10000).coerceAtMost(state.duration)
                    onSeek(newPos)
                }
            },
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Avanzar 10 segundos",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PlayerBottomControls(
    playbackState: PlaybackState?,
    currentMedia: MediaInfo,
    isTablet: Boolean,
    onSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        playbackState?.let { state ->
            // Progress bar
            PlayerProgressBar(
                currentPosition = state.currentPosition,
                duration = state.duration,
                bufferedPosition = state.bufferedPosition,
                onSeek = onSeek,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdaptiveText(
                    text = formatTime(state.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                
                AdaptiveText(
                    text = formatTime(state.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PlayerProgressBar(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
    val bufferedProgress = if (duration > 0) bufferedPosition.toFloat() / duration else 0f
    
    Box(
        modifier = modifier.height(4.dp)
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        // Buffered progress
        Box(
            modifier = Modifier
                .fillMaxWidth(bufferedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.5f))
        )
        
        // Current progress
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun PlayerSidePanels(
    showSettings: Boolean,
    showQualitySelector: Boolean,
    showSubtitles: Boolean,
    showPlaylist: Boolean,
    availableQualities: List<Quality>,
    availableSubtitles: List<Subtitle>,
    playlistItems: List<PlaylistItem>,
    currentQuality: Quality?,
    currentSubtitle: Subtitle?,
    onCloseSettings: () -> Unit,
    onCloseQualitySelector: () -> Unit,
    onCloseSubtitles: () -> Unit,
    onClosePlaylist: () -> Unit,
    onQualitySelected: (Quality) -> Unit,
    onSubtitleSelected: (Subtitle) -> Unit,
    onPlaylistItemSelected: (PlaylistItem) -> Unit,
    onPlayerSettingsChanged: (PlayerSettings) -> Unit,
    contentType: ContentType,
    isTablet: Boolean
) {
    // Settings panel
    AnimatedVisibility(
        visible = showSettings,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        PlayerSettingsPanel(
            onClose = onCloseSettings,
            onSettingsChanged = onPlayerSettingsChanged,
            isTablet = isTablet
        )
    }
    
    // Quality selector panel
    AnimatedVisibility(
        visible = showQualitySelector,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        QualitySelectorPanel(
            qualities = availableQualities,
            currentQuality = currentQuality,
            onQualitySelected = onQualitySelected,
            onClose = onCloseQualitySelector,
            isTablet = isTablet
        )
    }
    
    // Subtitles panel
    AnimatedVisibility(
        visible = showSubtitles,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        SubtitlesPanel(
            subtitles = availableSubtitles,
            currentSubtitle = currentSubtitle,
            onSubtitleSelected = onSubtitleSelected,
            onClose = onCloseSubtitles,
            isTablet = isTablet
        )
    }
    
    // Playlist panel
    AnimatedVisibility(
        visible = showPlaylist,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        PlaylistPanel(
            items = playlistItems,
            onItemSelected = onPlaylistItemSelected,
            onClose = onClosePlaylist,
            contentType = contentType,
            isTablet = isTablet
        )
    }
}

@Composable
fun PlayerSettingsPanel(
    onClose: () -> Unit,
    onSettingsChanged: (PlayerSettings) -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.4f else 1f)
            .fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings content here
            AdaptiveText(
                text = "Configuración del reproductor en desarrollo...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QualitySelectorPanel(
    qualities: List<Quality>,
    currentQuality: Quality?,
    onQualitySelected: (Quality) -> Unit,
    onClose: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.4f else 1f)
            .fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = "Calidad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(qualities) { quality ->
                    QualityItem(
                        quality = quality,
                        isSelected = quality == currentQuality,
                        onClick = { 
                            onQualitySelected(quality)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SubtitlesPanel(
    subtitles: List<Subtitle>,
    currentSubtitle: Subtitle?,
    onSubtitleSelected: (Subtitle) -> Unit,
    onClose: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.4f else 1f)
            .fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = "Subtítulos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(subtitles) { subtitle ->
                    SubtitleItem(
                        subtitle = subtitle,
                        isSelected = subtitle == currentSubtitle,
                        onClick = { 
                            onSubtitleSelected(subtitle)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistPanel(
    items: List<PlaylistItem>,
    onItemSelected: (PlaylistItem) -> Unit,
    onClose: () -> Unit,
    contentType: ContentType,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.5f else 1f)
            .fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = when (contentType) {
                        ContentType.LIVE_TV -> "Canales"
                        ContentType.SERIES -> "Episodios"
                        else -> "Lista"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(items) { item ->
                    PlaylistItemCard(
                        item = item,
                        onClick = { 
                            onItemSelected(item)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QualityItem(
    quality: Quality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AdaptiveText(
                    text = quality.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                quality.description?.let { desc ->
                    AdaptiveText(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SubtitleItem(
    subtitle: Subtitle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdaptiveText(
                text = subtitle.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PlaylistItemCard(
    item: PlaylistItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = item.title,
                modifier = Modifier
                    .size(60.dp, 45.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AdaptiveText(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                item.subtitle?.let { subtitle ->
                    AdaptiveText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Helper functions
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

// Data classes for player functionality
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1f
)

data class Quality(
    val id: String,
    val name: String,
    val description: String? = null,
    val bitrate: Int? = null
)

data class Subtitle(
    val id: String,
    val name: String,
    val language: String? = null
)

data class PlaylistItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnail: String? = null,
    val duration: Long? = null
)

data class PlayerSettings(
    val autoPlay: Boolean = true,
    val skipIntro: Boolean = false,
    val subtitleSize: Float = 1f,
    val playbackSpeed: Float = 1f
)