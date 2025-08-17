package com.kybers.stream.presentation.screens.seriesdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.AddFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.RemoveFavoriteUseCase
import com.kybers.stream.domain.usecase.favorites.IsFavoriteUseCase
import com.kybers.stream.domain.usecase.TMDBUseCases
import com.kybers.stream.domain.repository.XtreamRepository
import com.kybers.stream.data.cache.DatabaseCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesDetailUiState(
    val isLoading: Boolean = false,
    val seriesDetail: SeriesDetail? = null,
    val enrichedSeriesData: EnrichedSeries? = null,
    val isLoadingTMDB: Boolean = false,
    val tmdbError: String? = null,
    val error: String? = null
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val tmdbUseCases: TMDBUseCases,
    private val xtreamRepository: XtreamRepository,
    private val databaseCacheManager: DatabaseCacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    private val _seriesId = MutableStateFlow("")
    private val seriesId: StateFlow<String> = _seriesId.asStateFlow()

    private val _selectedSeason = MutableStateFlow<Season?>(null)
    val selectedSeason: StateFlow<Season?> = _selectedSeason.asStateFlow()

    val isFavorite: StateFlow<Boolean> = seriesId
        .filter { it.isNotEmpty() }
        .flatMapLatest { id ->
            isFavoriteUseCase(id, ContentType.SERIES)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun loadSeriesDetail(seriesId: String) {
        _seriesId.value = seriesId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener información básica de la serie desde Xtream
                when (val result = xtreamRepository.getSeriesInfo(seriesId)) {
                    is XtreamResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                seriesDetail = result.data,
                                error = null
                            )
                        }
                        
                        // Set first season as selected by default
                        if (result.data.seasons.isNotEmpty()) {
                            _selectedSeason.value = result.data.seasons.first()
                        }
                        
                        // Buscar datos TMDB si existe tmdb_id
                        val tmdbId = result.data.tmdbRating // tmdbRating contiene el tmdb_id
                        if (!tmdbId.isNullOrEmpty()) {
                            loadTMDBData(seriesId, tmdbId)
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
    
    private fun loadTMDBData(seriesId: String, tmdbId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTMDB = true, tmdbError = null) }
            
            try {
                // Verificar caché local primero
                val cachedData = databaseCacheManager.getCachedTMDBSeries(tmdbId)
                if (cachedData != null && databaseCacheManager.isTMDBCacheValid(tmdbId)) {
                    // Usar datos del caché
                    val currentDetail = _uiState.value.seriesDetail
                    if (currentDetail != null) {
                        _uiState.update { 
                            it.copy(
                                isLoadingTMDB = false,
                                enrichedSeriesData = EnrichedSeries(
                                    seriesId = currentDetail.seriesId,
                                    name = currentDetail.name,
                                    cover = currentDetail.poster,
                                    categoryId = "", // Se podría obtener del contexto si es necesario
                                    plot = currentDetail.plot,
                                    cast = currentDetail.cast,
                                    director = currentDetail.director,
                                    genre = currentDetail.genre,
                                    releaseDate = currentDetail.releaseDate,
                                    lastModified = System.currentTimeMillis(),
                                    rating = currentDetail.rating,
                                    rating5Based = currentDetail.rating?.toDoubleOrNull() ?: 0.0,
                                    backdropPath = emptyList(),
                                    youtubeTrailer = null,
                                    episodeRunTime = null,
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
                val tmdbResult = tmdbUseCases.getSeriesDetails(tmdbId)
                if (tmdbResult.isSuccess) {
                    val tmdbData = tmdbResult.getOrNull()
                    if (tmdbData != null) {
                        // Guardar en caché
                        databaseCacheManager.cacheTMDBSeries(tmdbData, tmdbId)
                        
                        val currentDetail = _uiState.value.seriesDetail
                        if (currentDetail != null) {
                            _uiState.update { 
                                it.copy(
                                    isLoadingTMDB = false,
                                    enrichedSeriesData = EnrichedSeries(
                                        seriesId = currentDetail.seriesId,
                                        name = currentDetail.name,
                                        cover = currentDetail.poster,
                                        categoryId = "", // Se podría obtener del contexto si es necesario
                                        plot = currentDetail.plot,
                                        cast = currentDetail.cast,
                                        director = currentDetail.director,
                                        genre = currentDetail.genre,
                                        releaseDate = currentDetail.releaseDate,
                                        lastModified = System.currentTimeMillis(),
                                        rating = currentDetail.rating,
                                        rating5Based = currentDetail.rating?.toDoubleOrNull() ?: 0.0,
                                        backdropPath = emptyList(),
                                        youtubeTrailer = null,
                                        episodeRunTime = null,
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
                            tmdbError = "Error cargando datos TMDB: ${tmdbResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoadingTMDB = false,
                        tmdbError = "Error inesperado cargando TMDB: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectSeason(season: Season) {
        _selectedSeason.value = season
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val series = _uiState.value.seriesDetail ?: return@launch
            val currentlyFavorite = isFavorite.value
            
            try {
                if (currentlyFavorite) {
                    removeFavoriteUseCase(series.id, ContentType.SERIES)
                } else {
                    val favoriteItem = FavoriteItem(
                        contentId = series.id,
                        contentType = ContentType.SERIES,
                        name = series.name,
                        imageUrl = series.poster,
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

    private fun createMockSeriesDetail(seriesId: String): SeriesDetail {
        // Mock data - in real implementation, this would come from API
        val mockEpisodes1 = listOf(
            Episode(
                id = "${seriesId}_s1e1",
                episodeNumber = 1,
                seasonNumber = 1,
                name = "Episodio Piloto",
                overview = "El episodio que inicia toda la aventura. Los personajes principales se conocen y comienza su viaje épico.",
                runtime = "45",
                airDate = "2023-01-15",
                still = "https://example.com/episode1.jpg"
            ),
            Episode(
                id = "${seriesId}_s1e2",
                episodeNumber = 2,
                seasonNumber = 1,
                name = "El Despertar",
                overview = "Los protagonistas descubren sus verdaderos poderes mientras enfrentan su primera gran amenaza.",
                runtime = "42",
                airDate = "2023-01-22",
                still = "https://example.com/episode2.jpg"
            ),
            Episode(
                id = "${seriesId}_s1e3",
                episodeNumber = 3,
                seasonNumber = 1,
                name = "Alianzas Inesperadas",
                overview = "Enemigos se convierten en aliados cuando una amenaza mayor aparece en el horizonte.",
                runtime = "48",
                airDate = "2023-01-29",
                still = "https://example.com/episode3.jpg"
            )
        )
        
        val mockEpisodes2 = listOf(
            Episode(
                id = "${seriesId}_s2e1",
                episodeNumber = 1,
                seasonNumber = 2,
                name = "Nuevo Comienzo",
                overview = "La segunda temporada comienza con nuestros héroes enfrentando las consecuencias de la temporada anterior.",
                runtime = "50",
                airDate = "2023-09-10",
                still = "https://example.com/s2episode1.jpg"
            ),
            Episode(
                id = "${seriesId}_s2e2",
                episodeNumber = 2,
                seasonNumber = 2,
                name = "El Retorno",
                overview = "Un personaje querido regresa de manera inesperada, cambiando toda la dinámica del grupo.",
                runtime = "47",
                airDate = "2023-09-17",
                still = "https://example.com/s2episode2.jpg"
            )
        )
        
        val seasons = listOf(
            Season(
                seasonNumber = 1,
                name = "Primera Temporada",
                overview = "La temporada que inició todo. Los orígenes de nuestros héroes y su primer gran desafío.",
                airDate = "2023-01-15",
                episodes = mockEpisodes1,
                episodeCount = mockEpisodes1.size
            ),
            Season(
                seasonNumber = 2,
                name = "Segunda Temporada",
                overview = "La aventura continúa con nuevos desafíos y revelaciones impactantes.",
                airDate = "2023-09-10",
                episodes = mockEpisodes2,
                episodeCount = mockEpisodes2.size
            )
        )
        
        return SeriesDetail(
            id = seriesId,
            name = "Serie Épica de Fantasía",
            seriesId = seriesId,
            year = "2023",
            rating = "9.2",
            genre = "Fantasía, Drama, Aventura",
            plot = "Una serie épica que sigue las aventuras de un grupo de héroes en un mundo mágico lleno de peligros y maravillas. Con una narrativa compleja y personajes profundamente desarrollados, cada episodio revela nuevos misterios mientras los protagonistas enfrentan amenazas cada vez mayores.",
            cast = "Protagonista Principal, Heroína Valiente, Mentor Sabio, Villano Complejo, Personaje Misterioso",
            director = "Director Visionario",
            poster = "https://example.com/series_poster.jpg",
            backdrop = "https://example.com/series_backdrop.jpg",
            imdbRating = "9.2",
            language = "Español",
            country = "España",
            releaseDate = "2023-01-15",
            seasons = seasons,
            totalSeasons = seasons.size,
            totalEpisodes = seasons.sumOf { it.episodeCount },
            status = "Continuing"
        )
    }
}