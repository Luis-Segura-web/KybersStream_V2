package com.kybers.stream.presentation.screens.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.AddFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.RemoveFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.IsFavoriteUseCase
import com.kybers.stream.domain.usecase.playback.GetPlaybackProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val error: String? = null
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val getPlaybackProgressUseCase: GetPlaybackProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _movieId = MutableStateFlow("")
    private val movieId: StateFlow<String> = _movieId.asStateFlow()

    val isFavorite: StateFlow<Boolean> = movieId
        .filter { it.isNotEmpty() }
        .flatMapLatest { id ->
            isFavoriteUseCase(id, ContentType.VOD)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val playbackProgress: StateFlow<PlaybackProgress?> = movieId
        .filter { it.isNotEmpty() }
        .flatMapLatest { id ->
            getPlaybackProgressUseCase(id, ContentType.VOD)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadMovieDetail(movieId: String) {
        _movieId.value = movieId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // TODO: Implement actual API call to get movie details
                // For now, create a mock movie detail
                val mockMovie = createMockMovieDetail(movieId)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        movieDetail = mockMovie,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar detalles: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val movie = _uiState.value.movieDetail ?: return@launch
            val currentlyFavorite = isFavorite.value
            
            try {
                if (currentlyFavorite) {
                    removeFavoriteUseCase(movie.id, ContentType.VOD)
                } else {
                    val favoriteItem = FavoriteItem(
                        contentId = movie.id,
                        contentType = ContentType.VOD,
                        name = movie.name,
                        imageUrl = movie.poster,
                        categoryId = null,
                        addedTimestamp = System.currentTimeMillis()
                    )
                    addFavoriteUseCase(favoriteItem)
                }
            } catch (e: Exception) {
                // Handle error - could show a snackbar
            }
        }
    }

    private fun createMockMovieDetail(movieId: String): MovieDetail {
        // Mock data - in real implementation, this would come from API
        return MovieDetail(
            id = movieId,
            name = "Película de Ejemplo",
            streamId = movieId,
            year = "2023",
            rating = "8.5",
            duration = "145",
            quality = "4K UHD",
            genre = "Acción, Aventura, Ciencia Ficción",
            plot = "Una épica aventura de ciencia ficción que sigue a un grupo de héroes mientras luchan contra fuerzas cósmicas para salvar la galaxia. Con efectos visuales impresionantes y una narrativa envolvente, esta película redefine el género.",
            cast = "Actor Principal, Actriz Protagonista, Actor Secundario, Villano Épico",
            director = "Director Famoso",
            poster = "https://example.com/poster.jpg",
            backdrop = "https://example.com/backdrop.jpg",
            imdbRating = "8.5",
            language = "Español",
            country = "Estados Unidos",
            releaseDate = "2023-05-15"
        )
    }
}