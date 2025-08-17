package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*

interface TMDBRepository {
    suspend fun getMovieDetails(movieId: String): Result<TMDBMovieData>
    suspend fun getSeriesDetails(seriesId: String): Result<TMDBSeriesData>
    suspend fun enrichMovie(movie: Movie): Result<EnrichedMovie>
    suspend fun enrichSeries(series: Series): Result<EnrichedSeries>
    suspend fun enrichMovies(movies: List<Movie>): Result<List<EnrichedMovie>>
    suspend fun enrichSeriesList(seriesList: List<Series>): Result<List<EnrichedSeries>>
}