package com.kybers.stream.di

import com.kybers.stream.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideTMDBUseCases(
        enrichMovie: EnrichMovieUseCase,
        enrichSeries: EnrichSeriesUseCase,
        enrichMoviesList: EnrichMoviesListUseCase,
        enrichSeriesList: EnrichSeriesListUseCase,
        getMovieDetails: GetMovieDetailsUseCase,
        getSeriesDetails: GetSeriesDetailsUseCase
    ): TMDBUseCases {
        return TMDBUseCases(
            enrichMovie = enrichMovie,
            enrichSeries = enrichSeries,
            enrichMoviesList = enrichMoviesList,
            enrichSeriesList = enrichSeriesList,
            getMovieDetails = getMovieDetails,
            getSeriesDetails = getSeriesDetails
        )
    }
}