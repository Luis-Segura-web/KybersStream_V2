package com.kybers.stream.domain.usecase

import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.TMDBRepository
import javax.inject.Inject

data class TMDBUseCases(
    val enrichMovie: EnrichMovieUseCase,
    val enrichSeries: EnrichSeriesUseCase,
    val enrichMoviesList: EnrichMoviesListUseCase,
    val enrichSeriesList: EnrichSeriesListUseCase,
    val getMovieDetails: GetMovieDetailsUseCase,
    val getSeriesDetails: GetSeriesDetailsUseCase
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