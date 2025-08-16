package com.kybers.stream.presentation.screens.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvScreen(
    viewModel: TvViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Reproductor en la parte superior
        PlayerSection(
            viewModel = viewModel,
            currentMedia = currentMedia,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Barra de búsqueda y categorías
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
                .padding(16.dp)
        )

        // Lista de canales
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorMessage(
                    error = uiState.error,
                    onRetry = { viewModel.loadChannels() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                ChannelsList(
                    channels = uiState.filteredChannels,
                    viewMode = uiState.viewMode,
                    onChannelClick = { channel ->
                        viewModel.playChannel(channel)
                    },
                    onFavoriteClick = { channel ->
                        viewModel.toggleFavorite(channel)
                    },
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .background(Color.Black)
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (currentMedia != null) {
            // Reproductor Media3
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.getExoPlayer()
                        useController = true
                        controllerShowTimeoutMs = 3000
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder cuando no hay reproducción
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selecciona un canal para reproducir",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
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
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.name)
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
    viewMode: ViewMode,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit,
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
                        onClick = { onChannelClick(channel) },
                        onFavoriteClick = { onFavoriteClick(channel) }
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
                
                // EPG actual (simulado)
                Text(
                    text = "20:00 - Programa Actual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // EPG siguiente (simulado)
                Text(
                    text = "21:00 - Siguiente Programa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                
                // EPG actual (simulado)
                Text(
                    text = "20:00 - Programa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
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