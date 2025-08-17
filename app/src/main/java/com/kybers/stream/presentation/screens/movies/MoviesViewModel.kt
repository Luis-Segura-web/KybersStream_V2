package com.kybers.stream.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.Category
import com.kybers.stream.domain.model.Movie
import com.kybers.stream.domain.usecase.xtream.GetVodCategoriesUseCase
import com.kybers.stream.domain.usecase.xtream.GetVodStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val allMovies: List<Movie> = emptyList(),
    val filteredMovies: List<Movie> = emptyList(),
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
class MoviesViewModel @Inject constructor(
    private val getVodCategoriesUseCase: GetVodCategoriesUseCase,
    private val getVodStreamsUseCase: GetVodStreamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private var allMovies: List<Movie> = emptyList()

    init {
        loadCategories()
        loadMovies()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getVodCategoriesUseCase().collect { result ->
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

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getVodStreamsUseCase().collect { result ->
                result.fold(
                    onSuccess = { movies ->
                        allMovies = movies
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                movies = movies,
                                allMovies = movies,
                                filteredMovies = applyFilters(movies, it.selectedCategory, it.searchQuery),
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
                filteredMovies = applyFilters(allMovies, categoryName, currentState.searchQuery)
            )
        }
    }

    fun search(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                isSearching = query.isNotEmpty(),
                filteredMovies = applyFilters(allMovies, currentState.selectedCategory, query)
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
}