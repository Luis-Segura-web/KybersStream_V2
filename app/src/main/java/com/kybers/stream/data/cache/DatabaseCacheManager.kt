package com.kybers.stream.data.cache

import com.kybers.stream.data.local.dao.XtreamCacheDao
import com.kybers.stream.data.local.dao.TMDBCacheDao
import com.kybers.stream.data.local.entity.*
import com.kybers.stream.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseCacheManager @Inject constructor(
    private val xtreamCacheDao: XtreamCacheDao,
    private val tmdbCacheDao: TMDBCacheDao
) {
    
    companion object {
        private const val XTREAM_CACHE_HOURS = 12L
        private const val TMDB_CACHE_DAYS = 7L
    }
    
    // =============== XTREAM CACHE MANAGEMENT ===============
    
    suspend fun isXtreamCacheValid(userHash: String): Boolean = withContext(Dispatchers.IO) {
        val cutoffTime = LocalDateTime.now().minusHours(XTREAM_CACHE_HOURS)
        val syncMetadata = xtreamCacheDao.getSyncMetadata(userHash)
        
        syncMetadata?.let { metadata ->
            metadata.isValid && metadata.lastFullSync.isAfter(cutoffTime)
        } ?: false
    }
    
    suspend fun getXtreamCacheValidUntil(userHash: String): LocalDateTime? = withContext(Dispatchers.IO) {
        val syncMetadata = xtreamCacheDao.getSyncMetadata(userHash)
        syncMetadata?.lastFullSync?.plusHours(XTREAM_CACHE_HOURS)
    }
    
    suspend fun cacheXtreamMovies(movies: List<Movie>, userHash: String) = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val entities = movies.map { movie ->
            XtreamMovieCacheEntity(
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
                userHash = userHash,
                lastSync = now
            )
        }
        xtreamCacheDao.insertMovies(entities)
    }
    
    suspend fun cacheXtreamSeries(series: List<Series>, userHash: String) = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val entities = series.map { serie ->
            XtreamSeriesCacheEntity(
                seriesId = serie.seriesId,
                name = serie.name,
                cover = serie.cover,
                categoryId = serie.categoryId,
                plot = serie.plot,
                cast = serie.cast,
                director = serie.director,
                genre = serie.genre,
                releaseDate = serie.releaseDate,
                lastModified = serie.lastModified,
                rating = serie.rating,
                rating5Based = serie.rating5Based,
                backdropPath = serie.backdropPath,
                youtubeTrailer = serie.youtubeTrailer,
                episodeRunTime = serie.episodeRunTime,
                tmdbId = serie.tmdbId,
                userHash = userHash,
                lastSync = now
            )
        }
        xtreamCacheDao.insertSeries(entities)
    }
    
    suspend fun cacheXtreamChannels(channels: List<Channel>, userHash: String) = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val entities = channels.map { channel ->
            XtreamChannelCacheEntity(
                streamId = channel.streamId,
                name = channel.name,
                icon = channel.icon,
                categoryId = channel.categoryId,
                epgChannelId = channel.epgChannelId,
                isAdult = channel.isAdult,
                tvArchive = channel.tvArchive,
                tvArchiveDuration = channel.tvArchiveDuration,
                addedTimestamp = channel.addedTimestamp,
                userHash = userHash,
                lastSync = now
            )
        }
        xtreamCacheDao.insertChannels(entities)
    }
    
    suspend fun cacheXtreamCategories(categories: List<Category>, type: String, userHash: String) = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val entities = categories.map { category ->
            XtreamCategoryCacheEntity(
                categoryId = category.categoryId,
                name = category.categoryName,
                type = type,
                userHash = userHash,
                lastSync = now
            )
        }
        xtreamCacheDao.insertCategories(entities)
    }
    
    suspend fun updateXtreamSyncMetadata(userHash: String, moviesCount: Int, seriesCount: Int, channelsCount: Int, categoriesCount: Int) = withContext(Dispatchers.IO) {
        val metadata = XtreamSyncMetadataEntity(
            userHash = userHash,
            lastFullSync = LocalDateTime.now(),
            moviesCount = moviesCount,
            seriesCount = seriesCount,
            channelsCount = channelsCount,
            categoriesCount = categoriesCount,
            isValid = true
        )
        xtreamCacheDao.insertSyncMetadata(metadata)
    }
    
    suspend fun getCachedXtreamMovies(userHash: String): List<Movie> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getAllMovies(userHash).map { entity ->
            Movie(
                streamId = entity.streamId,
                name = entity.name,
                icon = entity.icon,
                categoryId = entity.categoryId,
                rating = entity.rating,
                rating5Based = entity.rating5Based,
                addedTimestamp = entity.addedTimestamp,
                isAdult = entity.isAdult,
                containerExtension = entity.containerExtension,
                tmdbId = entity.tmdbId
            )
        }
    }
    
    suspend fun getCachedXtreamSeries(userHash: String): List<Series> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getAllSeries(userHash).map { entity ->
            Series(
                seriesId = entity.seriesId,
                name = entity.name,
                cover = entity.cover,
                categoryId = entity.categoryId,
                plot = entity.plot,
                cast = entity.cast,
                director = entity.director,
                genre = entity.genre,
                releaseDate = entity.releaseDate,
                lastModified = entity.lastModified,
                rating = entity.rating,
                rating5Based = entity.rating5Based,
                backdropPath = entity.backdropPath,
                youtubeTrailer = entity.youtubeTrailer,
                episodeRunTime = entity.episodeRunTime,
                tmdbId = entity.tmdbId
            )
        }
    }
    
    suspend fun getCachedXtreamChannels(userHash: String): List<Channel> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getAllChannels(userHash).map { entity ->
            Channel(
                streamId = entity.streamId,
                name = entity.name,
                icon = entity.icon,
                categoryId = entity.categoryId,
                epgChannelId = entity.epgChannelId,
                isAdult = entity.isAdult,
                tvArchive = entity.tvArchive,
                tvArchiveDuration = entity.tvArchiveDuration,
                addedTimestamp = entity.addedTimestamp
            )
        }
    }
    
    suspend fun getCachedXtreamCategories(userHash: String, type: String): List<Category> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getCategoriesByType(userHash, type).map { entity ->
            Category(
                categoryId = entity.categoryId,
                categoryName = entity.name,
                type = when (entity.type) {
                    "live" -> CategoryType.LIVE_TV
                    "vod" -> CategoryType.VOD
                    "series" -> CategoryType.SERIES
                    else -> CategoryType.VOD
                }
            )
        }
    }
    
    // =============== TMDB CACHE MANAGEMENT ===============
    
    suspend fun isTMDBCacheValid(tmdbId: String): Boolean = withContext(Dispatchers.IO) {
        val cutoffTime = LocalDateTime.now().minusDays(TMDB_CACHE_DAYS)
        val movieEntity = tmdbCacheDao.getMovieById(tmdbId)
        val seriesEntity = tmdbCacheDao.getSeriesById(tmdbId)
        
        (movieEntity?.lastSync?.isAfter(cutoffTime) == true) || 
        (seriesEntity?.lastSync?.isAfter(cutoffTime) == true)
    }
    
    suspend fun cacheTMDBMovie(movieData: TMDBMovieData, tmdbId: String) = withContext(Dispatchers.IO) {
        val entity = TMDBMovieCacheEntity(
            tmdbId = tmdbId,
            title = movieData.title,
            originalTitle = movieData.originalTitle,
            overview = movieData.overview,
            posterPath = movieData.posterPath,
            backdropPath = movieData.backdropPath,
            releaseDate = movieData.releaseDate,
            voteAverage = movieData.voteAverage,
            voteCount = movieData.voteCount,
            runtime = movieData.runtime,
            genres = movieData.genres.map { it.name }, // Simplificado para cache
            adult = movieData.adult,
            budget = movieData.budget,
            revenue = movieData.revenue,
            tagline = movieData.tagline,
            status = movieData.status,
            originalLanguage = movieData.originalLanguage,
            popularity = movieData.popularity,
            homepage = movieData.homepage,
            imdbId = movieData.imdbId,
            lastSync = LocalDateTime.now()
        )
        tmdbCacheDao.insertMovie(entity)
    }
    
    suspend fun cacheTMDBSeries(seriesData: TMDBSeriesData, tmdbId: String) = withContext(Dispatchers.IO) {
        val entity = TMDBSeriesCacheEntity(
            tmdbId = tmdbId,
            name = seriesData.name,
            originalName = seriesData.originalName,
            overview = seriesData.overview,
            posterPath = seriesData.posterPath,
            backdropPath = seriesData.backdropPath,
            firstAirDate = seriesData.firstAirDate,
            lastAirDate = seriesData.lastAirDate,
            voteAverage = seriesData.voteAverage,
            voteCount = seriesData.voteCount,
            genres = seriesData.genres.map { it.name }, // Simplificado para cache
            adult = seriesData.adult,
            episodeRunTime = seriesData.episodeRunTime,
            inProduction = seriesData.inProduction,
            numberOfEpisodes = seriesData.numberOfEpisodes,
            numberOfSeasons = seriesData.numberOfSeasons,
            originalLanguage = seriesData.originalLanguage,
            popularity = seriesData.popularity,
            status = seriesData.status,
            tagline = seriesData.tagline,
            type = seriesData.type,
            homepage = seriesData.homepage,
            lastSync = LocalDateTime.now()
        )
        tmdbCacheDao.insertSeries(entity)
    }
    
    suspend fun getCachedTMDBMovie(tmdbId: String): TMDBMovieData? = withContext(Dispatchers.IO) {
        tmdbCacheDao.getMovieById(tmdbId)?.let { entity ->
            TMDBMovieData(
                id = entity.tmdbId.toIntOrNull() ?: 0,
                title = entity.title,
                originalTitle = entity.originalTitle,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                releaseDate = entity.releaseDate,
                voteAverage = entity.voteAverage,
                voteCount = entity.voteCount,
                runtime = entity.runtime,
                genres = entity.genres.map { TMDBGenre(0, it) }, // ID simplificado
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                spokenLanguages = emptyList(),
                adult = entity.adult,
                budget = entity.budget,
                revenue = entity.revenue,
                tagline = entity.tagline,
                status = entity.status,
                originalLanguage = entity.originalLanguage,
                popularity = entity.popularity,
                homepage = entity.homepage,
                imdbId = entity.imdbId,
                credits = null,
                videos = emptyList()
            )
        }
    }
    
    suspend fun getCachedTMDBSeries(tmdbId: String): TMDBSeriesData? = withContext(Dispatchers.IO) {
        tmdbCacheDao.getSeriesById(tmdbId)?.let { entity ->
            TMDBSeriesData(
                id = entity.tmdbId.toIntOrNull() ?: 0,
                name = entity.name,
                originalName = entity.originalName,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                firstAirDate = entity.firstAirDate,
                lastAirDate = entity.lastAirDate,
                voteAverage = entity.voteAverage,
                voteCount = entity.voteCount,
                genres = entity.genres.map { TMDBGenre(0, it) }, // ID simplificado
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                spokenLanguages = emptyList(),
                adult = entity.adult,
                createdBy = emptyList(),
                episodeRunTime = entity.episodeRunTime,
                inProduction = entity.inProduction,
                numberOfEpisodes = entity.numberOfEpisodes,
                numberOfSeasons = entity.numberOfSeasons,
                originalLanguage = entity.originalLanguage,
                popularity = entity.popularity,
                status = entity.status,
                tagline = entity.tagline,
                type = entity.type,
                homepage = entity.homepage,
                networks = emptyList(),
                seasons = emptyList(),
                credits = null,
                videos = emptyList()
            )
        }
    }
    
    // =============== UTILITY FUNCTIONS ===============
    
    fun generateUserHash(username: String, password: String, serverUrl: String): String {
        val input = "$username:$password:$serverUrl"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    suspend fun invalidateXtreamCache(userHash: String) = withContext(Dispatchers.IO) {
        xtreamCacheDao.clearMovies(userHash)
        xtreamCacheDao.clearSeries(userHash)
        xtreamCacheDao.clearChannels(userHash)
        xtreamCacheDao.clearCategories(userHash)
        xtreamCacheDao.clearSyncMetadata(userHash)
    }
    
    suspend fun cleanupExpiredData() = withContext(Dispatchers.IO) {
        val xtreamCutoff = LocalDateTime.now().minusHours(XTREAM_CACHE_HOURS)
        val tmdbCutoff = LocalDateTime.now().minusDays(TMDB_CACHE_DAYS)
        
        // Cleanup TMDB cache
        tmdbCacheDao.cleanupExpiredData()
    }
    
    suspend fun cleanupOtherUsersData(currentUserHash: String) = withContext(Dispatchers.IO) {
        xtreamCacheDao.cleanupOtherUsersMovies(currentUserHash)
        xtreamCacheDao.cleanupOtherUsersSeries(currentUserHash)
        xtreamCacheDao.cleanupOtherUsersChannels(currentUserHash)
        xtreamCacheDao.cleanupOtherUsersCategories(currentUserHash)
        xtreamCacheDao.cleanupOtherUsersSyncMetadata(currentUserHash)
    }
    
    // =============== HOME SCREEN OPTIMIZED QUERIES ===============
    
    suspend fun getAvailableMoviesTmdbIds(userHash: String): List<String> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getAllMovieTmdbIds(userHash)
    }
    
    suspend fun getAvailableSeriesTmdbIds(userHash: String): List<String> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getAllSeriesTmdbIds(userHash)
    }
    
    suspend fun getMoviesGroupedByTmdbId(userHash: String, tmdbId: String): List<Movie> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getMoviesByTmdbId(userHash, tmdbId).map { entity ->
            Movie(
                streamId = entity.streamId,
                name = entity.name,
                icon = entity.icon,
                categoryId = entity.categoryId,
                rating = entity.rating,
                rating5Based = entity.rating5Based,
                addedTimestamp = entity.addedTimestamp,
                isAdult = entity.isAdult,
                containerExtension = entity.containerExtension,
                tmdbId = entity.tmdbId
            )
        }
    }
    
    suspend fun getSeriesGroupedByTmdbId(userHash: String, tmdbId: String): List<Series> = withContext(Dispatchers.IO) {
        xtreamCacheDao.getSeriesByTmdbId(userHash, tmdbId).map { entity ->
            Series(
                seriesId = entity.seriesId,
                name = entity.name,
                cover = entity.cover,
                categoryId = entity.categoryId,
                plot = entity.plot,
                cast = entity.cast,
                director = entity.director,
                genre = entity.genre,
                releaseDate = entity.releaseDate,
                lastModified = entity.lastModified,
                rating = entity.rating,
                rating5Based = entity.rating5Based,
                backdropPath = entity.backdropPath,
                youtubeTrailer = entity.youtubeTrailer,
                episodeRunTime = entity.episodeRunTime,
                tmdbId = entity.tmdbId
            )
        }
    }
    
    // =============== BATCH TMDB CACHE OPERATIONS ===============
    
    suspend fun getCachedTMDBMovies(tmdbIds: List<String>): Map<String, TMDBMovieData> = withContext(Dispatchers.IO) {
        val validIds = tmdbIds.filter { isTMDBCacheValid(it) }
        val entities = tmdbCacheDao.getMoviesByIds(validIds)
        
        entities.associate { entity ->
            entity.tmdbId to TMDBMovieData(
                id = entity.tmdbId.toIntOrNull() ?: 0,
                title = entity.title,
                originalTitle = entity.originalTitle,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                releaseDate = entity.releaseDate,
                voteAverage = entity.voteAverage,
                voteCount = entity.voteCount,
                runtime = entity.runtime,
                genres = entity.genres.map { TMDBGenre(0, it) },
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                spokenLanguages = emptyList(),
                adult = entity.adult,
                budget = entity.budget,
                revenue = entity.revenue,
                tagline = entity.tagline,
                status = entity.status,
                originalLanguage = entity.originalLanguage,
                popularity = entity.popularity,
                homepage = entity.homepage,
                imdbId = entity.imdbId,
                credits = null,
                videos = emptyList()
            )
        }
    }
    
    suspend fun getCachedTMDBSeries(tmdbIds: List<String>): Map<String, TMDBSeriesData> = withContext(Dispatchers.IO) {
        val validIds = tmdbIds.filter { isTMDBCacheValid(it) }
        val entities = tmdbCacheDao.getSeriesByIds(validIds)
        
        entities.associate { entity ->
            entity.tmdbId to TMDBSeriesData(
                id = entity.tmdbId.toIntOrNull() ?: 0,
                name = entity.name,
                originalName = entity.originalName,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                firstAirDate = entity.firstAirDate,
                lastAirDate = entity.lastAirDate,
                voteAverage = entity.voteAverage,
                voteCount = entity.voteCount,
                genres = entity.genres.map { TMDBGenre(0, it) },
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                spokenLanguages = emptyList(),
                adult = entity.adult,
                createdBy = emptyList(),
                episodeRunTime = entity.episodeRunTime,
                inProduction = entity.inProduction,
                numberOfEpisodes = entity.numberOfEpisodes,
                numberOfSeasons = entity.numberOfSeasons,
                originalLanguage = entity.originalLanguage,
                popularity = entity.popularity,
                status = entity.status,
                tagline = entity.tagline,
                type = entity.type,
                homepage = entity.homepage,
                networks = emptyList(),
                seasons = emptyList(),
                credits = null,
                videos = emptyList()
            )
        }
    }
}