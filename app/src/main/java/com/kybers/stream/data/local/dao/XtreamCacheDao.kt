package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface XtreamCacheDao {
    
    // =============== MOVIES ===============
    @Query("SELECT * FROM xtream_movies_cache WHERE userHash = :userHash")
    suspend fun getAllMovies(userHash: String): List<XtreamMovieCacheEntity>
    
    @Query("SELECT * FROM xtream_movies_cache WHERE userHash = :userHash AND categoryId = :categoryId")
    suspend fun getMoviesByCategory(userHash: String, categoryId: String): List<XtreamMovieCacheEntity>
    
    @Query("SELECT * FROM xtream_movies_cache WHERE streamId = :streamId AND userHash = :userHash")
    suspend fun getMovieById(streamId: String, userHash: String): XtreamMovieCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<XtreamMovieCacheEntity>)
    
    @Query("DELETE FROM xtream_movies_cache WHERE userHash = :userHash")
    suspend fun clearMovies(userHash: String)
    
    @Query("DELETE FROM xtream_movies_cache WHERE userHash = :userHash AND lastSync < :cutoffTime")
    suspend fun deleteExpiredMovies(userHash: String, cutoffTime: LocalDateTime)
    
    // =============== SERIES ===============
    @Query("SELECT * FROM xtream_series_cache WHERE userHash = :userHash")
    suspend fun getAllSeries(userHash: String): List<XtreamSeriesCacheEntity>
    
    @Query("SELECT * FROM xtream_series_cache WHERE userHash = :userHash AND categoryId = :categoryId")
    suspend fun getSeriesByCategory(userHash: String, categoryId: String): List<XtreamSeriesCacheEntity>
    
    @Query("SELECT * FROM xtream_series_cache WHERE seriesId = :seriesId AND userHash = :userHash")
    suspend fun getSeriesById(seriesId: String, userHash: String): XtreamSeriesCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<XtreamSeriesCacheEntity>)
    
    @Query("DELETE FROM xtream_series_cache WHERE userHash = :userHash")
    suspend fun clearSeries(userHash: String)
    
    @Query("DELETE FROM xtream_series_cache WHERE userHash = :userHash AND lastSync < :cutoffTime")
    suspend fun deleteExpiredSeries(userHash: String, cutoffTime: LocalDateTime)
    
    // =============== CHANNELS ===============
    @Query("SELECT * FROM xtream_channels_cache WHERE userHash = :userHash")
    suspend fun getAllChannels(userHash: String): List<XtreamChannelCacheEntity>
    
    @Query("SELECT * FROM xtream_channels_cache WHERE userHash = :userHash AND categoryId = :categoryId")
    suspend fun getChannelsByCategory(userHash: String, categoryId: String): List<XtreamChannelCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<XtreamChannelCacheEntity>)
    
    @Query("DELETE FROM xtream_channels_cache WHERE userHash = :userHash")
    suspend fun clearChannels(userHash: String)
    
    @Query("DELETE FROM xtream_channels_cache WHERE userHash = :userHash AND lastSync < :cutoffTime")
    suspend fun deleteExpiredChannels(userHash: String, cutoffTime: LocalDateTime)
    
    // =============== CATEGORIES ===============
    @Query("SELECT * FROM xtream_categories_cache WHERE userHash = :userHash AND type = :type")
    suspend fun getCategoriesByType(userHash: String, type: String): List<XtreamCategoryCacheEntity>
    
    @Query("SELECT * FROM xtream_categories_cache WHERE userHash = :userHash")
    suspend fun getAllCategories(userHash: String): List<XtreamCategoryCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<XtreamCategoryCacheEntity>)
    
    @Query("DELETE FROM xtream_categories_cache WHERE userHash = :userHash")
    suspend fun clearCategories(userHash: String)
    
    @Query("DELETE FROM xtream_categories_cache WHERE userHash = :userHash AND lastSync < :cutoffTime")
    suspend fun deleteExpiredCategories(userHash: String, cutoffTime: LocalDateTime)
    
    // =============== SYNC METADATA ===============
    @Query("SELECT * FROM xtream_sync_metadata WHERE userHash = :userHash")
    suspend fun getSyncMetadata(userHash: String): XtreamSyncMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: XtreamSyncMetadataEntity)
    
    @Query("DELETE FROM xtream_sync_metadata WHERE userHash = :userHash")
    suspend fun clearSyncMetadata(userHash: String)
    
    // =============== CACHE VALIDATION ===============
    @Query("SELECT COUNT(*) FROM xtream_movies_cache WHERE userHash = :userHash AND lastSync > :cutoffTime")
    suspend fun getValidMoviesCount(userHash: String, cutoffTime: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM xtream_series_cache WHERE userHash = :userHash AND lastSync > :cutoffTime")
    suspend fun getValidSeriesCount(userHash: String, cutoffTime: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM xtream_channels_cache WHERE userHash = :userHash AND lastSync > :cutoffTime")
    suspend fun getValidChannelsCount(userHash: String, cutoffTime: LocalDateTime): Int
    
    // =============== CLEANUP ===============
    @Query("DELETE FROM xtream_movies_cache WHERE userHash != :currentUserHash")
    suspend fun cleanupOtherUsersMovies(currentUserHash: String)
    
    @Query("DELETE FROM xtream_series_cache WHERE userHash != :currentUserHash")
    suspend fun cleanupOtherUsersSeries(currentUserHash: String)
    
    @Query("DELETE FROM xtream_channels_cache WHERE userHash != :currentUserHash")
    suspend fun cleanupOtherUsersChannels(currentUserHash: String)
    
    @Query("DELETE FROM xtream_categories_cache WHERE userHash != :currentUserHash")
    suspend fun cleanupOtherUsersCategories(currentUserHash: String)
    
    @Query("DELETE FROM xtream_sync_metadata WHERE userHash != :currentUserHash")
    suspend fun cleanupOtherUsersSyncMetadata(currentUserHash: String)
    
    // =============== TMDB ID QUERIES ===============
    @Query("SELECT DISTINCT tmdbId FROM xtream_movies_cache WHERE userHash = :userHash AND tmdbId IS NOT NULL AND tmdbId != ''")
    suspend fun getAllMovieTmdbIds(userHash: String): List<String>
    
    @Query("SELECT DISTINCT tmdbId FROM xtream_series_cache WHERE userHash = :userHash AND tmdbId IS NOT NULL AND tmdbId != ''")
    suspend fun getAllSeriesTmdbIds(userHash: String): List<String>
    
    @Query("SELECT * FROM xtream_movies_cache WHERE userHash = :userHash AND tmdbId = :tmdbId")
    suspend fun getMoviesByTmdbId(userHash: String, tmdbId: String): List<XtreamMovieCacheEntity>
    
    @Query("SELECT * FROM xtream_series_cache WHERE userHash = :userHash AND tmdbId = :tmdbId")
    suspend fun getSeriesByTmdbId(userHash: String, tmdbId: String): List<XtreamSeriesCacheEntity>
}