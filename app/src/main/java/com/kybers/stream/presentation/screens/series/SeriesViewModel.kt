package com.kybers.stream.presentation.screens.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.Category
import com.kybers.stream.domain.model.EnrichedSeries
import com.kybers.stream.domain.model.Series
import com.kybers.stream.domain.model.XtreamResult
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.usecase.GetSeriesCategoriesUseCase
import com.kybers.stream.domain.usecase.GetSeriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesUiState(
    val isLoading: Boolean = false,
    val isEnrichingWithTMDB: Boolean = false,
    val series: List<Series> = emptyList(),
    val enrichedSeries: List<EnrichedSeries> = emptyList(),
    val allSeries: List<Series> = emptyList(),
    val allEnrichedSeries: List<EnrichedSeries> = emptyList(),
    val filteredSeries: List<Series> = emptyList(),
    val filteredEnrichedSeries: List<EnrichedSeries> = emptyList(),
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
class SeriesViewModel @Inject constructor(
    private val getSeriesCategoriesUseCase: GetSeriesCategoriesUseCase,
    private val getSeriesUseCase: GetSeriesUseCase,
    private val tmdbUseCases: TMDBUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    private var allSeries: List<Series> = emptyList()
    private var allEnrichedSeries: List<EnrichedSeries> = emptyList()

    init {
        loadCategories()
        loadSeries()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getSeriesCategoriesUseCase().collect { result ->
                when (result) {
                    is XtreamResult.Success -> {
                        _uiState.update { it.copy(categories = result.data, error = null) }
                    }
                    is XtreamResult.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is XtreamResult.Loading -> {
                        // Mantener estado de carga si es necesario
                    }
                }
            }
        }
    }

    fun loadSeries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getSeriesUseCase().collect { result ->
                when (result) {
                    is XtreamResult.Success -> {
                        allSeries = result.data
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                series = result.data,
                                allSeries = result.data,
                                filteredSeries = applyFilters(result.data, it.selectedCategory, it.searchQuery),
                                error = null
                            )
                        }
                        
                        // Enriquecer con datos de TMDB
                        if (_uiState.value.useTMDBData) {
                            enrichSeriesWithTMDB(result.data)
                        }
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
    
    private fun enrichSeriesWithTMDB(series: List<Series>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnrichingWithTMDB = true) }
            
            tmdbUseCases.enrichSeriesList(series).fold(
                onSuccess = { enrichedSeries ->
                    allEnrichedSeries = enrichedSeries
                    _uiState.update { currentState ->
                        currentState.copy(
                            isEnrichingWithTMDB = false,
                            enrichedSeries = enrichedSeries,
                            allEnrichedSeries = enrichedSeries,
                            filteredEnrichedSeries = applyFiltersToEnriched(
                                enrichedSeries, 
                                currentState.selectedCategory, 
                                currentState.searchQuery
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isEnrichingWithTMDB = false,
                            error = "Error enriqueciendo con TMDB: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun selectCategory(categoryName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = categoryName,
                filteredSeries = applyFilters(allSeries, categoryName, currentState.searchQuery),
                filteredEnrichedSeries = applyFiltersToEnriched(allEnrichedSeries, categoryName, currentState.searchQuery)
            )
        }
    }

    fun search(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                isSearching = query.isNotEmpty(),
                filteredSeries = applyFilters(allSeries, currentState.selectedCategory, query),
                filteredEnrichedSeries = applyFiltersToEnriched(allEnrichedSeries, currentState.selectedCategory, query)
            )
        }
    }

    fun filterByQuality(quality: String) {
        _uiState.update { currentState ->
            currentState.copy(
                qualityFilter = quality,
                filteredSeries = applyAllFilters(allSeries, currentState)
            )
        }
    }

    fun filterByYear(year: String) {
        _uiState.update { currentState ->
            currentState.copy(
                yearFilter = year,
                filteredSeries = applyAllFilters(allSeries, currentState)
            )
        }
    }

    fun filterByGenre(genre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                genreFilter = genre,
                filteredSeries = applyAllFilters(allSeries, currentState)
            )
        }
    }

    fun sortBy(sortOption: String) {
        _uiState.update { currentState ->
            currentState.copy(
                sortBy = sortOption,
                filteredSeries = applyAllFilters(allSeries, currentState)
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
                filteredSeries = allSeries
            )
        }
    }

    fun toggleFavorite(seriesId: String) {
        // TODO: Implement favorite functionality
    }
    
    fun toggleTMDBData() {
        _uiState.update { currentState ->
            val newUseTMDB = !currentState.useTMDBData
            currentState.copy(useTMDBData = newUseTMDB)
        }
        
        // Si se activa TMDB y no tenemos datos enriquecidos, los cargamos
        if (_uiState.value.useTMDBData && allEnrichedSeries.isEmpty() && allSeries.isNotEmpty()) {
            enrichSeriesWithTMDB(allSeries)
        }
    }

    private fun applyFilters(
        series: List<Series>,
        categoryName: String,
        searchQuery: String
    ): List<Series> {
        var filtered = series

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

    private fun applyAllFilters(series: List<Series>, state: SeriesUiState): List<Series> {
        var filtered = series

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
        enrichedSeries: List<EnrichedSeries>,
        categoryName: String,
        searchQuery: String
    ): List<EnrichedSeries> {
        var filtered = enrichedSeries

        // Filtrar por categoría
        if (categoryName.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == categoryName }
        }

        // Filtrar por búsqueda (incluir búsqueda en datos de TMDB)
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { enrichedSeries ->
                enrichedSeries.name.contains(searchQuery, ignoreCase = true) ||
                enrichedSeries.tmdbData?.name?.contains(searchQuery, ignoreCase = true) == true ||
                enrichedSeries.tmdbData?.originalName?.contains(searchQuery, ignoreCase = true) == true ||
                enrichedSeries.tmdbData?.overview?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        return filtered
    }
    
    private fun applyAllFiltersToEnriched(enrichedSeries: List<EnrichedSeries>, state: SeriesUiState): List<EnrichedSeries> {
        var filtered = enrichedSeries

        // Filtrar por categoría
        if (state.selectedCategory.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId == state.selectedCategory }
        }

        // Filtrar por búsqueda (incluir búsqueda en datos de TMDB)
        if (state.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { enrichedSeries ->
                enrichedSeries.name.contains(state.searchQuery, ignoreCase = true) ||
                enrichedSeries.tmdbData?.name?.contains(state.searchQuery, ignoreCase = true) == true ||
                enrichedSeries.tmdbData?.originalName?.contains(state.searchQuery, ignoreCase = true) == true ||
                enrichedSeries.tmdbData?.overview?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }

        // Filtrar por calidad
        if (state.qualityFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedSeries ->
                // Buscar en calidad original de Xtream
                val xtreamQuality = when {
                    enrichedSeries.name.contains("4K", ignoreCase = true) -> "4K"
                    enrichedSeries.name.contains("1080p", ignoreCase = true) -> "1080p"
                    enrichedSeries.name.contains("720p", ignoreCase = true) -> "720p"
                    enrichedSeries.name.contains("480p", ignoreCase = true) -> "480p"
                    else -> null
                }
                xtreamQuality?.contains(state.qualityFilter, ignoreCase = true) == true
            }
        }

        // Filtrar por año
        if (state.yearFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedSeries ->
                // Usar año de TMDB si está disponible, sino el de Xtream
                val year = enrichedSeries.tmdbData?.firstAirDate?.take(4) 
                    ?: enrichedSeries.releaseDate?.take(4)
                    ?: enrichedSeries.name.let { name ->
                        // Extraer año del nombre si no está en TMDB
                        Regex("\\b(19|20)\\d{2}\\b").find(name)?.value
                    }
                year == state.yearFilter
            }
        }

        // Filtrar por género
        if (state.genreFilter.isNotEmpty()) {
            filtered = filtered.filter { enrichedSeries ->
                // Buscar en géneros de TMDB primero, luego en Xtream
                enrichedSeries.tmdbData?.genres?.any { 
                    it.name.contains(state.genreFilter, ignoreCase = true) 
                } == true ||
                enrichedSeries.genre?.contains(state.genreFilter, ignoreCase = true) == true
            }
        }

        // Aplicar ordenamiento
        filtered = when (state.sortBy) {
            "name_asc" -> filtered.sortedBy { it.tmdbData?.name ?: it.name }
            "name_desc" -> filtered.sortedByDescending { it.tmdbData?.name ?: it.name }
            "year_asc" -> filtered.sortedBy { 
                it.tmdbData?.firstAirDate?.take(4)?.toIntOrNull() ?: 0 
            }
            "year_desc" -> filtered.sortedByDescending { 
                it.tmdbData?.firstAirDate?.take(4)?.toIntOrNull() ?: 0 
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