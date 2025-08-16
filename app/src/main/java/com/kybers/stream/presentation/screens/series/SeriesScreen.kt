package com.kybers.stream.presentation.screens.series

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.Series
import com.kybers.stream.presentation.screens.movies.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel = hiltViewModel(),
    onSeriesClick: (Series) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barra de búsqueda, categorías y vista
        SearchAndFilterSection(
            searchQuery = searchQuery,
            onSearchQueryChange = { 
                searchQuery = it
                viewModel.search(it)
            },
            selectedCategory = selectedCategory,
            categories = uiState.categories,
            showCategoryDropdown = showCategoryDropdown,
            onShowCategoryDropdown = { showCategoryDropdown = it },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryDropdown = false
                viewModel.selectCategory(category)
            },
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Lista de series
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
                    error = uiState.error!!,
                    onRetry = { viewModel.loadSeries() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                SeriesList(
                    series = uiState.filteredSeries,
                    viewMode = viewMode,
                    onSeriesClick = onSeriesClick,
                    modifier = Modifier.fillMaxSize()
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
                placeholder = { Text("Buscar series...") },
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
fun SeriesList(
    series: List<Series>,
    viewMode: ViewMode,
    onSeriesClick: (Series) -> Unit,
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
                    items = series,
                    key = { it.seriesId }
                ) { seriesItem ->
                    SeriesGridItem(
                        series = seriesItem,
                        onClick = { onSeriesClick(seriesItem) }
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
                    items = series,
                    key = { it.seriesId }
                ) { seriesItem ->
                    SeriesListItem(
                        series = seriesItem,
                        onClick = { onSeriesClick(seriesItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun SeriesGridItem(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Miniatura de la serie
            AsyncImage(
                model = series.cover,
                contentDescription = "Miniatura de ${series.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            // Información de la serie
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Título (máx. 2 líneas)
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Temporadas/Episodios (simulado)
                Text(
                    text = "3 temporadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Género (simulado)
                Text(
                    text = "Drama",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Calidad (simulada)
                Text(
                    text = "HD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SeriesListItem(
    series: Series,
    onClick: () -> Unit,
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
            // Miniatura
            AsyncImage(
                model = series.cover,
                contentDescription = "Miniatura de ${series.name}",
                modifier = Modifier
                    .size(width = 60.dp, height = 90.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información de la serie
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título (máx. 2 líneas)
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Temporadas/Episodios
                    Text(
                        text = "3 temporadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Calidad
                    Text(
                        text = "HD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Género
                Text(
                    text = "Drama, Suspense",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
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