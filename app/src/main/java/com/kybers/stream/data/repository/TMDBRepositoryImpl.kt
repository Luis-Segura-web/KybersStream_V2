package com.kybers.stream.data.repository

import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.data.remote.api.TMDBApi
import com.kybers.stream.data.remote.dto.*
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TMDBRepositoryImpl @Inject constructor(
    private val tmdbApi: TMDBApi,
    private val databaseCacheManager: DatabaseCacheManager
) : TMDBRepository {
    
    companion object {
        private const val TMDB_API_KEY = "0a82c6ff2b4b130f83facf56ae9a89b1"
        private const val LANGUAGE = "es-ES"
    }
    
    override suspend fun getMovieDetails(movieId: String): Result<TMDBMovieData> {
        return try {
            // Verificar cache primero
            if (databaseCacheManager.isTMDBCacheValid(movieId)) {
                val cachedMovie = databaseCacheManager.getCachedTMDBMovie(movieId)
                if (cachedMovie != null) {
                    return Result.success(cachedMovie)
                }
            }
            
            // Si no hay cache válido, hacer llamada a TMDB
            val response = tmdbApi.getMovieDetails(
                movieId = movieId,
                apiKey = TMDB_API_KEY,
                language = LANGUAGE,
                appendToResponse = "credits,videos"
            )
            
            if (response.isSuccessful) {
                response.body()?.let { movieDto ->
                    val movieData = movieDto.toDomain()
                    // Guardar en cache
                    databaseCacheManager.cacheTMDBMovie(movieData, movieId)
                    Result.success(movieData)
                } ?: Result.failure(Exception("Respuesta vacía de TMDB"))
            } else {
                Result.failure(Exception("Error TMDB: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSeriesDetails(seriesId: String): Result<TMDBSeriesData> {
        return try {
            // Verificar cache primero
            if (databaseCacheManager.isTMDBCacheValid(seriesId)) {
                val cachedSeries = databaseCacheManager.getCachedTMDBSeries(seriesId)
                if (cachedSeries != null) {
                    return Result.success(cachedSeries)
                }
            }
            
            // Si no hay cache válido, hacer llamada a TMDB
            val response = tmdbApi.getSeriesDetails(
                tvId = seriesId,
                apiKey = TMDB_API_KEY,
                language = LANGUAGE,
                appendToResponse = "credits,videos"
            )
            
            if (response.isSuccessful) {
                response.body()?.let { seriesDto ->
                    val seriesData = seriesDto.toDomain()
                    // Guardar en cache
                    databaseCacheManager.cacheTMDBSeries(seriesData, seriesId)
                    Result.success(seriesData)
                } ?: Result.failure(Exception("Respuesta vacía de TMDB"))
            } else {
                Result.failure(Exception("Error TMDB: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enrichMovie(movie: Movie): Result<EnrichedMovie> {
        return try {
            val tmdbData = if (!movie.tmdbId.isNullOrEmpty()) {
                getMovieDetails(movie.tmdbId).getOrNull()
            } else null
            
            Result.success(
                EnrichedMovie(
                    streamId = movie.streamId,
                    name = movie.name,
                    icon = movie.icon,
                    categoryId = movie.categoryId,
                    rating = movie.rating,
                    rating5Based = movie.rating5Based,
                    addedTimestamp = movie.addedTimestamp,
                    isAdult = movie.isAdult,
                    containerExtension = movie.containerExtension,
                    tmdbId = movie.tmdbId,
                    tmdbData = tmdbData
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enrichSeries(series: Series): Result<EnrichedSeries> {
        return try {
            val tmdbData = if (!series.tmdbId.isNullOrEmpty()) {
                getSeriesDetails(series.tmdbId).getOrNull()
            } else null
            
            Result.success(
                EnrichedSeries(
                    seriesId = series.seriesId,
                    name = series.name,
                    cover = series.cover,
                    categoryId = series.categoryId,
                    plot = series.plot,
                    cast = series.cast,
                    director = series.director,
                    genre = series.genre,
                    releaseDate = series.releaseDate,
                    lastModified = series.lastModified,
                    rating = series.rating,
                    rating5Based = series.rating5Based,
                    backdropPath = series.backdropPath,
                    youtubeTrailer = series.youtubeTrailer,
                    episodeRunTime = series.episodeRunTime,
                    tmdbId = series.tmdbId,
                    tmdbData = tmdbData
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enrichMovies(movies: List<Movie>): Result<List<EnrichedMovie>> {
        return try {
            coroutineScope {
                val enrichedMovies = movies.map { movie ->
                    async { enrichMovie(movie) }
                }.awaitAll()
                
                val failures = enrichedMovies.filter { it.isFailure }
                if (failures.isNotEmpty()) {
                    Result.failure(failures.first().exceptionOrNull() ?: Exception("Error enriqueciendo películas"))
                } else {
                    Result.success(enrichedMovies.mapNotNull { it.getOrNull() })
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enrichSeriesList(seriesList: List<Series>): Result<List<EnrichedSeries>> {
        return try {
            coroutineScope {
                val enrichedSeries = seriesList.map { series ->
                    async { enrichSeries(series) }
                }.awaitAll()
                
                val failures = enrichedSeries.filter { it.isFailure }
                if (failures.isNotEmpty()) {
                    Result.failure(failures.first().exceptionOrNull() ?: Exception("Error enriqueciendo series"))
                } else {
                    Result.success(enrichedSeries.mapNotNull { it.getOrNull() })
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions para convertir DTOs a modelos de dominio
private fun TMDBMovieDto.toDomain(): TMDBMovieData {
    return TMDBMovieData(
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = TMDBApi.getImageUrl(posterPath),
        backdropPath = TMDBApi.getImageUrl(backdropPath, TMDBApi.BACKDROP_SIZE_W1280),
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        runtime = runtime,
        genres = genres.map { it.toDomain() },
        productionCompanies = productionCompanies.map { it.toDomain() },
        productionCountries = productionCountries.map { it.toDomain() },
        spokenLanguages = spokenLanguages.map { it.toDomain() },
        adult = adult,
        budget = budget,
        revenue = revenue,
        tagline = tagline,
        status = status,
        originalLanguage = originalLanguage,
        popularity = popularity,
        homepage = homepage,
        imdbId = imdbId,
        credits = null, // Se manejará por separado si es necesario
        videos = emptyList() // Se manejará por separado si es necesario
    )
}

private fun TMDBSeriesDto.toDomain(): TMDBSeriesData {
    return TMDBSeriesData(
        name = name,
        originalName = originalName,
        overview = overview,
        posterPath = TMDBApi.getImageUrl(posterPath),
        backdropPath = TMDBApi.getImageUrl(backdropPath, TMDBApi.BACKDROP_SIZE_W1280),
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.toDomain() },
        productionCompanies = productionCompanies.map { it.toDomain() },
        productionCountries = productionCountries.map { it.toDomain() },
        spokenLanguages = spokenLanguages.map { it.toDomain() },
        adult = adult,
        createdBy = createdBy.map { it.toDomain() },
        episodeRunTime = episodeRunTime,
        inProduction = inProduction,
        numberOfEpisodes = numberOfEpisodes,
        numberOfSeasons = numberOfSeasons,
        originalLanguage = originalLanguage,
        popularity = popularity,
        status = status,
        tagline = tagline,
        type = type,
        homepage = homepage,
        networks = networks.map { it.toDomain() },
        seasons = seasons.map { it.toDomain() },
        credits = null, // Se manejará por separado si es necesario
        videos = emptyList() // Se manejará por separado si es necesario
    )
}

private fun TMDBGenreDto.toDomain() = TMDBGenre(id = id, name = name)

private fun TMDBProductionCompanyDto.toDomain() = TMDBProductionCompany(
    id = id,
    name = name,
    logoPath = TMDBApi.getImageUrl(logoPath),
    originCountry = originCountry
)

private fun TMDBProductionCountryDto.toDomain() = TMDBProductionCountry(
    iso31661 = iso31661,
    name = name
)

private fun TMDBSpokenLanguageDto.toDomain() = TMDBSpokenLanguage(
    iso6391 = iso6391,
    englishName = englishName,
    name = name
)

private fun TMDBCreatedByDto.toDomain() = TMDBCreatedBy(
    id = id,
    creditId = creditId,
    name = name,
    profilePath = TMDBApi.getImageUrl(profilePath, TMDBApi.PROFILE_SIZE_W185)
)

private fun TMDBNetworkDto.toDomain() = TMDBNetwork(
    id = id,
    name = name,
    logoPath = TMDBApi.getImageUrl(logoPath),
    originCountry = originCountry
)

private fun TMDBSeasonDto.toDomain() = TMDBSeason(
    id = id,
    airDate = airDate,
    episodeCount = episodeCount,
    name = name,
    overview = overview,
    posterPath = TMDBApi.getImageUrl(posterPath),
    seasonNumber = seasonNumber
)

private fun TMDBCreditsDto.toDomain() = TMDBCredits(
    cast = cast.map { it.toDomain() },
    crew = crew.map { it.toDomain() }
)

private fun TMDBCastDto.toDomain() = TMDBCast(
    id = id,
    name = name,
    character = character,
    profilePath = TMDBApi.getImageUrl(profilePath, TMDBApi.PROFILE_SIZE_W185),
    order = order
)

private fun TMDBCrewDto.toDomain() = TMDBCrew(
    id = id,
    name = name,
    job = job,
    department = department,
    profilePath = TMDBApi.getImageUrl(profilePath, TMDBApi.PROFILE_SIZE_W185)
)

private fun TMDBVideoDto.toDomain() = TMDBVideo(
    id = id,
    key = key,
    name = name,
    site = site,
    type = type,
    official = official,
    publishedAt = publishedAt
)