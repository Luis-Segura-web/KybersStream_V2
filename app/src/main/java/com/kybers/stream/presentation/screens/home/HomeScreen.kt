package com.kybers.stream.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.ContentItem
import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.domain.model.Movie
import com.kybers.stream.domain.model.Series
import com.kybers.stream.domain.model.TMDBFilteredContent
import com.kybers.stream.domain.model.EnrichedTMDBMovie
import com.kybers.stream.domain.model.EnrichedTMDBSeries
import com.kybers.stream.presentation.screens.movies.MoviesScreen
import com.kybers.stream.presentation.screens.series.SeriesScreen
import com.kybers.stream.presentation.screens.tv.TvScreen
import com.kybers.stream.presentation.components.discovery.ContentCarouselSection
import com.kybers.stream.presentation.components.discovery.LoadingCarousel
import com.kybers.stream.presentation.components.discovery.ErrorCarousel
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents
import kotlinx.coroutines.delay

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Inicio", Icons.Default.Home, "home"),
    TV("TV", Icons.Default.Tv, "tv"),
    MOVIES("Películas", Icons.Default.Movie, "movies"),
    SERIES("Series", Icons.Default.VideoLibrary, "series"),
    SETTINGS("Ajustes", Icons.Default.Settings, "settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMovieDetail: (String) -> Unit = {},
    onNavigateToSeriesDetail: (String) -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onNavigateToSeries: () -> Unit = {},
    onNavigateToTV: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.HOME) }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            if (selectedTab == BottomNavItem.HOME) {
                TopAppBar(
                    title = {
                        AdaptiveText(
                            text = "KybersStream",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar contenido"
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Configuración"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.semantics { 
                    contentDescription = "Navegación principal de la aplicación" 
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                BottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = null,
                                modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                            ) 
                        },
                        label = { 
                            AdaptiveText(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = selectedTab == item,
                        onClick = { 
                            selectedTab = item
                            if (item == BottomNavItem.SETTINGS) {
                                onNavigateToSettings()
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomNavItem.HOME -> {
                    HomeTabContent(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToMovieDetail = onNavigateToMovieDetail,
                        onNavigateToSeriesDetail = onNavigateToSeriesDetail
                    )
                }
                BottomNavItem.TV -> {
                    TvScreen()
                }
                BottomNavItem.MOVIES -> {
                    MoviesScreen(
                        onNavigateToMovieDetail = { movieId -> onNavigateToMovieDetail(movieId) }
                    )
                }
                BottomNavItem.SERIES -> {
                    SeriesScreen(
                        onNavigateToSeriesDetail = { seriesId -> onNavigateToSeriesDetail(seriesId) }
                    )
                }
                BottomNavItem.SETTINGS -> {
                    // El settings se navega externamente
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToMovieDetail: (String) -> Unit,
    onNavigateToSeriesDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val hapticFeedback = LocalHapticFeedback.current
    
    // Memoizar callbacks para evitar recomposiciones innecesarias
    val onMovieClick = remember(onNavigateToMovieDetail, hapticFeedback) {
        { movieId: String ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToMovieDetail(movieId)
        }
    }
    
    val onSeriesClick = remember(onNavigateToSeriesDetail, hapticFeedback) {
        { seriesId: String ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToSeriesDetail(seriesId)
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Pantalla principal con contenido recomendado" },
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 32.dp else 24.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = if (isTablet) 16.dp else 8.dp,
            bottom = if (isTablet) 32.dp else 24.dp
        )
    ) {
        // Hero Carousel - Banner principal con datos TMDB (solo películas de Xtream disponibles)
        item {
            AnimatedVisibility(
                visible = uiState.tmdbFilteredContent?.hasContent == true,
                enter = fadeIn(animationSpec = tween(300))
            ) {
                val heroItems = uiState.tmdbFilteredContent?.let { tmdbContent ->
                    // Solo usar películas populares y trending de TMDB que están disponibles en Xtream
                    val topMovies = (tmdbContent.popularMovies.take(3) + tmdbContent.trendingMovies.take(2))
                        .distinctBy { it.xtreamMovie.streamId }
                        .take(5)
                        .map { 
                            ContentItem.MovieItem(
                                id = it.xtreamMovie.streamId,
                                title = it.xtreamMovie.name.removeXtreamSuffix(), // Usar nombre de Xtream sin "(Xtream)"
                                posterUrl = it.tmdbData.posterPath ?: it.xtreamMovie.icon,
                                backdropUrl = it.tmdbData.backdropPath,
                                year = it.tmdbData.releaseDate?.take(4),
                                rating = String.format("%.1f", it.tmdbData.voteAverage),
                                genre = it.tmdbData.genres.firstOrNull()?.name,
                                quality = "HD",
                                duration = it.tmdbData.runtime?.let { runtime -> "${runtime}min" },
                                plot = it.tmdbData.overview ?: it.xtreamMovie.plot
                            )
                        }
                    topMovies
                } ?: emptyList()
                
                HeroCarousel(
                    items = heroItems,
                    isLoading = false,
                    onItemClick = { item -> 
                        when (item.contentType) {
                            ContentType.VOD -> onMovieClick(item.id)
                            ContentType.SERIES -> onSeriesClick(item.id)
                            ContentType.EPISODE -> onSeriesClick(item.id)
                            ContentType.LIVE_TV -> onSeriesClick(item.id)
                        }
                    },
                    onPlayClick = { item -> 
                        when (item.contentType) {
                            ContentType.VOD -> onMovieClick(item.id)
                            ContentType.SERIES -> onSeriesClick(item.id)
                            ContentType.EPISODE -> onSeriesClick(item.id)
                            ContentType.LIVE_TV -> onSeriesClick(item.id)
                        }
                    },
                    modifier = Modifier.padding(bottom = if (isTablet) 16.dp else 8.dp)
                )
            }
        }
        
        // Secciones de contenido TMDB
        // Continuar viendo (solo si hay progreso)
        if (uiState.continueWatching.isNotEmpty()) {
            item {
                ContinueWatchingSection(
                    items = uiState.continueWatching,
                    onItemClick = { item -> /* TODO: Handle PlaybackProgress click */ },
                    onPlayClick = { item -> /* TODO: Handle PlaybackProgress play */ },
                    isTablet = isTablet,
                    modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                )
            }
        }
                
                // Favoritos (solo si hay favoritos)
                if (uiState.favorites.isNotEmpty()) {
                    item {
                        FavoritesSection(
                            favorites = uiState.favorites,
                            onFavoriteClick = { item -> /* TODO: Handle FavoriteItem click */ },
                            onPlayClick = { item -> /* TODO: Handle FavoriteItem play */ },
                            isTablet = isTablet,
                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                        )
                    }
                }
                
                // Películas recientes (solo datos Xtream)
                if (uiState.recentMovies.isNotEmpty()) {
                    item {
                        RecentMoviesSection(
                            title = "Películas Recientes",
                            movies = uiState.recentMovies,
                            onMovieClick = { movie -> 
                                onMovieClick(movie.streamId)
                            },
                            isTablet = isTablet,
                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                        )
                    }
                }
                
                // Series recientes (solo datos Xtream)
                if (uiState.recentSeries.isNotEmpty()) {
                    item {
                        RecentSeriesSection(
                            title = "Series Recientes",
                            series = uiState.recentSeries,
                            onSeriesClick = { series -> 
                                onSeriesClick(series.seriesId)
                            },
                            isTablet = isTablet,
                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                        )
                    }
                }
                
                // ========== SECCIONES TMDB FILTRADAS ==========
                uiState.tmdbFilteredContent?.let { tmdbContent ->
                    // Películas populares de TMDB (filtradas por disponibilidad)
                    if (tmdbContent.popularMovies.isNotEmpty()) {
                        item {
                            TMDBMoviesSection(
                                title = "🔥 Películas Populares",
                                subtitle = "Lo más popular según TMDB",
                                movies = tmdbContent.popularMovies,
                                onMovieClick = { movie -> 
                                    onMovieClick(movie.xtreamMovie.streamId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                    
                    // Películas trending de TMDB
                    if (tmdbContent.trendingMovies.isNotEmpty()) {
                        item {
                            TMDBMoviesSection(
                                title = "📈 Películas en Tendencia",
                                subtitle = "Las más comentadas esta semana",
                                movies = tmdbContent.trendingMovies,
                                onMovieClick = { movie -> 
                                    onMovieClick(movie.xtreamMovie.streamId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                    
                    // Películas mejor calificadas de TMDB
                    if (tmdbContent.topRatedMovies.isNotEmpty()) {
                        item {
                            TMDBMoviesSection(
                                title = "⭐ Películas Mejor Calificadas",
                                subtitle = "Las mejores según críticos y usuarios",
                                movies = tmdbContent.topRatedMovies,
                                onMovieClick = { movie -> 
                                    onMovieClick(movie.xtreamMovie.streamId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                    
                    // Series populares de TMDB
                    if (tmdbContent.popularSeries.isNotEmpty()) {
                        item {
                            TMDBSeriesSection(
                                title = "🔥 Series Populares",
                                subtitle = "Las series más populares según TMDB",
                                series = tmdbContent.popularSeries,
                                onSeriesClick = { series -> 
                                    onSeriesClick(series.xtreamSeries.seriesId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                    
                    // Series trending de TMDB
                    if (tmdbContent.trendingSeries.isNotEmpty()) {
                        item {
                            TMDBSeriesSection(
                                title = "📈 Series en Tendencia",
                                subtitle = "Las más comentadas esta semana",
                                series = tmdbContent.trendingSeries,
                                onSeriesClick = { series -> 
                                    onSeriesClick(series.xtreamSeries.seriesId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                    
                    // Series mejor calificadas de TMDB
                    if (tmdbContent.topRatedSeries.isNotEmpty()) {
                        item {
                            TMDBSeriesSection(
                                title = "⭐ Series Mejor Calificadas",
                                subtitle = "Las mejores series según críticos y usuarios",
                                series = tmdbContent.topRatedSeries,
                                onSeriesClick = { series -> 
                                    onSeriesClick(series.xtreamSeries.seriesId)
                                },
                                isTablet = isTablet,
                                modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                            )
                        }
                    }
                }
                
                // Estado de carga para TMDB con mejor UX
                if (uiState.isLoadingTMDB) {
                    items(3) { index ->
                        SkeletonContentSection(
                            title = when (index) {
                                0 -> "🔥 Películas Populares"
                                1 -> "📈 Series en Tendencia" 
                                else -> "⭐ Mejor Calificados"
                            },
                            isTablet = isTablet,
                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                        )
                    }
                }
                
                // Error de TMDB con mejor UX
                uiState.tmdbError?.let { error ->
                    item {
                        ErrorStateCard(
                            title = "Error al cargar contenido TMDB",
                            message = error,
                            onRetry = { viewModel.refreshTMDBContent() },
                            isTablet = isTablet,
                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                        )
                    }
                }
        
        // Información adicional al final con mejor espaciado
        item {
            Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 32.dp))
        }
    }
}

// Nuevos componentes para HomeScreen moderna

@Composable
fun HeroCarousel(
    items: List<ContentItem>,
    isLoading: Boolean,
    onItemClick: (ContentItem) -> Unit,
    onPlayClick: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isTablet = configuration.screenWidthDp >= 600
    val heroHeight = if (isTablet) 320.dp else 220.dp
    val hapticFeedback = LocalHapticFeedback.current
    
    if (isLoading) {
        // Skeleton del hero carousel
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(heroHeight)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }
    
    if (items.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { items.size.coerceAtMost(5) })
    
    // Auto-scroll para el hero carousel con mejor UX
    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        
        while (true) {
            delay(6000) // Aumentar a 6 segundos para mejor UX
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                HeroCarouselItem(
                    item = items[page],
                    onItemClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onItemClick(items[page]) 
                    },
                    onPlayClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayClick(items[page]) 
                    },
                    isTablet = isTablet,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isTablet) 24.dp else 16.dp)
                )
            }
            
            // Indicadores de página
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pagerState.pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) 
                                    MaterialTheme.colorScheme.primary
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCarouselItem(
    item: ContentItem,
    onItemClick: () -> Unit,
    onPlayClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onItemClick,
        modifier = modifier
            .graphicsLayer {
                // Añadir sutil elevación con shadow
                shadowElevation = if (isTablet) 12.dp.toPx() else 8.dp.toPx()
            },
        shape = RoundedCornerShape(if (isTablet) 20.dp else 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTablet) 12.dp else 8.dp)
    ) {
        Box {
            // Imagen de fondo con backdrop o poster de TMDB
            if (!item.backdropUrl.isNullOrEmpty() || !item.posterUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.backdropUrl ?: item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Overlay con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Contenido superpuesto
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdaptiveText(
                    text = item.title, // Usar el título del item (nombre de Xtream sin "(Xtream)")
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                // Mostrar información adicional si está disponible
                val plotText = when (item) {
                    is ContentItem.MovieItem -> item.plot
                    is ContentItem.SeriesItem -> item.plot
                    else -> null
                }
                
                if (!plotText.isNullOrEmpty()) {
                    AdaptiveText(
                        text = plotText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 3
                    )
                } else {
                    AdaptiveText(
                        text = "Disfruta del mejor contenido de entretenimiento disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 3
                    )
                }
                
                // Metadata del contenido
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val itemYear = item.year
                    val itemGenre = item.genre
                    val itemRating = item.rating
                    
                    if (!itemYear.isNullOrEmpty()) {
                        Text(
                            text = itemYear,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    if (!itemGenre.isNullOrEmpty()) {
                        if (!itemYear.isNullOrEmpty()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = itemGenre,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    if (!itemRating.isNullOrEmpty()) {
                        if (!itemYear.isNullOrEmpty() || !itemGenre.isNullOrEmpty()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = itemRating,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(if (isTablet) 48.dp else 40.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver ahora",
                            style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onItemClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                        modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
                    ) {
                        Text(
                            text = "Más info",
                            style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonContentSection(
    title: String,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Título skeleton
        SkeletonComponents.SkeletonBox(
            modifier = Modifier
                .width(if (isTablet) 240.dp else 200.dp)
                .height(if (isTablet) 28.dp else 24.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cards skeleton
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
        ) {
            items(5) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .width(if (isTablet) 180.dp else 160.dp)
                        .height(if (isTablet) 270.dp else 240.dp)
                        .clip(RoundedCornerShape(if (isTablet) 16.dp else 12.dp))
                )
            }
        }
    }
}

@Composable
fun ErrorStateCard(
    title: String,
    message: String,
    onRetry: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(if (isTablet) 32.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 56.dp else 48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AdaptiveText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AdaptiveText(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AdaptiveText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AdaptiveText(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onActionClick) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    items: List<PlaybackProgress>,
    onItemClick: (PlaybackProgress) -> Unit,
    onPlayClick: (PlaybackProgress) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continuar viendo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = items,
                key = { item -> "continue_${item.contentId}" }
            ) { item ->
                ContinueWatchingCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: PlaybackProgress,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .width(if (isTablet) 200.dp else 160.dp)
            .height(if (isTablet) 140.dp else 120.dp)
            .clip(RoundedCornerShape(if (isTablet) 12.dp else 8.dp))
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        ),
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Box {
            // Placeholder para imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Barra de progreso
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { item.progressPercentage },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            // Información
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.contentId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favorites: List<FavoriteItem>,
    onFavoriteClick: (FavoriteItem) -> Unit,
    onPlayClick: (FavoriteItem) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Favoritos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = favorites,
                key = { favorite -> "favorite_${favorite.contentId}" }
            ) { favorite ->
                FavoriteCard(
                    favorite = favorite,
                    onClick = { onFavoriteClick(favorite) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: FavoriteItem,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .width(if (isTablet) 140.dp else 120.dp)
            .height(if (isTablet) 180.dp else 160.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(if (isTablet) 12.dp else 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        ),
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Column {
            // Imagen
            AsyncImage(
                model = favorite.imageUrl,
                contentDescription = favorite.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Información
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Tipo de contenido
                Text(
                    text = when (favorite.contentType) {
                        ContentType.LIVE_TV -> "TV"
                        ContentType.VOD -> "Película"
                        ContentType.SERIES -> "Serie"
                        ContentType.EPISODE -> "Episodio"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun RecentMoviesSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdaptiveText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { /* TODO: Navigate to movies screen */ }) {
                Text("Ver todo")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = movies.take(15),
                key = { movie -> "movie_${movie.streamId}" }
            ) { movie ->
                XtreamMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun RecentSeriesSection(
    title: String,
    series: List<Series>,
    onSeriesClick: (Series) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdaptiveText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { /* TODO: Navigate to series screen */ }) {
                Text("Ver todo")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = series.take(15),
                key = { serie -> "series_${serie.seriesId}" }
            ) { serie ->
                XtreamSeriesCard(
                    series = serie,
                    onClick = { onSeriesClick(serie) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun XtreamMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .width(if (isTablet) 180.dp else 160.dp)
            .height(if (isTablet) 270.dp else 240.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        )
    ) {
        Box {
            // Imagen del póster (solo placeholder ya que TMDB se carga on-demand)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!movie.icon.isNullOrEmpty()) {
                    AsyncImage(
                        model = movie.icon,
                        contentDescription = movie.name.removeXtreamSuffix(), // Sin "(Xtream)"
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Ver detalles",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Información básica
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = movie.name.removeXtreamSuffix(), // Usar nombre de Xtream sin "(Xtream)"
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!movie.rating.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = movie.rating,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XtreamSeriesCard(
    series: Series,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .width(if (isTablet) 180.dp else 160.dp)
            .height(if (isTablet) 270.dp else 240.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        )
    ) {
        Box {
            // Imagen del cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!series.cover.isNullOrEmpty()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.name.removeXtreamSuffix(), // Sin "(Xtream)"
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Ver detalles",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Información básica
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = series.name.removeXtreamSuffix(), // Usar nombre de Xtream sin "(Xtream)"
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!series.rating.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = series.rating,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        if (!series.releaseDate.isNullOrEmpty()) {
                            if (!series.rating.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = series.releaseDate.take(4),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== SECCIONES TMDB ==========

@Composable
fun TMDBMoviesSection(
    title: String,
    subtitle: String,
    movies: List<EnrichedTMDBMovie>,
    onMovieClick: (EnrichedTMDBMovie) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = movies.take(15),
                key = { movie -> "tmdb_movie_${movie.tmdbData.id}" }
            ) { movie ->
                TMDBMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun TMDBSeriesSection(
    title: String,
    subtitle: String,
    series: List<EnrichedTMDBSeries>,
    onSeriesClick: (EnrichedTMDBSeries) -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = rememberLazyListState())
        ) {
            items(
                items = series.take(15),
                key = { seriesItem -> "tmdb_series_${seriesItem.tmdbData.id}" }
            ) { seriesItem ->
                TMDBSeriesCard(
                    series = seriesItem,
                    onClick = { onSeriesClick(seriesItem) },
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun TMDBMovieCard(
    movie: EnrichedTMDBMovie,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .width(if (isTablet) 180.dp else 160.dp)
            .height(if (isTablet) 270.dp else 240.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de fondo - usar poster de TMDB o icono de Xtream como fallback
            AsyncImage(
                model = movie.tmdbData.posterPath ?: movie.xtreamMovie.icon,
                contentDescription = "Poster de ${movie.xtreamMovie.name.removeXtreamSuffix()}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradiente para mejorar la legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Información superpuesta - usar nombre de Xtream
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = movie.xtreamMovie.name.removeXtreamSuffix(), // Usar nombre de Xtream sin "(Xtream)"
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (movie.tmdbData.voteAverage > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", movie.tmdbData.voteAverage),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    movie.tmdbData.releaseDate?.let { releaseDate ->
                        if (movie.tmdbData.voteAverage > 0) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = releaseDate.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Badge de disponibilidad si viene de Xtream
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TMDBSeriesCard(
    series: EnrichedTMDBSeries,
    onClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .width(if (isTablet) 180.dp else 160.dp)
            .height(if (isTablet) 270.dp else 240.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTablet) 6.dp else 4.dp,
            pressedElevation = if (isTablet) 12.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de fondo - usar poster de TMDB o cover de Xtream como fallback
            AsyncImage(
                model = series.tmdbData.posterPath ?: series.xtreamSeries.cover,
                contentDescription = "Poster de ${series.xtreamSeries.name.removeXtreamSuffix()}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradiente para mejorar la legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Información superpuesta - usar nombre de Xtream
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = series.xtreamSeries.name.removeXtreamSuffix(), // Usar nombre de Xtream sin "(Xtream)"
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (series.tmdbData.voteAverage > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", series.tmdbData.voteAverage),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    series.tmdbData.firstAirDate?.let { firstAirDate ->
                        if (series.tmdbData.voteAverage > 0) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = firstAirDate.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Badge de disponibilidad si viene de Xtream
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getServiceNamesString(services: List<String>): String {
    return services.joinToString(", ") { service ->
        when (service.lowercase()) {
            "netflix" -> "Netflix"
            "disney+" -> "Disney+"
            "hbo max" -> "HBO Max"
            "amazon prime video" -> "Prime Video"
            "hulu" -> "Hulu"
            "apple tv+" -> "Apple TV+"
            "paramount+" -> "Paramount+"
            "peacock" -> "Peacock"
            "showtime" -> "Showtime"
            "starz" -> "Starz"
            else -> service
        }
    }
}

// Función para remover "(Xtream)" y variaciones del sufijo de los nombres
private fun String.removeXtreamSuffix(): String {
    return this.replace(Regex("\\s*\\(Xtream\\)\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*\\[Xtream\\]\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*-\\s*Xtream\\s*$", RegexOption.IGNORE_CASE), "")
        .trim()
}
