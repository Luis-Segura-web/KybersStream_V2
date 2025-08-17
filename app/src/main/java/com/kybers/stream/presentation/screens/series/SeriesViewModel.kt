package com.kybers.stream.presentation.screens.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.Category
import com.kybers.stream.domain.model.Series
import com.kybers.stream.domain.usecase.xtream.GetSeriesCategoriesUseCase
import com.kybers.stream.domain.usecase.xtream.GetSeriesStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesUiState(
    val isLoading: Boolean = false,
    val series: List<Series> = emptyList(),
    val allSeries: List<Series> = emptyList(),
    val filteredSeries: List<Series> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val qualityFilter: String = "",
    val yearFilter: String = "",
    val genreFilter: String = "",
    val sortBy: String = "",
    val error: String? = null
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val getSeriesCategoriesUseCase: GetSeriesCategoriesUseCase,
    private val getSeriesStreamsUseCase: GetSeriesStreamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    private var allSeries: List<Series> = emptyList()

    init {
        loadCategories()
        loadSeries()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getSeriesCategoriesUseCase().collect { result ->
                result.fold(
                    onSuccess = { categories ->
                        _uiState.update { it.copy(categories = categories, error = null) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }

    fun loadSeries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getSeriesStreamsUseCase().collect { result ->
                result.fold(
                    onSuccess = { series ->
                        allSeries = series
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                series = series,
                                allSeries = series,
                                filteredSeries = applyFilters(series, it.selectedCategory, it.searchQuery),
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    fun selectCategory(categoryName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = categoryName,
                filteredSeries = applyFilters(allSeries, categoryName, currentState.searchQuery)
            )
        }
    }

    fun search(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                isSearching = query.isNotEmpty(),
                filteredSeries = applyFilters(allSeries, currentState.selectedCategory, query)
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
}