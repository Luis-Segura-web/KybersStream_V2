package com.kybers.stream.presentation.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.*
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    onNavigateBack: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onMovieSelected: (Movie) -> Unit,
    onSeriesSelected: (Series) -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Búsqueda global con resultados tabulados" }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con barra de búsqueda
            SearchHeader(
                searchText = searchText,
                onSearchTextChange = { 
                    searchText = it
                    viewModel.updateSearchQuery(it)
                },
                onNavigateBack = onNavigateBack,
                onClearSearch = { 
                    searchText = ""
                    viewModel.clearSearch()
                },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                isTablet = isTablet
            )
            
            when {
                searchQuery.isEmpty() -> {
                    SearchEmptyState(
                        onSuggestionSelected = { suggestion ->
                            searchText = suggestion
                            viewModel.updateSearchQuery(suggestion)
                        },
                        isTablet = isTablet,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.isLoading -> {
                    SearchLoadingState(
                        isTablet = isTablet,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.error != null -> {
                    SearchErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.search(searchQuery) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.hasResults() -> {
                    SearchResultsContent(
                        uiState = uiState,
                        selectedTab = selectedTab,
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
                        onChannelSelected = onChannelSelected,
                        onMovieSelected = onMovieSelected,
                        onSeriesSelected = onSeriesSelected,
                        isTablet = isTablet,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                else -> {
                    SearchNoResultsState(
                        searchQuery = searchQuery,
                        onSuggestionSelected = { suggestion ->
                            searchText = suggestion
                            viewModel.updateSearchQuery(suggestion)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearSearch: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { 
                    Text("Buscar canales, películas, series...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun SearchResultsContent(
    uiState: GlobalSearchUiState,
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onMovieSelected: (Movie) -> Unit,
    onSeriesSelected: (Series) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Pestañas de resultados
        SearchResultsTabs(
            uiState = uiState,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isTablet = isTablet
        )
        
        // Contenido de la pestaña seleccionada
        when (selectedTab) {
            SearchTab.ALL -> {
                SearchAllResults(
                    uiState = uiState,
                    onChannelSelected = onChannelSelected,
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
            SearchTab.CHANNELS -> {
                SearchChannelResults(
                    channels = uiState.channels,
                    onChannelSelected = onChannelSelected,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
            SearchTab.MOVIES -> {
                SearchMovieResults(
                    movies = uiState.movies,
                    onMovieSelected = onMovieSelected,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
            SearchTab.SERIES -> {
                SearchSeriesResults(
                    series = uiState.series,
                    onSeriesSelected = onSeriesSelected,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SearchResultsTabs(
    uiState: GlobalSearchUiState,
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    isTablet: Boolean
) {
    val tabs = listOf(
        SearchTab.ALL to "Todos (${uiState.totalResults})",
        SearchTab.CHANNELS to "Canales (${uiState.channels.size})",
        SearchTab.MOVIES to "Películas (${uiState.movies.size})",
        SearchTab.SERIES to "Series (${uiState.series.size})"
    )
    
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
        modifier = Modifier.fillMaxWidth(),
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty()) {
                val currentTabPosition = tabPositions[tabs.indexOfFirst { it.first == selectedTab }]
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(currentTabPosition),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    AdaptiveText(
                        text = label,
                        style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Composable
fun SearchAllResults(
    uiState: GlobalSearchUiState,
    onChannelSelected: (Channel) -> Unit,
    onMovieSelected: (Movie) -> Unit,
    onSeriesSelected: (Series) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de canales
        if (uiState.channels.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Canales",
                    count = uiState.channels.size,
                    isTablet = isTablet
                )
            }
            
            items(uiState.channels.take(3)) { channel ->
                SearchChannelItem(
                    channel = channel,
                    onSelected = { onChannelSelected(channel) },
                    isTablet = isTablet
                )
            }
            
            if (uiState.channels.size > 3) {
                item {
                    TextButton(
                        onClick = { /* TODO: Show all channels */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todos los canales (${uiState.channels.size})")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // Sección de películas
        if (uiState.movies.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Películas",
                    count = uiState.movies.size,
                    isTablet = isTablet
                )
            }
            
            items(uiState.movies.take(3)) { movie ->
                SearchMovieItem(
                    movie = movie,
                    onSelected = { onMovieSelected(movie) },
                    isTablet = isTablet
                )
            }
            
            if (uiState.movies.size > 3) {
                item {
                    TextButton(
                        onClick = { /* TODO: Show all movies */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todas las películas (${uiState.movies.size})")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // Sección de series
        if (uiState.series.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Series",
                    count = uiState.series.size,
                    isTablet = isTablet
                )
            }
            
            items(uiState.series.take(3)) { series ->
                SearchSeriesItem(
                    series = series,
                    onSelected = { onSeriesSelected(series) },
                    isTablet = isTablet
                )
            }
            
            if (uiState.series.size > 3) {
                item {
                    TextButton(
                        onClick = { /* TODO: Show all series */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todas las series (${uiState.series.size})")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchChannelResults(
    channels: List<Channel>,
    onChannelSelected: (Channel) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            SearchChannelItem(
                channel = channel,
                onSelected = { onChannelSelected(channel) },
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun SearchMovieResults(
    movies: List<Movie>,
    onMovieSelected: (Movie) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(movies) { movie ->
            SearchMovieItem(
                movie = movie,
                onSelected = { onMovieSelected(movie) },
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun SearchSeriesResults(
    series: List<Series>,
    onSeriesSelected: (Series) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(series) { seriesItem ->
            SearchSeriesItem(
                series = seriesItem,
                onSelected = { onSeriesSelected(seriesItem) },
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun SearchSectionHeader(
    title: String,
    count: Int,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdaptiveText(
            text = title,
            style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            AdaptiveText(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SearchChannelItem(
    channel: Channel,
    onSelected: () -> Unit,
    isTablet: Boolean
) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.icon,
                contentDescription = "Logo de ${channel.name}",
                modifier = Modifier
                    .size(if (isTablet) 56.dp else 48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AdaptiveText(
                    text = channel.name,
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                channel.categoryName?.let { category ->
                    AdaptiveText(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproducir",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SearchMovieItem(
    movie: Movie,
    onSelected: () -> Unit,
    isTablet: Boolean
) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = movie.poster,
                contentDescription = "Póster de ${movie.name}",
                modifier = Modifier
                    .width(if (isTablet) 80.dp else 60.dp)
                    .height(if (isTablet) 120.dp else 90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdaptiveText(
                    text = movie.name,
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.year?.let { year ->
                        SearchChip(text = year)
                    }
                    
                    movie.genre?.let { genre ->
                        SearchChip(text = genre)
                    }
                }
                
                movie.plot?.let { plot ->
                    AdaptiveText(
                        text = plot,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SearchSeriesItem(
    series: Series,
    onSelected: () -> Unit,
    isTablet: Boolean
) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = series.cover,
                contentDescription = "Póster de ${series.name}",
                modifier = Modifier
                    .width(if (isTablet) 80.dp else 60.dp)
                    .height(if (isTablet) 120.dp else 90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdaptiveText(
                    text = series.name,
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    series.genre?.let { genre ->
                        SearchChip(text = genre)
                    }
                    
                    SearchChip(text = "${series.seasonCount ?: 0} temporadas")
                }
                
                series.plot?.let { plot ->
                    AdaptiveText(
                        text = plot,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SearchChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        AdaptiveText(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SearchEmptyState(
    onSuggestionSelected: (String) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Netflix", "HBO", "Deportes", "Noticias", "Música",
        "Acción", "Comedia", "Drama", "Terror", "Sci-Fi"
    )
    
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(if (isTablet) 80.dp else 64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = "Buscar contenido",
            style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = "Encuentra canales, películas y series",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AdaptiveText(
            text = "Sugerencias:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }
}

@Composable
fun SearchNoResultsState(
    searchQuery: String,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Películas acción", "Series drama", "Canales deportes", "Documentales"
    )
    
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
            text = "Sin resultados",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = "No se encontraron resultados para \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AdaptiveText(
            text = "Prueba con:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        AdaptiveText(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SearchLoadingState(
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Skeleton tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        }
        
        // Skeleton results
        repeat(8) {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 120.dp else 100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun SearchErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
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
            text = "Error en la búsqueda",
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

// ViewModel placeholder y clases de datos
class GlobalSearchViewModel : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _selectedTab = MutableStateFlow(SearchTab.ALL)
    val selectedTab: StateFlow<SearchTab> = _selectedTab
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            search(query)
        }
    }
    
    fun search(query: String) {
        // TODO: Implementar búsqueda
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = GlobalSearchUiState()
    }
    
    fun selectTab(tab: SearchTab) {
        _selectedTab.value = tab
    }
}

data class GlobalSearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList()
) {
    val totalResults: Int = channels.size + movies.size + series.size
    
    fun hasResults(): Boolean = totalResults > 0
}

enum class SearchTab {
    ALL, CHANNELS, MOVIES, SERIES
}