package com.kybers.stream.presentation.screens.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.Channel
import com.kybers.stream.presentation.components.epg.EpgNowNextDisplay
import com.kybers.stream.presentation.components.epg.EpgLoadingPlaceholder
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents
import com.kybers.stream.presentation.components.ViewMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvScreen(
    viewModel: TvViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val isTablet = remember(windowInfo.containerSize) { with(density){ windowInfo.containerSize.width.toDp() >= 600.dp } }

    var searchQuery by remember { mutableStateOf("") }
    // Eliminado selectedCategory / dropdown; se usa expandedCategoryId en viewModel
    val expandedCategoryId = uiState.expandedCategoryId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .consumeWindowInsets(PaddingValues(bottom = 0.dp)) // Consume solo los insets que el padre ya maneja
            .semantics { contentDescription = "Pantalla de TV en vivo con reproductor y lista de canales" }
    ) {
        // Reproductor en la parte superior con ratio 16:9
        PlayerSection(
            viewModel = viewModel,
            currentMedia = currentMedia,
            playbackState = playbackState,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 250.dp else 200.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Barra de búsqueda y filtros
        LegacySearchBarSection(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it; viewModel.search(it) },
            onClear = {
                searchQuery = ""
                viewModel.search("")
            },
            isSearching = uiState.isSearching,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )


        // Lista de canales
        when {
            uiState.isLoading -> {
                ChannelsLoadingState(
                    viewMode = uiState.viewMode,
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.error != null -> {
                ChannelsErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadChannels() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.filteredChannels.isEmpty() && searchQuery.isNotEmpty() -> {
                ChannelsEmptyState(
                    searchQuery = searchQuery,
                    selectedCategory = "",
                    onClearFilters = {
                        searchQuery = ""
                        viewModel.clearFilters()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                CategoriesAccordion(
                    uiState = uiState,
                    onToggleCategory = { viewModel.toggleCategory(it) },
                    onChannelClick = { viewModel.playChannel(it) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onRefreshEpg = { viewModel.refreshEpg() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerSection(
    viewModel: TvViewModel,
    currentMedia: com.kybers.stream.domain.model.MediaInfo?,
    playbackState: com.kybers.stream.domain.model.PlaybackState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (currentMedia != null) {
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
                
                // Información del canal superpuesta
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        AdaptiveText(
                            text = currentMedia.title ?: "Canal en vivo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (playbackState != null) {
                            val playbackText = when (playbackState) {
                                is com.kybers.stream.domain.model.PlaybackState.Playing -> "Reproduciendo"
                                is com.kybers.stream.domain.model.PlaybackState.Paused -> "Pausado"
                                is com.kybers.stream.domain.model.PlaybackState.Buffering -> "Cargando..."
                                is com.kybers.stream.domain.model.PlaybackState.Idle -> "Detenido"
                                is com.kybers.stream.domain.model.PlaybackState.Error -> "Error"
                            }
                            AdaptiveText(
                                text = playbackText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                // Placeholder elegante cuando no hay reproducción
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        
                        AdaptiveText(
                            text = "Selecciona un canal para reproducir",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        AdaptiveText(
                            text = "Explora la lista de canales disponibles",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegacySearchBarSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("Buscar canales...") },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoriesAccordion(
    uiState: TvUiState,
    onToggleCategory: (String) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit,
    onRefreshEpg: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = uiState.categories
    val expandedId = uiState.expandedCategoryId
    val channels = uiState.filteredChannels
    val channelsByCategory = remember(channels) {
        channels.groupBy { it.categoryId ?: "" }
    }

    // Construir lista ordenada con expanded primero (stickyHeader) y resto después
    val expandedCategory = categories.firstOrNull { it.categoryId == expandedId }
    val otherCategories = categories.filter { it.categoryId != expandedId }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (expandedCategory != null) {
            stickyHeader {
                CategoryHeader(
                    category = expandedCategory,
                    isExpanded = true,
                    onClick = { onToggleCategory(expandedCategory.categoryId) }
                )
            }
            val catChannels = channelsByCategory[expandedCategory.categoryId] ?: emptyList()
            items(catChannels, key = { it.streamId }) { ch ->
                ChannelItem(
                    channel = ch,
                    channelEpg = uiState.channelsEpg[ch.streamId],
                    isLoadingEpg = uiState.isLoadingEpg,
                    isCurrentlyPlaying = uiState.currentPlayingChannelId == ch.streamId,
                    onClick = { onChannelClick(ch) },
                    onFavoriteClick = { onFavoriteClick(ch) },
                    onRefreshEpg = onRefreshEpg
                )
            }
        }
        items(otherCategories, key = { it.categoryId }) { category ->
            CategoryHeader(
                category = category,
                isExpanded = false,
                onClick = { onToggleCategory(category.categoryId) }
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    category: com.kybers.stream.domain.model.Category,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = if (isExpanded) 4.dp else 0.dp,
        color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Contraer" else "Expandir"
            )
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    channelEpg: com.kybers.stream.domain.model.ChannelEpg?,
    isLoadingEpg: Boolean,
    isCurrentlyPlaying: Boolean = false,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRefreshEpg: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentlyPlaying) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentlyPlaying)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Logo del canal - cambio a cuadrado según especificaciones
            AsyncImage(
                model = channel.icon,
                contentDescription = "Logo de ${channel.name}",
                modifier = Modifier
                    .size(56.dp) // Tamaño aumentado para mejor visibilidad
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas en lugar de círculo
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del canal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del canal - permitir hasta 4 líneas
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 4, // Permitir hasta 4 líneas según especificación
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentlyPlaying)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // EPG mejorado con mejor formateo
                when {
                    isLoadingEpg -> {
                        EpgLoadingPlaceholder()
                    }
                    channelEpg != null -> {
                        EpgNowNextDisplay(
                            channelEpg = channelEpg,
                            use24HourFormat = true
                        )
                    }
                    else -> {
                        Text(
                            text = "Sin información de EPG",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Botón de favorito
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (false) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (false) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = if (false) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChannelGridItem(
    channel: Channel,
    channelEpg: com.kybers.stream.domain.model.ChannelEpg?,
    isLoadingEpg: Boolean,
    isCurrentlyPlaying: Boolean = false,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen del canal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.icon,
                    contentDescription = "Logo de ${channel.name}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Información del canal
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Nombre del canal
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // EPG actual
                channelEpg?.currentProgram?.let { currentProgram ->
                    Text(
                        text = "${currentProgram.getFormattedTime().split(" - ")[0]} - ${currentProgram.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } ?: if (isLoadingEpg) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                    )
                } else {
                    Text(
                        text = "Sin información",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botón de favorito en la esquina
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (false) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (false) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelsLoadingState(
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) {
                    ChannelItemSkeleton()
                }
            }
        }
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(12) {
                    ChannelGridItemSkeleton()
                }
            }
        }
    }
}

@Composable
fun ChannelItemSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                )
            }
            
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun ChannelGridItemSkeleton() {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                )
            }
        }
    }
}

@Composable
fun ChannelsErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = "Error al cargar canales",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
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

@Composable
fun ChannelsEmptyState(
    searchQuery: String,
    selectedCategory: String,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = if (searchQuery.isNotEmpty() || selectedCategory.isNotEmpty()) {
                "No se encontraron canales"
            } else {
                "No hay canales disponibles"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = if (searchQuery.isNotEmpty() || selectedCategory.isNotEmpty()) {
                "Intenta con otros términos de búsqueda o categorías"
            } else {
                "Verifica tu conexión o contacta con tu proveedor de IPTV"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (searchQuery.isNotEmpty() || selectedCategory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onClearFilters) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar filtros")
            }
        }
    }
}
