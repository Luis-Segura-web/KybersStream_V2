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
    val filteredMovies: List<Movie> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
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
                filteredMovies = applyFilters(allMovies, currentState.selectedCategory, query)
            )
        }
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
}