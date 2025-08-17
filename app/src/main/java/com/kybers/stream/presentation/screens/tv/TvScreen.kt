package com.kybers.stream.presentation.screens.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.kybers.stream.domain.model.Channel
import com.kybers.stream.presentation.components.SearchBar
import com.kybers.stream.presentation.components.ViewMode
import com.kybers.stream.presentation.components.ViewModeToggle
import com.kybers.stream.presentation.components.epg.EpgNowNextDisplay
import com.kybers.stream.presentation.components.epg.EpgLoadingPlaceholder
import com.kybers.stream.presentation.components.epg.EpgErrorDisplay
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvScreen(
    viewModel: TvViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
        SearchAndFilterSection(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearch = { viewModel.search(it) },
            isSearching = uiState.isSearching,
            viewMode = uiState.viewMode,
            onViewModeChange = { viewModel.changeViewMode(it) },
            selectedCategory = selectedCategory,
            categories = uiState.categories,
            showCategoryDropdown = showCategoryDropdown,
            onShowCategoryDropdown = { showCategoryDropdown = it },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryDropdown = false
                viewModel.selectCategory(category)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Chips de categorías rápidas
        if (uiState.categories.isNotEmpty()) {
            CategoryChipsSection(
                categories = uiState.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    viewModel.selectCategory(category)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

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
            uiState.filteredChannels.isEmpty() -> {
                ChannelsEmptyState(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    onClearFilters = {
                        searchQuery = ""
                        selectedCategory = ""
                        viewModel.clearFilters()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                ChannelsList(
                    channels = uiState.filteredChannels,
                    channelsEpg = uiState.channelsEpg,
                    viewMode = uiState.viewMode,
                    isLoadingEpg = uiState.isLoadingEpg,
                    currentPlayingId = currentMedia?.id,
                    onChannelClick = { channel ->
                        viewModel.playChannel(channel)
                    },
                    onFavoriteClick = { channel ->
                        viewModel.toggleFavorite(channel)
                    },
                    onRefreshEpg = { viewModel.refreshEpg() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PlayerSection(
    viewModel: TvViewModel,
    currentMedia: com.kybers.stream.domain.model.MediaInfo?,
    playbackState: String?, // TODO: Use proper playback state type
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
                            AdaptiveText(
                                text = playbackState,
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
fun CategoryChipsSection(
    categories: List<com.kybers.stream.domain.model.Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FilterChip(
                onClick = { onCategorySelected("") },
                label = { Text("Todos") },
                selected = selectedCategory.isEmpty(),
                leadingIcon = if (selectedCategory.isEmpty()) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
        
        items(categories.take(8)) { category -> // Mostrar máximo 8 categorías
            FilterChip(
                onClick = { onCategorySelected(category.categoryName) },
                label = { Text(category.categoryName) },
                selected = selectedCategory == category.categoryName,
                leadingIcon = if (selectedCategory == category.categoryName) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isSearching: Boolean,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    selectedCategory: String,
    categories: List<com.kybers.stream.domain.model.Category>,
    showCategoryDropdown: Boolean,
    onShowCategoryDropdown: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Barra de búsqueda y toggle de vista
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                placeholder = "Buscar canales...",
                isLoading = isSearching,
                modifier = Modifier.weight(1f)
            )
            
            ViewModeToggle(
                currentMode = viewMode,
                onModeChange = onViewModeChange
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Selector de categorías
        ExposedDropdownMenuBox(
            expanded = showCategoryDropdown,
            onExpandedChange = onShowCategoryDropdown
        ) {
            OutlinedTextField(
                value = selectedCategory.ifEmpty { "Todas las categorías" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Outlined.ExpandMore, contentDescription = "Expandir")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { onShowCategoryDropdown(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas las categorías") },
                    onClick = {
                        onCategorySelected("")
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.categoryName) },
                        onClick = {
                            onCategorySelected(category.categoryName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelsList(
    channels: List<Channel>,
    channelsEpg: Map<String, com.kybers.stream.domain.model.ChannelEpg>,
    viewMode: ViewMode,
    isLoadingEpg: Boolean,
    currentPlayingId: String? = null,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit,
    onRefreshEpg: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = channels,
                    key = { it.streamId }
                ) { channel ->
                    ChannelItem(
                        channel = channel,
                        channelEpg = channelsEpg[channel.streamId],
                        isLoadingEpg = isLoadingEpg,
                        isCurrentlyPlaying = currentPlayingId == channel.streamId,
                        onClick = { onChannelClick(channel) },
                        onFavoriteClick = { onFavoriteClick(channel) },
                        onRefreshEpg = onRefreshEpg
                    )
                }
            }
        }
        
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = channels,
                    key = { it.streamId }
                ) { channel ->
                    ChannelGridItem(
                        channel = channel,
                        channelEpg = channelsEpg[channel.streamId],
                        isLoadingEpg = isLoadingEpg,
                        isCurrentlyPlaying = currentPlayingId == channel.streamId,
                        onClick = { onChannelClick(channel) },
                        onFavoriteClick = { onFavoriteClick(channel) }
                    )
                }
            }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo del canal
            AsyncImage(
                model = channel.icon,
                contentDescription = "Logo de ${channel.name}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del canal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del canal
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // EPG real o placeholder
                when {
                    isLoadingEpg -> {
                        EpgLoadingPlaceholder()
                    }
                    channelEpg != null -> {
                        EpgNowNextDisplay(
                            channelEpg = channelEpg,
                            use24HourFormat = true // TODO: Get from preferences
                        )
                    }
                    else -> {
                        EpgErrorDisplay(
                            onRetry = onRefreshEpg
                        )
                    }
                }
            }
            
            // Botón de favorito
            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(
                    imageVector = if (false) Icons.Default.Favorite else Icons.Default.FavoriteBorder, // TODO: manejar favoritos
                    contentDescription = "Favorito",
                    tint = if (false) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
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
fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}