package com.kybers.stream.data.repository

import com.kybers.stream.data.remote.api.XtreamApi
import com.kybers.stream.data.remote.dto.*
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val retrofit: Retrofit
) : XtreamRepository {

    private suspend fun getCredentials(): Triple<String, String, String>? {
        val user = userRepository.getCurrentUser().first()
        return if (user != null) {
            Triple(user.server, user.username, user.password)
        } else null
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
                        info = MovieInfo(
                            movieImage = detailDto.info.movieImage,
                            name = detailDto.info.name,
                            tmdbId = detailDto.info.tmdbId,
                            backdrop = detailDto.info.backdrop,
                            youtubeTrailer = detailDto.info.youtubeTrailer,
                            genre = detailDto.info.genre,
                            plot = detailDto.info.plot,
                            cast = detailDto.info.cast,
                            rating = detailDto.info.rating,
                            director = detailDto.info.director,
                            releaseDate = detailDto.info.releaseDate,
                            backdropPath = detailDto.info.backdropPath,
                            durationSecs = detailDto.info.durationSecs,
                            duration = detailDto.info.duration
                        ),
                        movieData = MovieData(
                            streamId = detailDto.movieData.streamId,
                            name = detailDto.movieData.name,
                            addedTimestamp = detailDto.movieData.added.toLongOrNull() ?: 0L,
                            categoryId = detailDto.movieData.categoryId,
                            containerExtension = detailDto.movieData.containerExtension,
                            customSid = detailDto.movieData.customSid,
                            directSource = detailDto.movieData.directSource
                        )
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
                    val seriesDetail = SeriesDetail(
                        seasons = detailDto.seasons.map { seasonDto ->
                            Season(
                                airDate = seasonDto.airDate,
                                episodeCount = seasonDto.episodeCount,
                                id = seasonDto.id,
                                name = seasonDto.name,
                                seasonNumber = seasonDto.seasonNumber,
                                overview = seasonDto.overview,
                                cover = seasonDto.cover,
                                coverBig = seasonDto.coverBig
                            )
                        },
                        info = SeriesInfo(
                            name = detailDto.info.name,
                            cover = detailDto.info.cover,
                            youtubeTrailer = detailDto.info.youtubeTrailer,
                            genre = detailDto.info.genre,
                            releaseDate = detailDto.info.releaseDate,
                            plot = detailDto.info.plot,
                            cast = detailDto.info.cast,
                            rating = detailDto.info.rating,
                            rating5Based = detailDto.info.rating5Based,
                            director = detailDto.info.director,
                            backdropPath = detailDto.info.backdropPath,
                            lastModified = detailDto.info.lastModified.toLongOrNull() ?: 0L,
                            episodeRunTime = detailDto.info.episodeRunTime,
                            categoryId = detailDto.info.categoryId
                        ),
                        episodes = detailDto.episodes.mapValues { (_, episodeList) ->
                            episodeList.map { episodeDto ->
                                Episode(
                                    id = episodeDto.id,
                                    episodeNum = episodeDto.episodeNum,
                                    title = episodeDto.title,
                                    containerExtension = episodeDto.containerExtension,
                                    addedTimestamp = episodeDto.added.toLongOrNull() ?: 0L,
                                    info = EpisodeInfo(
                                        movieImage = episodeDto.info.movieImage,
                                        releaseDate = episodeDto.info.releaseDate,
                                        youtubeTrailer = episodeDto.info.youtubeTrailer,
                                        plot = episodeDto.info.plot,
                                        cast = episodeDto.info.cast,
                                        rating = episodeDto.info.rating,
                                        rating5Based = episodeDto.info.rating5Based,
                                        director = episodeDto.info.director,
                                        durationSecs = episodeDto.info.durationSecs,
                                        duration = episodeDto.info.duration
                                    ),
                                    season = episodeDto.season,
                                    customSid = episodeDto.customSid,
                                    directSource = episodeDto.directSource
                                )
                            }
                        }
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