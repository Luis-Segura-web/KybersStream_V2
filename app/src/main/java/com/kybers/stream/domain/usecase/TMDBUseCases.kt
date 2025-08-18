package com.kybers.stream.domain.usecase

import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.TMDBRepository
import javax.inject.Inject

data class TMDBUseCases(
    val enrichMovie: EnrichMovieUseCase,
    val enrichSeries: EnrichSeriesUseCase,
    val enrichMoviesList: EnrichMoviesListUseCase,
    val enrichSeriesList: EnrichSeriesListUseCase,
    val getMovieDetails: GetMovieDetailsUseCase,
    val getSeriesDetails: GetSeriesDetailsUseCase,
    val getPopularMovies: GetPopularMoviesUseCase,
    val getTrendingMovies: GetTrendingMoviesUseCase,
    val getTopRatedMovies: GetTopRatedMoviesUseCase,
    val getPopularSeries: GetPopularSeriesUseCase,
    val getTrendingSeries: GetTrendingSeriesUseCase,
    val getTopRatedSeries: GetTopRatedSeriesUseCase,
    val getFilteredTMDBContent: GetFilteredTMDBContentUseCase
)

class EnrichMovieUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(movie: Movie): Result<EnrichedMovie> {
        return tmdbRepository.enrichMovie(movie)
    }
}

class EnrichSeriesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(series: Series): Result<EnrichedSeries> {
        return tmdbRepository.enrichSeries(series)
    }
}

class EnrichMoviesListUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(movies: List<Movie>): Result<List<EnrichedMovie>> {
        return tmdbRepository.enrichMovies(movies)
    }
}

class EnrichSeriesListUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(seriesList: List<Series>): Result<List<EnrichedSeries>> {
        return tmdbRepository.enrichSeriesList(seriesList)
    }
}

class GetMovieDetailsUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(movieId: String): Result<TMDBMovieData> {
        return tmdbRepository.getMovieDetails(movieId)
    }
}

class GetSeriesDetailsUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(seriesId: String): Result<TMDBSeriesData> {
        return tmdbRepository.getSeriesDetails(seriesId)
    }
}

// Nuevos Use Cases para contenido popular de TMDB
class GetPopularMoviesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBMovieData>> {
        return tmdbRepository.getPopularMovies()
    }
}

class GetTrendingMoviesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBMovieData>> {
        return tmdbRepository.getTrendingMovies()
    }
}

class GetTopRatedMoviesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBMovieData>> {
        return tmdbRepository.getTopRatedMovies()
    }
}

class GetPopularSeriesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBSeriesData>> {
        return tmdbRepository.getPopularSeries()
    }
}

class GetTrendingSeriesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBSeriesData>> {
        return tmdbRepository.getTrendingSeries()
    }
}

class GetTopRatedSeriesUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository
) {
    suspend operator fun invoke(): Result<List<TMDBSeriesData>> {
        return tmdbRepository.getTopRatedSeries()
    }
}

// Use Case principal que filtra el contenido de TMDB con el disponible en Xtream
class GetFilteredTMDBContentUseCase @Inject constructor(
    private val tmdbRepository: TMDBRepository,
    private val databaseCacheManager: DatabaseCacheManager
) {
    suspend operator fun invoke(userHash: String): Result<TMDBFilteredContent> {
        return try {
            // Obtener contenido de TMDB
            val popularMoviesResult = tmdbRepository.getPopularMovies()
            val trendingMoviesResult = tmdbRepository.getTrendingMovies()
            val topRatedMoviesResult = tmdbRepository.getTopRatedMovies()
            val popularSeriesResult = tmdbRepository.getPopularSeries()
            val trendingSeriesResult = tmdbRepository.getTrendingSeries()
            val topRatedSeriesResult = tmdbRepository.getTopRatedSeries()

            // Obtener contenido disponible en Xtream
            val availableMovies = databaseCacheManager.getCachedXtreamMovies(userHash)
            val availableSeries = databaseCacheManager.getCachedXtreamSeries(userHash)

            // Crear mapas de búsqueda por TMDB ID
            val movieTmdbMap = availableMovies.mapNotNull { movie -> 
                movie.tmdbId?.let { tmdbId -> tmdbId to movie }
            }.toMap()
            
            val seriesTmdbMap = availableSeries.mapNotNull { series -> 
                series.tmdbId?.let { tmdbId -> tmdbId to series }
            }.toMap()

            // Filtrar y combinar contenido
            val filteredContent = TMDBFilteredContent(
                popularMovies = filterAndEnrichMovies(popularMoviesResult.getOrNull() ?: emptyList(), movieTmdbMap),
                trendingMovies = filterAndEnrichMovies(trendingMoviesResult.getOrNull() ?: emptyList(), movieTmdbMap),
                topRatedMovies = filterAndEnrichMovies(topRatedMoviesResult.getOrNull() ?: emptyList(), movieTmdbMap),
                popularSeries = filterAndEnrichSeries(popularSeriesResult.getOrNull() ?: emptyList(), seriesTmdbMap),
                trendingSeries = filterAndEnrichSeries(trendingSeriesResult.getOrNull() ?: emptyList(), seriesTmdbMap),
                topRatedSeries = filterAndEnrichSeries(topRatedSeriesResult.getOrNull() ?: emptyList(), seriesTmdbMap)
            )

            Result.success(filteredContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun filterAndEnrichMovies(
        tmdbMovies: List<TMDBMovieData>,
        availableMovies: Map<String, Movie>
    ): List<EnrichedTMDBMovie> {
        return tmdbMovies.mapNotNull { tmdbMovie ->
            availableMovies[tmdbMovie.id.toString()]?.let { xtreamMovie ->
                EnrichedTMDBMovie(
                    tmdbData = tmdbMovie,
                    xtreamMovie = xtreamMovie
                )
            }
        }
    }

    private fun filterAndEnrichSeries(
        tmdbSeries: List<TMDBSeriesData>,
        availableSeries: Map<String, Series>
    ): List<EnrichedTMDBSeries> {
        return tmdbSeries.mapNotNull { tmdbSeries ->
            availableSeries[tmdbSeries.id.toString()]?.let { xtreamSeries ->
                EnrichedTMDBSeries(
                    tmdbData = tmdbSeries,
                    xtreamSeries = xtreamSeries
                )
            }
        }
    }
}