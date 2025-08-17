package com.kybers.stream.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kybers.stream.domain.model.EnrichedMovie
import com.kybers.stream.domain.model.EnrichedSeries

/**
 * Ejemplo de cómo usar los componentes mejorados con TMDB
 * Este archivo muestra la implementación recomendada para mostrar
 * películas y series con información enriquecida de TMDB
 */

@Composable
fun TMDBEnhancedMoviesGrid(
    movies: List<EnrichedMovie>,
    useTMDBData: Boolean,
    isLoadingTMDBData: Boolean,
    onMovieClick: (EnrichedMovie) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onToggleTMDBData: () -> Unit,
    getFavoriteStatus: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Toggle para activar/desactivar datos TMDB
        TMDBDataToggle(
            useTMDBData = useTMDBData,
            onToggle = onToggleTMDBData,
            isLoading = isLoadingTMDBData,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Grid de películas
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                TMDBEnhancedMovieCard(
                    movie = movie,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick,
                    isFavorite = getFavoriteStatus(movie.streamId),
                    showTMDBData = useTMDBData
                )
            }
        }
    }
}

@Composable
fun TMDBEnhancedSeriesGrid(
    series: List<EnrichedSeries>,
    useTMDBData: Boolean,
    isLoadingTMDBData: Boolean,
    onSeriesClick: (EnrichedSeries) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onToggleTMDBData: () -> Unit,
    getFavoriteStatus: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Toggle para activar/desactivar datos TMDB
        TMDBDataToggle(
            useTMDBData = useTMDBData,
            onToggle = onToggleTMDBData,
            isLoading = isLoadingTMDBData,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Grid de series
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(series) { serie ->
                TMDBEnhancedSeriesCard(
                    series = serie,
                    onSeriesClick = onSeriesClick,
                    onFavoriteClick = onFavoriteClick,
                    isFavorite = getFavoriteStatus(serie.seriesId),
                    showTMDBData = useTMDBData
                )
            }
        }
    }
}

/**
 * Ejemplo de cómo integrar en un ViewModel
 * 
 * En tu MoviesViewModel o SeriesViewModel, puedes usar así:
 * 
 * @Composable
 * fun MoviesScreen(viewModel: MoviesViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsState()
 *     
 *     TMDBEnhancedMoviesGrid(
 *         movies = if (uiState.useTMDBData) uiState.filteredEnrichedMovies else 
 *                  uiState.filteredMovies.map { EnrichedMovie from it },
 *         useTMDBData = uiState.useTMDBData,
 *         isLoadingTMDBData = uiState.isEnrichingWithTMDB,
 *         onMovieClick = { movie -> 
 *             // Navegar a detalle de película
 *         },
 *         onFavoriteClick = { movieId -> 
 *             viewModel.toggleFavorite(movieId)
 *         },
 *         onToggleTMDBData = {
 *             viewModel.toggleTMDBData()
 *         },
 *         getFavoriteStatus = { movieId ->
 *             // Retornar si la película está en favoritos
 *             false
 *         }
 *     )
 * }
 */

/**
 * Funciones de extensión para convertir entre tipos básicos y enriquecidos
 * Útiles cuando TMDB está desactivado pero queremos usar el mismo componente
 */

fun com.kybers.stream.domain.model.Movie.toEnriched(): EnrichedMovie {
    return EnrichedMovie(
        streamId = streamId,
        name = name,
        icon = icon,
        categoryId = categoryId,
        rating = rating,
        rating5Based = rating5Based,
        addedTimestamp = addedTimestamp,
        isAdult = isAdult,
        containerExtension = containerExtension,
        tmdbId = tmdbId,
        tmdbData = null
    )
}

fun com.kybers.stream.domain.model.Series.toEnriched(): EnrichedSeries {
    return EnrichedSeries(
        seriesId = seriesId,
        name = name,
        cover = cover,
        categoryId = categoryId,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        lastModified = lastModified,
        rating = rating,
        rating5Based = rating5Based,
        backdropPath = backdropPath,
        youtubeTrailer = youtubeTrailer,
        episodeRunTime = episodeRunTime,
        tmdbId = tmdbId,
        tmdbData = null
    )
}