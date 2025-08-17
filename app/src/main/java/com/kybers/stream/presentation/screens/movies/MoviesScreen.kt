package com.kybers.stream.presentation.screens.movies

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
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
import com.kybers.stream.domain.model.Movie
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents

enum class ViewMode {
    GRID, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = hiltViewModel(),
    onNavigateToMovieDetail: (String) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val focusManager = LocalFocusManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedQuality by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(if (isTablet) ViewMode.GRID else ViewMode.GRID) }

    // Recordar la vista seleccionada
    LaunchedEffect(viewMode) {
        // TODO: Save view preference to DataStore
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .consumeWindowInsets(PaddingValues(bottom = 0.dp)) // Consume solo los insets que el padre ya maneja
            .semantics { contentDescription = "Pantalla de películas con filtros y vista personalizable" }
    ) {
        // Header con título y controles
        MoviesTopSection(
            title = "Películas",
            resultCount = uiState.filteredMovies.size,
            totalCount = uiState.allMovies.size,
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Barra de búsqueda
        SearchSection(
            searchQuery = searchQuery,
            onSearchQueryChange = { query ->
                searchQuery = query
                viewModel.search(query)
            },
            onClear = {
                searchQuery = ""
                focusManager.clearFocus()
                viewModel.search("")
            },
            isSearching = uiState.isSearching,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filtros expandibles
        if (showFilters) {
            AdvancedFiltersSection(
                selectedCategory = selectedCategory,
                selectedQuality = selectedQuality,
                selectedYear = selectedYear,
                selectedGenre = selectedGenre,
                selectedSort = selectedSort,
                categories = uiState.categories,
                qualities = listOf("Todos", "SD", "HD", "FHD", "4K"),
                years = (2024 downTo 1990).map { it.toString() },
                genres = listOf("Todos", "Acción", "Drama", "Comedia", "Terror", "Ciencia Ficción", "Romance"),
                sortOptions = listOf("A-Z", "Z-A", "Año ↓", "Año ↑", "Agregado"),
                onCategorySelected = { category ->
                    selectedCategory = category
                    viewModel.selectCategory(category)
                },
                onQualitySelected = { quality ->
                    selectedQuality = quality
                    viewModel.filterByQuality(quality)
                },
                onYearSelected = { year ->
                    selectedYear = year
                    viewModel.filterByYear(year)
                },
                onGenreSelected = { genre ->
                    selectedGenre = genre
                    viewModel.filterByGenre(genre)
                },
                onSortSelected = { sort ->
                    selectedSort = sort
                    viewModel.sortBy(sort)
                },
                onClearFilters = {
                    selectedCategory = ""
                    selectedQuality = ""
                    selectedYear = ""
                    selectedGenre = ""
                    selectedSort = ""
                    viewModel.clearFilters()
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Lista de películas
        when {
            uiState.isLoading -> {
                MoviesLoadingState(
                    viewMode = viewMode,
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.error != null -> {
                MoviesErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadMovies() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.filteredMovies.isEmpty() -> {
                MoviesEmptyState(
                    hasFilters = searchQuery.isNotEmpty() || selectedCategory.isNotEmpty() || 
                                selectedQuality.isNotEmpty() || selectedYear.isNotEmpty() || 
                                selectedGenre.isNotEmpty(),
                    onClearFilters = {
                        searchQuery = ""
                        selectedCategory = ""
                        selectedQuality = ""
                        selectedYear = ""
                        selectedGenre = ""
                        selectedSort = ""
                        viewModel.clearFilters()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                MoviesList(
                    movies = uiState.filteredMovies,
                    viewMode = viewMode,
                    onMovieClick = { movie ->
                        onNavigateToMovieDetail(movie.streamId)
                        onMovieClick(movie)
                    },
                    onFavoriteClick = { movie ->
                        viewModel.toggleFavorite(movie.streamId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Nuevos componentes para MoviesScreen moderna

@Composable
fun MoviesTopSection(
    title: String,
    resultCount: Int,
    totalCount: Int,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            AdaptiveText(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            AdaptiveText(
                text = if (resultCount == totalCount) {
                    "$totalCount películas"
                } else {
                    "$resultCount de $totalCount películas"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón de filtros
            IconButton(
                onClick = onToggleFilters,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (showFilters) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (showFilters) "Ocultar filtros" else "Mostrar filtros",
                    tint = if (showFilters) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Toggle de vista
            SegmentedButton(
                options = listOf(
                    Icons.Default.GridView to "Cuadrícula",
                    Icons.Default.ViewList to "Lista"
                ),
                selectedIndex = if (viewMode == ViewMode.GRID) 0 else 1,
                onSelectionChanged = { index ->
                    onViewModeChange(if (index == 0) ViewMode.GRID else ViewMode.LIST)
                }
            )
        }
    }
}

@Composable
fun SegmentedButton(
    options: List<Pair<androidx.compose.ui.graphics.vector.ImageVector, String>>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, (icon, description) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedIndex == index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            Color.Transparent
                    )
                    .clickable { onSelectionChanged(index) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = if (selectedIndex == index) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { 
            AdaptiveText(
                text = "Buscar por título...",
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            Row {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda"
                        )
                    }
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { /* Ya se maneja en onSearchQueryChange */ }
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AdvancedFiltersSection(
    selectedCategory: String,
    selectedQuality: String,
    selectedYear: String,
    selectedGenre: String,
    selectedSort: String,
    categories: List<com.kybers.stream.domain.model.Category>,
    qualities: List<String>,
    years: List<String>,
    genres: List<String>,
    sortOptions: List<String>,
    onCategorySelected: (String) -> Unit,
    onQualitySelected: (String) -> Unit,
    onYearSelected: (String) -> Unit,
    onGenreSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = "Filtros avanzados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClearFilters) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar")
                }
            }
            
            // Filtros con chips horizontales
            FilterChipsRow(
                title = "Categoría",
                items = listOf("Todas") + categories.map { it.categoryName },
                selectedItem = selectedCategory.ifEmpty { "Todas" },
                onItemSelected = { item ->
                    onCategorySelected(if (item == "Todas") "" else item)
                }
            )
            
            FilterChipsRow(
                title = "Calidad",
                items = qualities,
                selectedItem = selectedQuality.ifEmpty { "Todos" },
                onItemSelected = { item ->
                    onQualitySelected(if (item == "Todos") "" else item)
                }
            )
            
            FilterChipsRow(
                title = "Año",
                items = listOf("Todos") + years.take(10), // Mostrar últimos 10 años
                selectedItem = selectedYear.ifEmpty { "Todos" },
                onItemSelected = { item ->
                    onYearSelected(if (item == "Todos") "" else item)
                }
            )
            
            FilterChipsRow(
                title = "Género",
                items = genres,
                selectedItem = selectedGenre.ifEmpty { "Todos" },
                onItemSelected = { item ->
                    onGenreSelected(if (item == "Todos") "" else item)
                }
            )
            
            FilterChipsRow(
                title = "Ordenar",
                items = sortOptions,
                selectedItem = selectedSort.ifEmpty { "A-Z" },
                onItemSelected = { item ->
                    onSortSelected(item)
                }
            )
        }
    }
}

@Composable
fun FilterChipsRow(
    title: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        AdaptiveText(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                FilterChip(
                    onClick = { onItemSelected(item) },
                    label = { Text(item) },
                    selected = selectedItem == item,
                    leadingIcon = if (selectedItem == item) {
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
}

@Composable
fun MoviesLoadingState(
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(12) {
                    MovieGridItemSkeleton()
                }
            }
        }
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(8) {
                    MovieListItemSkeleton()
                }
            }
        }
    }
}

@Composable
fun MovieGridItemSkeleton() {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            SkeletonComponents.SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
            
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                )
            }
        }
    }
}

@Composable
fun MovieListItemSkeleton() {
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
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                )
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                )
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                )
            }
        }
    }
}

@Composable
fun MoviesErrorState(
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
            text = "Error al cargar películas",
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

@Composable
fun MoviesEmptyState(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasFilters) Icons.Default.SearchOff else Icons.Default.Movie,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = if (hasFilters) {
                "No se encontraron películas"
            } else {
                "No hay películas disponibles"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = if (hasFilters) {
                "Intenta ajustar los filtros o términos de búsqueda"
            } else {
                "Verifica tu conexión o contacta con tu proveedor de IPTV"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (hasFilters) {
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
    selectedCategory: String,
    categories: List<com.kybers.stream.domain.model.Category>,
    showCategoryDropdown: Boolean,
    onShowCategoryDropdown: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar películas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // Botón de cambio de vista
            IconButton(
                onClick = {
                    onViewModeChange(
                        if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                    )
                }
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.GRID) Icons.Default.List else Icons.Default.GridView,
                    contentDescription = "Cambiar vista"
                )
            }
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
fun MoviesList(
    movies: List<Movie>,
    viewMode: ViewMode,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = movies,
                    key = { it.streamId }
                ) { movie ->
                    MovieGridItem(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onFavoriteClick = { onFavoriteClick(movie) }
                    )
                }
            }
        }
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = movies,
                    key = { it.streamId }
                ) { movie ->
                    MovieListItem(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onFavoriteClick = { onFavoriteClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieGridItem(
    movie: Movie,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column {
                // Póster (200×300px según especificaciones)
                Box {
                    AsyncImage(
                        model = movie.icon,
                        contentDescription = "Póster de ${movie.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f) // Ratio 200x300
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    
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
                            contentDescription = "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Información de la película
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Título (máx. 2 líneas)
                    AdaptiveText(
                        text = movie.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Año (simulado)
                    AdaptiveText(
                        text = "2023",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Duración (simulada)
                    AdaptiveText(
                        text = "2h 15m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Calidad
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                AdaptiveText(
                                    text = "HD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            // Género
                            AdaptiveText(
                                text = "Acción",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            // Botón de favorito en la esquina superior derecha
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder, // TODO: manejar estado de favorito
                    contentDescription = "Favorito",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
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
            // Póster miniatura
            AsyncImage(
                model = movie.icon,
                contentDescription = "Póster de ${movie.name}",
                modifier = Modifier
                    .size(width = 60.dp, height = 90.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información de la película
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título (máx. 2 líneas)
                AdaptiveText(
                    text = movie.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Año
                    AdaptiveText(
                        text = "2023",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Duración
                    AdaptiveText(
                        text = "2h 15m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Calidad
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        AdaptiveText(
                            text = "HD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Género
                AdaptiveText(
                    text = "Acción, Drama",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Botón de favorito
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder, // TODO: manejar estado de favorito
                    contentDescription = "Favorito",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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