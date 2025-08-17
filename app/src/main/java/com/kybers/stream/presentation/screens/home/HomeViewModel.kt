package com.kybers.stream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.data.sync.SyncManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.GetFavoritesUseCase
import com.kybers.stream.domain.usecase.playback.GetContinueWatchingUseCase
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.repository.DiscoveryRepository
import com.kybers.stream.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val favorites: List<FavoriteItem> = emptyList(),
    val continueWatching: List<PlaybackProgress> = emptyList(),
    val groupedMovies: List<GroupedTMDBContent> = emptyList(),
    val groupedSeries: List<GroupedTMDBContent> = emptyList(),
    val recentContent: List<Any> = emptyList(),
    val error: String? = null
)

data class GroupedTMDBContent(
    val tmdbId: String,
    val tmdbData: TMDBMovieData? = null,
    val tmdbSeriesData: TMDBSeriesData? = null,
    val xtreamMovies: List<Movie> = emptyList(),
    val xtreamSeries: List<Series> = emptyList(),
    val primaryTitle: String,
    val contentType: String // "movie" or "series"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    private val discoveryRepository: DiscoveryRepository,
    private val databaseCacheManager: DatabaseCacheManager,
    private val syncManager: SyncManager,
    private val tmdbUseCases: TMDBUseCases,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val discoveryData: StateFlow<DiscoveryData> = discoveryRepository.getDiscoveryDataFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DiscoveryData(emptyList(), isLoading = true)
        )

    init {
        loadHomeData()
        refreshDiscovery()
        loadGroupedContent()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Combinar favoritos y continuar viendo
                combine(
                    getFavoritesUseCase(),
                    getContinueWatchingUseCase(limit = 10)
                ) { favorites, continueWatching ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            favorites = favorites.take(10), // Limitar a 10 favoritos más recientes
                            continueWatching = continueWatching,
                            error = null
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
        loadGroupedContent()
    }

    fun refreshDiscovery() {
        viewModelScope.launch {
            try {
                discoveryRepository.refreshDiscoveryData()
            } catch (e: Exception) {
                // Error handling - could emit to a shared error state if needed
            }
        }
    }

    fun onContentItemClick(item: ContentItem) {
        when (item) {
            is ContentItem.MovieItem -> {
                // TODO: Navigate to movie detail screen
                // navigationController.navigate("movie_detail/${item.id}")
            }
            is ContentItem.SeriesItem -> {
                // TODO: Navigate to series detail screen
                // navigationController.navigate("series_detail/${item.id}")
            }
            is ContentItem.EpisodeItem -> {
                // TODO: Navigate to episode detail or start playback
                // navigationController.navigate("episode_detail/${item.id}")
            }
            is ContentItem.ContinueWatchingItem -> {
                // TODO: Resume playback from saved position
                // playerManager.resumePlayback(item.id, item.progress.positionMs)
            }
        }
    }

    fun onContentPlayClick(item: ContentItem) {
        when (item) {
            is ContentItem.MovieItem -> {
                // TODO: Start movie playback
                // playerManager.playMovie(item.id)
            }
            is ContentItem.SeriesItem -> {
                // TODO: Play first episode or last watched episode
                // playerManager.playSeriesFromBeginning(item.id)
            }
            is ContentItem.EpisodeItem -> {
                // TODO: Start episode playback
                // playerManager.playEpisode(item.id)
            }
            is ContentItem.ContinueWatchingItem -> {
                // Resume from current position
                // playerManager.resumePlayback(item.id, item.progress.positionMs)
            }
        }
    }

    fun onCategoryClick(categoryId: String) {
        // TODO: Navigate to category view
        // navigationController.navigate("category/$categoryId")
    }
    
    private fun loadGroupedContent() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser().first() ?: return@launch
                val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
                
                // Verificar si el cache de Xtream es válido
                if (!databaseCacheManager.isXtreamCacheValid(userHash)) {
                    // Si no es válido, intentar sincronizar
                    syncManager.performInitialSync()
                }
                
                // Obtener contenido de Xtream desde cache
                val cachedMovies = databaseCacheManager.getCachedXtreamMovies(userHash)
                val cachedSeries = databaseCacheManager.getCachedXtreamSeries(userHash)
                
                // Agrupar películas por TMDB ID
                val groupedMovies = groupMoviesByTMDBId(cachedMovies)
                
                // Agrupar series por TMDB ID
                val groupedSeries = groupSeriesByTMDBId(cachedSeries)
                
                _uiState.update { currentState ->
                    currentState.copy(
                        groupedMovies = groupedMovies,
                        groupedSeries = groupedSeries
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(error = "Error cargando contenido agrupado: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun groupMoviesByTMDBId(movies: List<Movie>): List<GroupedTMDBContent> {
        return movies
            .filter { !it.tmdbId.isNullOrEmpty() }
            .groupBy { it.tmdbId!! }
            .map { (tmdbId, movieGroup) ->
                // Intentar obtener datos de TMDB para este grupo
                val tmdbData = tmdbUseCases.getMovieDetails(tmdbId).getOrNull()
                
                GroupedTMDBContent(
                    tmdbId = tmdbId,
                    tmdbData = tmdbData,
                    xtreamMovies = movieGroup,
                    primaryTitle = tmdbData?.title ?: movieGroup.first().name,
                    contentType = "movie"
                )
            }
            .sortedByDescending { group ->
                // Ordenar por fecha de agregado más reciente en Xtream
                group.xtreamMovies.maxOfOrNull { it.addedTimestamp } ?: 0L
            }
            .take(20) // Limitar a 20 grupos para el Home
    }
    
    private suspend fun groupSeriesByTMDBId(series: List<Series>): List<GroupedTMDBContent> {
        return series
            .filter { !it.tmdbId.isNullOrEmpty() }
            .groupBy { it.tmdbId!! }
            .map { (tmdbId, seriesGroup) ->
                // Intentar obtener datos de TMDB para este grupo
                val tmdbData = tmdbUseCases.getSeriesDetails(tmdbId).getOrNull()
                
                GroupedTMDBContent(
                    tmdbId = tmdbId,
                    tmdbSeriesData = tmdbData,
                    xtreamSeries = seriesGroup,
                    primaryTitle = tmdbData?.name ?: seriesGroup.first().name,
                    contentType = "series"
                )
            }
            .sortedByDescending { group ->
                // Ordenar por fecha de modificación más reciente en Xtream
                group.xtreamSeries.maxOfOrNull { it.lastModified } ?: 0L
            }
            .take(20) // Limitar a 20 grupos para el Home
    }
    
    fun onGroupedContentClick(groupedContent: GroupedTMDBContent) {
        when (groupedContent.contentType) {
            "movie" -> {
                // Si hay múltiples películas con el mismo TMDB ID, mostrar opciones
                if (groupedContent.xtreamMovies.size > 1) {
                    // TODO: Navigate to selection screen
                    // navigationController.navigate("movie_selection/${groupedContent.tmdbId}")
                } else {
                    // TODO: Navigate directly to movie detail
                    // navigationController.navigate("movie_detail/${groupedContent.xtreamMovies.first().streamId}")
                }
            }
            "series" -> {
                // Si hay múltiples series con el mismo TMDB ID, mostrar opciones
                if (groupedContent.xtreamSeries.size > 1) {
                    // TODO: Navigate to selection screen
                    // navigationController.navigate("series_selection/${groupedContent.tmdbId}")
                } else {
                    // TODO: Navigate directly to series detail
                    // navigationController.navigate("series_detail/${groupedContent.xtreamSeries.first().seriesId}")
                }
            }
        }
    }
}