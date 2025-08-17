package com.kybers.stream.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.Category
import com.kybers.stream.domain.model.EnrichedMovie
import com.kybers.stream.domain.model.Movie
import com.kybers.stream.domain.model.XtreamResult
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.usecase.GetVodCategoriesUseCase
import com.kybers.stream.domain.usecase.GetVodStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val isLoading: Boolean = false,
    val isEnrichingWithTMDB: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val enrichedMovies: List<EnrichedMovie> = emptyList(),
    val allMovies: List<Movie> = emptyList(),
    val allEnrichedMovies: List<EnrichedMovie> = emptyList(),
    val filteredMovies: List<Movie> = emptyList(),
    val filteredEnrichedMovies: List<EnrichedMovie> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val qualityFilter: String = "",
    val yearFilter: String = "",
    val genreFilter: String = "",
    val sortBy: String = "",
    val useTMDBData: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getVodCategoriesUseCase: GetVodCategoriesUseCase,
    private val getVodStreamsUseCase: GetVodStreamsUseCase,
    private val tmdbUseCases: TMDBUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private var allMovies: List<Movie> = emptyList()
    private var allEnrichedMovies: List<EnrichedMovie> = emptyList()

    init {
        loadCategories()
        loadMovies()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getVodCategoriesUseCase().collect { result ->
                when (result) {
                    is XtreamResult.Success -> {
                        _uiState.update { it.copy(categories = result.data, error = null) }
                    }
                    is XtreamResult.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is XtreamResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                }
            }
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getVodStreamsUseCase().collect { result ->
                when (result) {
                    is XtreamResult.Success -> {
                        allMovies = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                movies = result.data,
                                allMovies = result.data,
                                filteredMovies = applyFilters(result.data, currentState.selectedCategory, currentState.searchQuery),
                                error = null
                            )
                        }
                        
                        // Ya no enriquecemos con TMDB aquí - se hace bajo demanda en detalles
                    }
                    is XtreamResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is XtreamResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                }
            }
        }
    }


    fun selectCategory(categoryName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = categoryName,
                filteredMovies = applyFilters(allMovies, categoryName, currentState.searchQuery),
                filteredEnrichedMovies = applyFiltersToEnriched(allEnrichedMovies, categoryName, currentState.searchQuery)
            )
        }
    }

    fun search(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                isSearching = query.isNotEmpty(),
                filteredMovies = applyFilters(allMovies, currentState.selectedCategory, query),
                filteredEnrichedMovies = applyFiltersToEnriched(allEnrichedMovies, currentState.selectedCategory, query)
            )
        }
    }

    fun filterByQuality(quality: String) {
        _uiState.update { currentState ->
            currentState.copy(
                qualityFilter = quality,
                filteredMovies = applyAllFilters(allMovies, currentState)
            )
        }
    }

    fun filterByYear(year: String) {
        _uiState.update { currentState ->
            currentState.copy(
                yearFilter = year,
                filteredMovies = applyAllFilters(allMovies, currentState)
            )
        }
    }

    fun filterByGenre(genre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                genreFilter = genre,
                filteredMovies = applyAllFilters(allMovies, currentState)
            )
        }
    }

    fun sortBy(sortOption: String) {
        _uiState.update { currentState ->
            currentState.copy(
                sortBy = sortOption,
                filteredMovies = applyAllFilters(allMovies, currentState)
            )
        }
    }

    fun clearFilters() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = "",
                qualityFilter = "",
                yearFilter = "",
                genreFilter = "",
                sortBy = "",
                filteredMovies = allMovies
            )
        }
    }

    fun toggleFavorite(movieId: String) {
        // TODO: Implement favorite functionality
    }
    
    fun toggleTMDBData() {
        _uiState.update { currentState ->
            val newUseTMDB = !currentState.useTMDBData
            currentState.copy(useTMDBData = newUseTMDB)
        }
        
        // TMDB ahora se carga bajo demanda en pantallas de detalle solamente
        // No se cargan datos TMDB en las listas principales
    }

    private fun applyFilters(
        movies: List<Movie>,
        categoryName: String,
        searchQuery: String
    ): List<Movie> {
        var filtered = movies

        // Filtrar por categoría
        if (categoryName.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == categoryName }
        }

        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { 
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    private fun applyAllFilters(movies: List<Movie>, state: MoviesUiState): List<Movie> {
        var filtered = movies

        // Filtrar por categoría
        if (state.selectedCategory.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == state.selectedCategory }
        }

        // Filtrar por búsqueda
        if (state.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { 
                it.name.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Filtrar por calidad
        if (state.qualityFilter.isNotEmpty()) {
            filtered = filtered.filter { 
                it.quality?.contains(state.qualityFilter, ignoreCase = true) == true
            }
        }

        // Filtrar por año
        if (state.yearFilter.isNotEmpty()) {
            filtered = filtered.filter { 
                it.year == state.yearFilter
            }
        }

        // Filtrar por género
        if (state.genreFilter.isNotEmpty()) {
            filtered = filtered.filter { 
                it.genre?.contains(state.genreFilter, ignoreCase = true) == true
            }
        }

        // Aplicar ordenamiento
        filtered = when (state.sortBy) {
            "name_asc" -> filtered.sortedBy { it.name }
            "name_desc" -> filtered.sortedByDescending { it.name }
            "year_asc" -> filtered.sortedBy { it.year }
            "year_desc" -> filtered.sortedByDescending { it.year }
            "rating_asc" -> filtered.sortedBy { it.rating?.toDoubleOrNull() ?: 0.0 }
            "rating_desc" -> filtered.sortedByDescending { it.rating?.toDoubleOrNull() ?: 0.0 }
            else -> filtered
        }

        return filtered
    }
    
    private fun applyFiltersToEnriched(
        enrichedMovies: List<EnrichedMovie>,
        categoryName: String,
        searchQuery: String
    ): List<EnrichedMovie> {
        var filtered = enrichedMovies

        // Filtrar por categoría
        if (categoryName.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == categoryName }
        }

        // Filtrar por búsqueda (incluir búsqueda en datos de TMDB)
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { enrichedMovie ->
                enrichedMovie.name.contains(searchQuery, ignoreCase = true) ||
                enrichedMovie.tmdbData?.title?.contains(searchQuery, ignoreCase = true) == true ||
                enrichedMovie.tmdbData?.originalTitle?.contains(searchQuery, ignoreCase = true) == true ||
                enrichedMovie.tmdbData?.overview?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        return filtered
    }
    
    private fun applyAllFiltersToEnriched(enrichedMovies: List<EnrichedMovie>, state: MoviesUiState): List<EnrichedMovie> {
        var filtered = enrichedMovies

        // Filtrar por categoría
        if (state.selectedCategory.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == state.selectedCategory }
        }

        // Filtrar por búsqueda (incluir búsqueda en datos de TMDB)
        if (state.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { enrichedMovie ->
                enrichedMovie.name.contains(state.searchQuery, ignoreCase = true) ||
                enrichedMovie.tmdbData?.title?.contains(state.searchQuery, ignoreCase = true) == true ||
                enrichedMovie.tmdbData?.originalTitle?.contains(state.searchQuery, ignoreCase = true) == true ||
                enrichedMovie.tmdbData?.overview?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }

        // Filtrar por calidad
        if (state.qualityFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedMovie ->
                // Buscar en calidad original de Xtream
                val xtreamQuality = when {
                    enrichedMovie.name.contains("4K", ignoreCase = true) -> "4K"
                    enrichedMovie.name.contains("1080p", ignoreCase = true) -> "1080p"
                    enrichedMovie.name.contains("720p", ignoreCase = true) -> "720p"
                    enrichedMovie.name.contains("480p", ignoreCase = true) -> "480p"
                    else -> null
                }
                xtreamQuality?.contains(state.qualityFilter, ignoreCase = true) == true
            }
        }

        // Filtrar por año
        if (state.yearFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedMovie ->
                // Usar año de TMDB si está disponible, sino el de Xtream
                val year = enrichedMovie.tmdbData?.releaseDate?.take(4) 
                    ?: enrichedMovie.name.let { name ->
                        // Extraer año del nombre si no está en TMDB
                        Regex("\\b(19|20)\\d{2}\\b").find(name)?.value
                    }
                year == state.yearFilter
            }
        }

        // Filtrar por género
        if (state.genreFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedMovie ->
                // Buscar en géneros de TMDB primero, luego en Xtream
                enrichedMovie.tmdbData?.genres?.any { 
                    it.name.contains(state.genreFilter, ignoreCase = true) 
                } == true
            }
        }

        // Aplicar ordenamiento
        filtered = when (state.sortBy) {
            "name_asc" -> filtered.sortedBy { it.tmdbData?.title ?: it.name }
            "name_desc" -> filtered.sortedByDescending { it.tmdbData?.title ?: it.name }
            "year_asc" -> filtered.sortedBy { 
                it.tmdbData?.releaseDate?.take(4)?.toIntOrNull() ?: 0 
            }
            "year_desc" -> filtered.sortedByDescending { 
                it.tmdbData?.releaseDate?.take(4)?.toIntOrNull() ?: 0 
            }
            "rating_asc" -> filtered.sortedBy { 
                it.tmdbData?.voteAverage ?: it.rating5Based 
            }
            "rating_desc" -> filtered.sortedByDescending { 
                it.tmdbData?.voteAverage ?: it.rating5Based 
            }
            "popularity_desc" -> filtered.sortedByDescending { 
                it.tmdbData?.popularity ?: 0.0 
            }
            else -> filtered
        }

        return filtered
    }
}