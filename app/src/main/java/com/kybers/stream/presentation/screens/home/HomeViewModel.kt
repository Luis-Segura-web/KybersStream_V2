package com.kybers.stream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.data.sync.SyncManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.GetFavoritesUseCase
import com.kybers.stream.domain.usecase.playback.GetContinueWatchingUseCase
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val favorites: List<FavoriteItem> = emptyList(),
    val continueWatching: List<PlaybackProgress> = emptyList(),
    val recentMovies: List<Movie> = emptyList(),
    val recentSeries: List<Series> = emptyList(),
    val recentContent: List<Any> = emptyList(),
    val tmdbFilteredContent: TMDBFilteredContent? = null,
    val tmdbContent: TMDBContent = TMDBContent(),
    val isLoadingTMDB: Boolean = false,
    val tmdbError: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    private val databaseCacheManager: DatabaseCacheManager,
    private val syncManager: SyncManager,
    private val tmdbUseCases: TMDBUseCases,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        loadRecentContent()
        loadTMDBContent()
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
        loadRecentContent()
        loadTMDBContent()
    }
    
    private fun loadRecentContent() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser().first() ?: return@launch
                val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
                
                // Verificar si el cache de Xtream es válido
                val isCacheValid = databaseCacheManager.isXtreamCacheValid(userHash)
                
                if (!isCacheValid) {
                    try {
                        // Si no es válido, intentar sincronizar
                        syncManager.performInitialSync()
                        // Dar un momento para que se complete la sincronización
                        kotlinx.coroutines.delay(2000)
                    } catch (syncError: Exception) {
                        // Continuar de todos modos para ver si hay datos antiguos
                    }
                }
                
                // Obtener contenido reciente de Xtream desde cache
                val allMovies = databaseCacheManager.getCachedXtreamMovies(userHash)
                val allSeries = databaseCacheManager.getCachedXtreamSeries(userHash)
                
                // Si hay pocos elementos con timestamp válido, usar los primeros elementos
                val recentMovies = if (allMovies.any { it.addedTimestamp > 0 }) {
                    allMovies.sortedByDescending { it.addedTimestamp }.take(20)
                } else {
                    // Fallback: usar los primeros 20 si no hay timestamps válidos
                    allMovies.take(20)
                }
                
                val recentSeries = if (allSeries.any { it.lastModified > 0 }) {
                    allSeries.sortedByDescending { it.lastModified }.take(20)
                } else {
                    // Fallback: usar los primeros 20 si no hay timestamps válidos  
                    allSeries.take(20)
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        recentMovies = recentMovies,
                        recentSeries = recentSeries
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(error = "Error cargando contenido reciente: ${e.message}")
                }
            }
        }
    }
    
    private fun loadTMDBContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTMDB = true, tmdbError = null) }
            
            try {
                val user = userRepository.getCurrentUser().first() ?: return@launch
                val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
                
                // Obtener contenido TMDB filtrado con el disponible en Xtream
                tmdbUseCases.getFilteredTMDBContent(userHash)
                    .onSuccess { filteredContent ->
                        // También obtener contenido TMDB completo para el HeroCarousel
                        tmdbUseCases.getAllTMDBContent()
                            .onSuccess { tmdbContent ->
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isLoadingTMDB = false,
                                        tmdbFilteredContent = filteredContent,
                                        tmdbContent = tmdbContent,
                                        tmdbError = null
                                    )
                                }
                            }
                            .onFailure { error ->
                                // Si falla obtener contenido completo, al menos usar el filtrado
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isLoadingTMDB = false,
                                        tmdbFilteredContent = filteredContent,
                                        tmdbError = null
                                    )
                                }
                            }
                    }
                    .onFailure { error ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoadingTMDB = false,
                                tmdbError = error.message ?: "Error cargando contenido TMDB"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoadingTMDB = false,
                        tmdbError = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refreshTMDBContent() {
        loadTMDBContent()
    }
}