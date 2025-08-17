package com.kybers.stream.presentation.screens.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.AddFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.RemoveFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.IsFavoriteUseCase
import com.kybers.stream.domain.usecase.playback.GetPlaybackProgressUseCase
import com.kybers.stream.domain.manager.PlaybackManager
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.repository.XtreamRepository
import com.kybers.stream.data.cache.DatabaseCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val enrichedMovieData: EnrichedMovie? = null,
    val isLoadingTMDB: Boolean = false,
    val tmdbError: String? = null,
    val error: String? = null
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val getPlaybackProgressUseCase: GetPlaybackProgressUseCase,
    private val playbackManager: PlaybackManager,
    private val tmdbUseCases: TMDBUseCases,
    private val xtreamRepository: XtreamRepository,
    private val databaseCacheManager: DatabaseCacheManager
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

    // Expose playback manager properties
    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    val currentMedia: StateFlow<MediaInfo?> = playbackManager.currentMedia
    
    // Expose playback manager for direct access
    fun getPlaybackManager(): PlaybackManager = playbackManager

    fun loadMovieDetail(movieId: String) {
        _movieId.value = movieId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener información básica de la película desde Xtream
                when (val result = xtreamRepository.getVodInfo(movieId)) {
                    is XtreamResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                movieDetail = result.data,
                                error = null
                            )
                        }
                        
                        // Buscar datos TMDB si existe tmdb_id
                        val tmdbId = result.data.tmdbRating // tmdbRating contiene el tmdb_id
                        if (!tmdbId.isNullOrEmpty()) {
                            loadTMDBData(movieId, tmdbId)
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
                        // Loading state is already handled above
                    }
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
    
    private fun loadTMDBData(movieId: String, tmdbId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTMDB = true, tmdbError = null) }
            
            try {
                // Verificar caché local primero
                val cachedData = databaseCacheManager.getCachedTMDBMovie(tmdbId)
                if (cachedData != null && databaseCacheManager.isTMDBCacheValid(tmdbId)) {
                    // Usar datos del caché
                    val currentDetail = _uiState.value.movieDetail
                    if (currentDetail != null) {
                        _uiState.update { 
                            it.copy(
                                isLoadingTMDB = false,
                                enrichedMovieData = EnrichedMovie(
                                    streamId = currentDetail.streamId,
                                    name = currentDetail.name,
                                    icon = currentDetail.poster,
                                    categoryId = "", // Se podría obtener del contexto si es necesario
                                    rating = currentDetail.rating,
                                    rating5Based = currentDetail.rating?.toDoubleOrNull() ?: 0.0,
                                    addedTimestamp = 0L,
                                    isAdult = false,
                                    containerExtension = null,
                                    tmdbId = tmdbId,
                                    tmdbData = cachedData
                                ),
                                tmdbError = null
                            )
                        }
                    }
                    return@launch
                }
                
                // Si no hay caché válido, obtener de TMDB API
                val tmdbResult = tmdbUseCases.getMovieDetails(tmdbId)
                if (tmdbResult.isSuccess) {
                    val tmdbData = tmdbResult.getOrNull()
                    if (tmdbData != null) {
                        // Guardar en caché
                        databaseCacheManager.cacheTMDBMovie(tmdbData, tmdbId)
                        
                        val currentDetail = _uiState.value.movieDetail
                        if (currentDetail != null) {
                            _uiState.update { 
                                it.copy(
                                    isLoadingTMDB = false,
                                    enrichedMovieData = EnrichedMovie(
                                        streamId = currentDetail.streamId,
                                        name = currentDetail.name,
                                        icon = currentDetail.poster,
                                        categoryId = "", // Se podría obtener del contexto si es necesario
                                        rating = currentDetail.rating,
                                        rating5Based = currentDetail.rating?.toDoubleOrNull() ?: 0.0,
                                        addedTimestamp = 0L,
                                        isAdult = false,
                                        containerExtension = null,
                                        tmdbId = tmdbId,
                                        tmdbData = tmdbData
                                    ),
                                    tmdbError = null
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoadingTMDB = false,
                            tmdbError = null // No mostrar error TMDB como error crítico
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoadingTMDB = false,
                        tmdbError = null // No mostrar error TMDB como error crítico
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