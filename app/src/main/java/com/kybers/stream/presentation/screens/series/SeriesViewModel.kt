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
    val filteredSeries: List<Series> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
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
                filteredSeries = applyFilters(allSeries, currentState.selectedCategory, query)
            )
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
}