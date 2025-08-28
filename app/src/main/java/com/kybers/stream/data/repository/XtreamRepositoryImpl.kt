package com.kybers.stream.data.repository

import com.kybers.stream.data.remote.api.XtreamApi
import com.kybers.stream.data.remote.dto.*
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import com.kybers.stream.data.cache.CacheManager
import com.kybers.stream.data.cache.CacheConfigs
import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.domain.usecase.network.RetryStrategyUseCase
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import com.kybers.stream.di.XtreamRetrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    @param:XtreamRetrofit private val retrofit: Retrofit,
    private val cacheManager: CacheManager,
    private val databaseCacheManager: DatabaseCacheManager,
    private val retryStrategy: RetryStrategyUseCase
) : XtreamRepository {

    private suspend fun getCredentials(): Triple<String, String, String>? {
        val user = userRepository.getCurrentUser().first()
        return if (user != null) {
            Triple(user.server, user.username, user.password)
        } else null
    }
    
    private suspend fun getUserHash(): String? {
        val (server, username, password) = getCredentials() ?: return null
        return databaseCacheManager.generateUserHash(username, password, server)
    }

    private fun createApiForServer(serverUrl: String): XtreamApi {
        val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return retrofit.newBuilder()
            .baseUrl(baseUrl)
            .build()
            .create(XtreamApi::class.java)
    }

    override suspend fun getLiveCategories(): XtreamResult<List<Category>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            // Intentar obtener del caché
            val cacheKey = "live_categories_${username}"
            val cache = cacheManager.getCache<String, List<Category>>("categories", CacheConfigs.CATEGORIES)
            
            cache.get(cacheKey)?.let { cachedCategories ->
                return XtreamResult.Success(cachedCategories)
            }
            
            // Si no está en caché, hacer llamada con reintentos
            val result = retryStrategy.executeWithRetry(RetryStrategyUseCase.Configs.NORMAL) {
                val api = createApiForServer(server)
                val response = api.getLiveCategories(username, password)
                
                if (response.isSuccessful) {
                    val categories = response.body()?.map { dto ->
                        Category(
                            categoryId = dto.categoryId,
                            categoryName = dto.categoryName,
                            parentId = dto.parentId,
                            type = CategoryType.LIVE_TV
                        )
                    } ?: emptyList()
                    
                    // Guardar en caché
                    cache.put(cacheKey, categories)
                    categories
                } else {
                    throw RuntimeException("Error del servidor: ${response.code()}")
                }
            }
            
            result.fold(
                onSuccess = { XtreamResult.Success(it) },
                onFailure = { error ->
                    val mappedError = com.kybers.stream.domain.model.NetworkErrorMapper.mapError(error)
                    XtreamResult.Error(mappedError.userMessage, XtreamErrorCode.NETWORK_ERROR)
                }
            )
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getVodCategories(): XtreamResult<List<Category>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getVodCategories(username, password)
            
            if (response.isSuccessful) {
                val categories = response.body()?.map { dto ->
                    Category(
                        categoryId = dto.categoryId,
                        categoryName = dto.categoryName,
                        parentId = dto.parentId,
                        type = CategoryType.VOD
                    )
                } ?: emptyList()
                XtreamResult.Success(categories)
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getSeriesCategories(): XtreamResult<List<Category>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getSeriesCategories(username, password)
            
            if (response.isSuccessful) {
                val categories = response.body()?.map { dto ->
                    Category(
                        categoryId = dto.categoryId,
                        categoryName = dto.categoryName,
                        parentId = dto.parentId,
                        type = CategoryType.SERIES
                    )
                } ?: emptyList()
                XtreamResult.Success(categories)
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getLiveStreams(categoryId: String?): XtreamResult<List<Channel>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getLiveStreams(username, password, categoryId = categoryId)
            
            if (response.isSuccessful) {
                val channels = response.body()?.map { dto ->
                    Channel(
                        streamId = dto.streamId,
                        name = dto.name,
                        icon = dto.streamIcon,
                        categoryId = dto.categoryId,
                        epgChannelId = dto.epgChannelId,
                        isAdult = dto.isAdult == "1",
                        tvArchive = dto.tvArchive == 1,
                        tvArchiveDuration = dto.tvArchiveDuration,
                        addedTimestamp = dto.added.toLongOrNull() ?: 0L
                    )
                } ?: emptyList()
                XtreamResult.Success(channels)
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getVodStreams(categoryId: String?): XtreamResult<List<Movie>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val userHash = getUserHash() 
                ?: return XtreamResult.Error("Error generando hash de usuario", XtreamErrorCode.UNKNOWN)
            
            // Verificar si el cache es válido
            if (databaseCacheManager.isXtreamCacheValid(userHash)) {
                val cachedMovies = databaseCacheManager.getCachedXtreamMovies(userHash)
                val filteredMovies = if (categoryId != null) {
                    cachedMovies.filter { it.categoryId == categoryId }
                } else {
                    cachedMovies
                }
                return XtreamResult.Success(filteredMovies)
            }
            
            // Si no hay cache válido, hacer llamada a la API
            val api = createApiForServer(server)
            val response = api.getVodStreams(username, password, categoryId = categoryId)
            
            if (response.isSuccessful) {
                val movies = response.body()?.map { dto ->
                    Movie(
                        streamId = dto.streamId,
                        name = dto.name,
                        icon = dto.streamIcon,
                        categoryId = dto.categoryId,
                        rating = dto.rating,
                        rating5Based = dto.rating5Based,
                        addedTimestamp = dto.added.toLongOrNull() ?: 0L,
                        isAdult = dto.isAdult == "1",
                        containerExtension = dto.containerExtension,
                        tmdbId = dto.tmdbId
                    )
                } ?: emptyList()
                
                // Guardar en cache si es la llamada completa (sin filtro de categoría)
                if (categoryId == null) {
                    databaseCacheManager.cacheXtreamMovies(movies, userHash)
                }
                
                XtreamResult.Success(movies)
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getSeries(categoryId: String?): XtreamResult<List<Series>> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val userHash = getUserHash() 
                ?: return XtreamResult.Error("Error generando hash de usuario", XtreamErrorCode.UNKNOWN)
            
            // Verificar si el cache es válido
            if (databaseCacheManager.isXtreamCacheValid(userHash)) {
                val cachedSeries = databaseCacheManager.getCachedXtreamSeries(userHash)
                val filteredSeries = if (categoryId != null) {
                    cachedSeries.filter { it.categoryId == categoryId }
                } else {
                    cachedSeries
                }
                return XtreamResult.Success(filteredSeries)
            }
            
            // Si no hay cache válido, hacer llamada a la API
            val api = createApiForServer(server)
            val response = api.getSeries(username, password, categoryId = categoryId)
            
            if (response.isSuccessful) {
                val series = response.body()?.map { dto ->
                    Series(
                        seriesId = dto.seriesId,
                        name = dto.name,
                        cover = dto.cover,
                        categoryId = dto.categoryId,
                        plot = dto.plot,
                        cast = dto.cast,
                        director = dto.director,
                        genre = dto.genre,
                        releaseDate = dto.releaseDate,
                        lastModified = dto.lastModified.toLongOrNull() ?: 0L,
                        rating = dto.rating,
                        rating5Based = dto.rating5Based,
                        backdropPath = dto.backdropPath,
                        youtubeTrailer = dto.youtubeTrailer,
                        episodeRunTime = dto.episodeRunTime,
                        tmdbId = dto.tmdbId
                    )
                } ?: emptyList()
                
                // Guardar en cache si es la llamada completa (sin filtro de categoría)
                if (categoryId == null) {
                    databaseCacheManager.cacheXtreamSeries(series, userHash)
                }
                
                XtreamResult.Success(series)
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getVodInfo(vodId: String): XtreamResult<MovieDetail> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getVodInfo(username, password, vodId = vodId)
            
            if (response.isSuccessful) {
                val detailDto = response.body()
                if (detailDto != null) {
                    val movieDetail = MovieDetail(
                        id = detailDto.movieData.streamId,
                        name = detailDto.info.name,
                        streamId = detailDto.movieData.streamId,
                        year = detailDto.info.releaseDate?.take(4),
                        rating = detailDto.info.rating,
                        duration = detailDto.info.duration,
                        quality = null, // Not available in DTO
                        genre = detailDto.info.genre,
                        plot = detailDto.info.plot,
                        cast = detailDto.info.cast,
                        director = detailDto.info.director,
                        poster = detailDto.info.movieImage,
                        backdrop = detailDto.info.backdrop,
                        trailerUrl = detailDto.info.youtubeTrailer,
                        imdbRating = null, // Parse from rating if needed
                        tmdbRating = detailDto.info.tmdbId,
                        language = null, // Not available in DTO
                        country = null, // Not available in DTO
                        releaseDate = detailDto.info.releaseDate
                    )
                    XtreamResult.Success(movieDetail)
                } else {
                    XtreamResult.Error("Respuesta vacía del servidor", XtreamErrorCode.SERVER_ERROR)
                }
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getSeriesInfo(seriesId: String): XtreamResult<SeriesDetail> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getSeriesInfo(username, password, seriesId = seriesId)
            
            if (response.isSuccessful) {
                val detailDto = response.body()
                if (detailDto != null) {
                    // Convert episodes map to seasons with episodes
                    val seasonsWithEpisodes = detailDto.seasons.map { seasonDto ->
                        val seasonEpisodes = detailDto.episodes[seasonDto.seasonNumber]?.map { episodeDto ->
                            Episode(
                                id = episodeDto.id,
                                episodeNumber = episodeDto.episodeNum,
                                seasonNumber = episodeDto.season,
                                name = episodeDto.title,
                                overview = episodeDto.info.plot,
                                runtime = episodeDto.info.duration,
                                airDate = episodeDto.info.releaseDate,
                                still = episodeDto.info.movieImage,
                                rating = episodeDto.info.rating.toString(),
                                streamUrl = null // Will be set during playback
                            )
                        } ?: emptyList()
                        
                        Season(
                            seasonNumber = seasonDto.seasonNumber.toIntOrNull() ?: 1,
                            name = seasonDto.name,
                            overview = seasonDto.overview,
                            airDate = seasonDto.airDate,
                            poster = seasonDto.cover,
                            episodes = seasonEpisodes,
                            episodeCount = seasonDto.episodeCount
                        )
                    }
                    
                    val seriesDetail = SeriesDetail(
                        id = seriesId,
                        name = detailDto.info.name,
                        seriesId = seriesId,
                        year = detailDto.info.releaseDate?.take(4),
                        rating = detailDto.info.rating,
                        genre = detailDto.info.genre,
                        plot = detailDto.info.plot,
                        cast = detailDto.info.cast,
                        director = detailDto.info.director,
                        poster = detailDto.info.cover,
                        backdrop = detailDto.info.backdropPath.firstOrNull(),
                        imdbRating = null, // Parse from rating if needed
                        tmdbRating = null, // Not available in DTO
                        language = null, // Not available in DTO
                        country = null, // Not available in DTO
                        releaseDate = detailDto.info.releaseDate,
                        seasons = seasonsWithEpisodes,
                        totalSeasons = seasonsWithEpisodes.size,
                        totalEpisodes = seasonsWithEpisodes.sumOf { it.episodes.size },
                        status = null // Not available in DTO
                    )
                    XtreamResult.Success(seriesDetail)
                } else {
                    XtreamResult.Error("Respuesta vacía del servidor", XtreamErrorCode.SERVER_ERROR)
                }
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getShortEpg(streamId: String, limit: Int): XtreamResult<EpgResponse> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getShortEpg(username, password, streamId = streamId, limit = limit)
            
            if (response.isSuccessful) {
                val epgDto = response.body()
                if (epgDto != null) {
                    val epgResponse = EpgResponse(
                        epgListings = epgDto.epgListings.map { listingDto ->
                            EpgListing(
                                id = listingDto.id,
                                title = listingDto.title,
                                start = listingDto.start,
                                stop = listingDto.stop,
                                description = listingDto.description,
                                channelId = listingDto.channelId
                            )
                        }
                    )
                    XtreamResult.Success(epgResponse)
                } else {
                    XtreamResult.Error("Respuesta vacía del servidor", XtreamErrorCode.SERVER_ERROR)
                }
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }

    override suspend fun getXmlEpg(): XtreamResult<String> {
        return try {
            val (server, username, password) = getCredentials() 
                ?: return XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS)
            
            val api = createApiForServer(server)
            val response = api.getXmlEpg(username, password)
            
            if (response.isSuccessful) {
                val xmlContent = response.body()
                if (xmlContent != null) {
                    XtreamResult.Success(xmlContent)
                } else {
                    XtreamResult.Error("Respuesta vacía del servidor", XtreamErrorCode.SERVER_ERROR)
                }
            } else {
                XtreamResult.Error("Error del servidor: ${response.code()}", XtreamErrorCode.SERVER_ERROR)
            }
        } catch (e: UnknownHostException) {
            XtreamResult.Error("No se puede conectar al servidor", XtreamErrorCode.NETWORK_ERROR)
        } catch (e: SocketTimeoutException) {
            XtreamResult.Error("Tiempo de espera agotado", XtreamErrorCode.TIMEOUT)
        } catch (e: Exception) {
            XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN)
        }
    }
}