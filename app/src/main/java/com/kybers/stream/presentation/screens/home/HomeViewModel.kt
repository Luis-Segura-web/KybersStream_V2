package com.kybers.stream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.GetFavoritesUseCase
import com.kybers.stream.domain.usecase.playback.GetContinueWatchingUseCase
import com.kybers.stream.domain.repository.DiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val favorites: List<FavoriteItem> = emptyList(),
    val continueWatching: List<PlaybackProgress> = emptyList(),
    val recentContent: List<Any> = emptyList(), // Puede ser Channel, Movie, Series
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    private val discoveryRepository: DiscoveryRepository
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
}