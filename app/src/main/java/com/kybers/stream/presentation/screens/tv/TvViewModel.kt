package com.kybers.stream.presentation.screens.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.kybers.stream.domain.manager.PlaybackManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.preferences.GetUserPreferencesUseCase
import com.kybers.stream.domain.usecase.preferences.UpdateUserPreferencesUseCase
import com.kybers.stream.domain.usecase.search.SearchContentUseCase
import com.kybers.stream.domain.usecase.search.SearchScope
import com.kybers.stream.domain.usecase.xtream.*
import com.kybers.stream.domain.usecase.epg.GetChannelEpgUseCase
import com.kybers.stream.domain.usecase.epg.RefreshEpgUseCase
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.presentation.components.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TvUiState(
    val isLoading: Boolean = false,
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST,
    val error: String? = null,
    val channelsEpg: Map<String, ChannelEpg> = emptyMap(),
    val isLoadingEpg: Boolean = false,
    val epgError: String? = null
)

@HiltViewModel
class TvViewModel @Inject constructor(
    private val getLiveCategoriesUseCase: GetLiveCategoriesUseCase,
    private val getLiveStreamsUseCase: GetLiveStreamsUseCase,
    private val searchContentUseCase: SearchContentUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val playbackManager: PlaybackManager,
    private val getChannelEpgUseCase: GetChannelEpgUseCase,
    private val refreshEpgUseCase: RefreshEpgUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    val playbackState = playbackManager.playbackState
    val currentMedia = playbackManager.currentMedia

    private var allChannels: List<Channel> = emptyList()

    init {
        loadUserPreferences()
        loadCategories()
        loadChannels()
        refreshEpg()
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { preferences ->
                val viewMode = when (preferences.viewModeTv) {
                    "grid" -> ViewMode.GRID
                    else -> ViewMode.LIST
                }
                _uiState.update { it.copy(viewMode = viewMode) }
            }
        }
    }

    fun getExoPlayer(): ExoPlayer = playbackManager.exoPlayer

    private fun loadCategories() {
        viewModelScope.launch {
            getLiveCategoriesUseCase().collect { result ->
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

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getLiveStreamsUseCase().collect { result ->
                result.fold(
                    onSuccess = { channels ->
                        allChannels = channels
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                channels = channels,
                                filteredChannels = applyFilters(channels, it.selectedCategory, it.searchQuery),
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
                filteredChannels = applyFilters(allChannels, categoryName, currentState.searchQuery)
            )
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            // Si no hay query, mostrar todos los canales filtrados por categoría
            _uiState.update { currentState ->
                currentState.copy(
                    filteredChannels = applyFilters(allChannels, currentState.selectedCategory, ""),
                    isSearching = false
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            searchContentUseCase(query, SearchScope.LIVE_TV, _uiState.value.selectedCategory.takeIf { it.isNotEmpty() })
                .collect { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            val channels = when (searchResult) {
                                is com.kybers.stream.domain.usecase.search.SearchResult.Channels -> searchResult.channels
                                is com.kybers.stream.domain.usecase.search.SearchResult.Mixed -> searchResult.channels
                                else -> emptyList()
                            }
                            _uiState.update { 
                                it.copy(
                                    filteredChannels = channels,
                                    isSearching = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isSearching = false,
                                    error = "Error en búsqueda: ${error.message}"
                                )
                            }
                        }
                    )
                }
        }
    }

    private fun applyFilters(
        channels: List<Channel>,
        categoryName: String,
        searchQuery: String
    ): List<Channel> {
        var filtered = channels

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

    fun playChannel(channel: Channel) {
        viewModelScope.launch {
            try {
                val mediaInfo = MediaInfo(
                    id = channel.streamId,
                    title = channel.name,
                    description = "Canal de TV en vivo",
                    artworkUri = channel.icon,
                    mediaUri = buildLiveStreamUrl(channel),
                    mediaType = MediaType.LIVE_TV
                )

                // Implementar regla de una sola conexión
                playbackManager.switchMedia(mediaInfo)
                playbackManager.play()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al reproducir canal: ${e.message}") }
            }
        }
    }

    private suspend fun buildLiveStreamUrl(channel: Channel): String {
        return try {
            // Obtener el perfil de usuario actual de forma síncrona
            val currentUser = userRepository.getCurrentUser().first()
            
            if (currentUser != null && currentUser.server.isNotEmpty() && 
                currentUser.username.isNotEmpty() && currentUser.password.isNotEmpty()) {
                
                // Construir URL del stream usando el formato de Xtream Codes
                // Formato para HLS: http://server:port/username/password/streamId.m3u8
                // Formato para TS: http://server:port/username/password/streamId
                val baseUrl = currentUser.server.removeSuffix("/")
                
                // Por defecto usar HLS (.m3u8) que es más estable para TV en vivo
                val streamUrl = "${baseUrl}/${currentUser.username}/${currentUser.password}/${channel.streamId}.m3u8"
                
                // Log para debugging (remover en producción)
                println("DEBUG: Construyendo URL de stream: $streamUrl")
                
                streamUrl
            } else {
                // Si no hay credenciales válidas, lanzar error específico
                throw IllegalStateException("No hay usuario autenticado. Por favor, inicie sesión nuevamente.")
            }
        } catch (e: Exception) {
            // Propagar el error con mensaje específico
            when (e) {
                is IllegalStateException -> throw e
                else -> throw IllegalStateException("Error al obtener credenciales de usuario: ${e.message}", e)
            }
        }
    }
    
    private suspend fun getAlternativeStreamUrl(channel: Channel): String? {
        return try {
            val currentUser = userRepository.getCurrentUser().first()
            if (currentUser != null) {
                val baseUrl = currentUser.server.removeSuffix("/")
                // Intentar con formato TS sin extensión
                "${baseUrl}/${currentUser.username}/${currentUser.password}/${channel.streamId}"
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun toggleFavorite(channel: Channel) {
        // TODO: Implementar gestión de favoritos
        viewModelScope.launch {
            // Guardar/quitar favorito usando DataStore
        }
    }

    fun stopPlayback() {
        playbackManager.stop()
    }

    fun pausePlayback() {
        playbackManager.pause()
    }

    fun resumePlayback() {
        playbackManager.play()
    }
    
    fun changeViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
        
        viewModelScope.launch {
            val modeString = when (viewMode) {
                ViewMode.GRID -> "grid"
                ViewMode.LIST -> "list"
            }
            updateUserPreferencesUseCase.updateViewMode("tv", modeString)
        }
    }
    
    // EPG Methods
    fun refreshEpg() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEpg = true, epgError = null) }
            
            refreshEpgUseCase().fold(
                onSuccess = { epgData ->
                    loadChannelsEpg()
                    _uiState.update { it.copy(isLoadingEpg = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoadingEpg = false, 
                            epgError = "Error al cargar EPG: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    private fun loadChannelsEpg() {
        viewModelScope.launch {
            val channelIds = _uiState.value.filteredChannels.map { it.streamId }
            val epgMap = mutableMapOf<String, ChannelEpg>()
            
            channelIds.forEach { streamId ->
                getChannelEpgUseCase(streamId).fold(
                    onSuccess = { channelEpg ->
                        epgMap[streamId] = channelEpg.copy(channelName = 
                            _uiState.value.filteredChannels.find { it.streamId == streamId }?.name ?: ""
                        )
                    },
                    onFailure = { /* Log error but continue with other channels */ }
                )
            }
            
            _uiState.update { it.copy(channelsEpg = epgMap) }
        }
    }
    
    fun getChannelEpg(streamId: String): ChannelEpg? {
        return _uiState.value.channelsEpg[streamId]
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}