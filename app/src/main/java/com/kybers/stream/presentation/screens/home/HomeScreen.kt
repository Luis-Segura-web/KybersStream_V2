package com.kybers.stream.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.presentation.screens.movies.MoviesScreen
import com.kybers.stream.presentation.screens.series.SeriesScreen
import com.kybers.stream.presentation.screens.tv.TvScreen
import com.kybers.stream.presentation.components.discovery.ContentCarouselSection
import com.kybers.stream.presentation.components.discovery.LoadingCarousel
import com.kybers.stream.presentation.components.discovery.ErrorCarousel

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Inicio", Icons.Default.Home, "home"),
    TV("TV", Icons.Default.Tv, "tv"),
    MOVIES("Películas", Icons.Default.Movie, "movies"),
    SERIES("Series", Icons.Default.VideoLibrary, "series")
}

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == item,
                        onClick = { selectedTab = item }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomNavItem.HOME -> {
                    HomeTabContent(onNavigateToLogin = onNavigateToLogin)
                }
                BottomNavItem.TV -> {
                    TvScreen()
                }
                BottomNavItem.MOVIES -> {
                    MoviesScreen()
                }
                BottomNavItem.SERIES -> {
                    SeriesScreen()
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val discoveryData by viewModel.discoveryData.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Header con título y botón de actualizar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inicio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { viewModel.refreshDiscovery() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }
        }
        
        // Mostrar carruseles de descubrimiento
        when {
            discoveryData.isLoading -> {
                items(3) { index ->
                    LoadingCarousel(
                        title = when (index) {
                            0 -> "Continuar viendo"
                            1 -> "Recomendado para ti"
                            else -> "Recientes"
                        }
                    )
                }
            }
            
            discoveryData.error != null -> {
                item {
                    ErrorCarousel(
                        title = "Error al cargar contenido",
                        error = discoveryData.error!!,
                        onRetry = { viewModel.refreshDiscovery() }
                    )
                }
            }
            
            discoveryData.hasContent -> {
                discoveryData.sections.forEach { section ->
                    section.carousels.forEach { carousel ->
                        item(key = carousel.id) {
                            ContentCarouselSection(
                                carousel = carousel,
                                onItemClick = { item ->
                                    viewModel.onContentItemClick(item)
                                },
                                onPlayClick = { item ->
                                    viewModel.onContentPlayClick(item)
                                },
                                onMoreClick = {
                                    // TODO: Navigate to full category view
                                }
                            )
                        }
                    }
                }
            }
            
            else -> {
                item {
                    // Estado vacío
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "No hay contenido disponible",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        
                        Text(
                            text = "Agrega contenido a favoritos o comienza a ver algo para ver recomendaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Botón de cerrar sesión al final
        item {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    items: List<PlaybackProgress>,
    onItemClick: (PlaybackProgress) -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(items) { item ->
                ContinueWatchingCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: PlaybackProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(favorites) { favorite ->
                FavoriteCard(
                    favorite = favorite,
                    onClick = { onFavoriteClick(favorite) }
                )
            }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: FavoriteItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
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