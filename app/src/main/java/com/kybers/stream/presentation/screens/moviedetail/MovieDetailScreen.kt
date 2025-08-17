package com.kybers.stream.presentation.screens.moviedetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import com.kybers.stream.domain.model.MovieDetail
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    onNavigateBack: () -> Unit,
    onPlay: (MovieDetail) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetail(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Pantalla de detalle de película con reproductor integrado" }
    ) {
        when {
            uiState.isLoading -> {
                MovieDetailLoadingState(
                    isTablet = isTablet,
                    onNavigateBack = onNavigateBack
                )
            }
            
            uiState.error != null -> {
                MovieDetailErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadMovieDetail(movieId) },
                    onNavigateBack = onNavigateBack
                )
            }
            
            uiState.movieDetail != null -> {
                MovieDetailContent(
                    movie = uiState.movieDetail!!,
                    playbackProgress = playbackProgress,
                    playbackState = playbackState,
                    currentMedia = currentMedia,
                    isFavorite = isFavorite,
                    isTablet = isTablet,
                    onPlay = onPlay,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onNavigateBack = onNavigateBack,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun MovieDetailLoadingState(
    isTablet: Boolean,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header skeleton con botón de navegación
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 400.dp else 280.dp)
        ) {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier.fillMaxSize()
            )
            
            // Botón de volver funcional
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
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
        
        // Content skeleton
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(32.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    SkeletonComponents.SkeletonBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }
            
            repeat(5) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
            }
        }
    }
}

@Composable
fun MovieDetailErrorState(
    error: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Botón de volver
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver"
            )
        }
        
        // Estado de error centrado
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AdaptiveText(
                text = "Error al cargar película",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AdaptiveText(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onRetry) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailContent(
    movie: MovieDetail,
    playbackProgress: PlaybackProgress?,
    playbackState: String?,
    currentMedia: com.kybers.stream.domain.model.MediaInfo?,
    isFavorite: Boolean,
    isTablet: Boolean,
    onPlay: (MovieDetail) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MovieDetailViewModel
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showPlayer by remember { mutableStateOf(false) }
    var isPlayerMinimized by remember { mutableStateOf(false) }
    
    // Animación para el parallax effect
    val parallaxOffset by animateFloatAsState(
        targetValue = scrollState.value * 0.5f,
        animationSpec = tween(300),
        label = "parallax"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero section con reproductor/backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 400.dp else 280.dp)
            ) {
                if (showPlayer && currentMedia != null) {
                    // Reproductor integrado
                    MoviePlayerSection(
                        viewModel = viewModel,
                        currentMedia = currentMedia,
                        playbackState = playbackState,
                        isMinimized = isPlayerMinimized,
                        onMinimize = { isPlayerMinimized = true },
                        onClose = { 
                            showPlayer = false
                            isPlayerMinimized = false
                        }
                    )
                } else {
                    // Backdrop con parallax
                    AsyncImage(
                        model = movie.backdrop ?: movie.poster,
                        contentDescription = "Fondo de ${movie.name}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = parallaxOffset
                            },
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradiente elegante
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                }
                
                // Header con controles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
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
                    
                    if (!showPlayer) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (isFavorite) Color.Red else Color.White
                                )
                            }
                            
                            IconButton(
                                onClick = { /* TODO: Share functionality */ },
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Play button central (solo si no está reproduciéndose)
                if (!showPlayer) {
                    FloatingActionButton(
                        onClick = { 
                            showPlayer = true
                            onPlay(movie)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (playbackProgress != null) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                            contentDescription = if (playbackProgress != null) "Continuar" else "Reproducir",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        
            // Información principal
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Título con animación
                AdaptiveText(
                    text = movie.name,
                    style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(
                        1f - (scrollState.value * 0.001f).coerceAtMost(0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Metadatos con chips mejorados
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    movie.year?.let { year ->
                        item {
                            EnhancedChip(
                                label = year,
                                icon = Icons.Default.DateRange
                            )
                        }
                    }
                    
                    movie.durationDisplay?.let { duration ->
                        item {
                            EnhancedChip(
                                label = duration,
                                icon = Icons.Default.Schedule
                            )
                        }
                    }
                    
                    movie.quality?.let { quality ->
                        item {
                            EnhancedChip(
                                label = quality,
                                icon = Icons.Default.HighQuality,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    movie.genre?.let { genre ->
                        item {
                            EnhancedChip(
                                label = genre,
                                icon = Icons.Default.Category
                            )
                        }
                    }
                }
                
                // Rating mejorado
                movie.ratingDisplay?.let { rating ->
                    Card(
                        modifier = Modifier.padding(top = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AdaptiveText(
                                text = "Rating: $rating",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            
                // Botones de acción mejorados
                if (!showPlayer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón principal de reproducción
                        Button(
                            onClick = { 
                                showPlayer = true
                                onPlay(movie)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (playbackProgress != null) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AdaptiveText(
                                text = if (playbackProgress != null) "Continuar" else "Reproducir",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        
                        // Botón de descarga (placeholder)
                        OutlinedButton(
                            onClick = { /* TODO: Download functionality */ },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar"
                            )
                        }
                        
                        // Botón de añadir a lista (placeholder)
                        OutlinedButton(
                            onClick = { /* TODO: Add to list functionality */ },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir a lista"
                            )
                        }
                    }
                }
            
                // Barra de progreso mejorada
                playbackProgress?.let { progress ->
                    Card(
                        modifier = Modifier.padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AdaptiveText(
                                        text = "Continuar viendo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                AdaptiveText(
                                    text = "${(progress.progressPercentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = { progress.progressPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            
                // Sinopsis expandible
                movie.plot?.let { plot ->
                    var isExpanded by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.padding(top = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AdaptiveText(
                                text = "Sinopsis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            AdaptiveText(
                                text = plot,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                            )
                            
                            if (plot.length > 150) {
                                TextButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = if (isExpanded) "Ver menos" else "Ver más",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            
                // Información adicional en tarjetas
                if (movie.hasExtendedInfo) {
                    Card(
                        modifier = Modifier.padding(top = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AdaptiveText(
                                text = "Información",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            movie.director?.let { director ->
                                EnhancedInfoRow(
                                    icon = Icons.Default.Person, 
                                    label = "Director", 
                                    value = director
                                )
                            }
                            
                            movie.cast?.let { cast ->
                                EnhancedInfoRow(
                                    icon = Icons.Default.People,
                                    label = "Reparto", 
                                    value = cast,
                                    maxLines = 3
                                )
                            }
                            
                            movie.language?.let { language ->
                                EnhancedInfoRow(
                                    icon = Icons.Default.Language,
                                    label = "Idioma", 
                                    value = language
                                )
                            }
                            
                            movie.country?.let { country ->
                                EnhancedInfoRow(
                                    icon = Icons.Default.Public,
                                    label = "País", 
                                    value = country
                                )
                            }
                            
                            movie.releaseDate?.let { date ->
                                EnhancedInfoRow(
                                    icon = Icons.Default.Event,
                                    label = "Fecha de estreno", 
                                    value = date
                                )
                            }
                        }
                    }
                }
                
                // Espaciado final
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Player minimizado flotante
        if (showPlayer && isPlayerMinimized) {
            MinimizedPlayerOverlay(
                movie = movie,
                onExpand = { isPlayerMinimized = false },
                onClose = { 
                    showPlayer = false
                    isPlayerMinimized = false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun MoviePlayerSection(
    viewModel: MovieDetailViewModel,
    currentMedia: com.kybers.stream.domain.model.MediaInfo,
    playbackState: String?,
    isMinimized: Boolean,
    onMinimize: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Reproductor Media3
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.getExoPlayer()
                        useController = true
                        controllerShowTimeoutMs = 3000
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Controles superiores
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Minimize,
                        contentDescription = "Minimizar",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MinimizedPlayerOverlay(
    movie: MovieDetail,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onExpand,
        modifier = modifier
            .width(200.dp)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            // Miniatura
            AsyncImage(
                model = movie.poster,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            
            // Título
            Text(
                text = movie.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
            
            // Botón cerrar
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
            }
            AdaptiveText(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun EnhancedInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AdaptiveText(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            AdaptiveText(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Mantener componente legacy
@Composable
fun Chip(
    label: String,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    modifier: Modifier = Modifier
) {
    EnhancedChip(
        label = label,
        color = color,
        modifier = modifier
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}