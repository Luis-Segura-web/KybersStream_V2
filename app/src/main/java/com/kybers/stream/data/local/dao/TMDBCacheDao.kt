package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.TMDBMovieCacheEntity
import com.kybers.stream.data.local.entity.TMDBSeriesCacheEntity
import com.kybers.stream.data.local.entity.TMDBSyncMetadataEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TMDBCacheDao {
    
    // =============== MOVIES ===============
    @Query("SELECT * FROM tmdb_movie_cache WHERE tmdbId = :tmdbId")
    suspend fun getMovieById(tmdbId: String): TMDBMovieCacheEntity?
    
    @Query("SELECT * FROM tmdb_movie_cache WHERE tmdbId IN (:tmdbIds)")
    suspend fun getMoviesByIds(tmdbIds: List<String>): List<TMDBMovieCacheEntity>
    
    @Query("SELECT * FROM tmdb_movie_cache WHERE lastSync > :cutoffTime")
    suspend fun getValidMovies(cutoffTime: LocalDateTime): List<TMDBMovieCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: TMDBMovieCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<TMDBMovieCacheEntity>)
    
    @Query("DELETE FROM tmdb_movie_cache WHERE tmdbId = :tmdbId")
    suspend fun deleteMovie(tmdbId: String)
    
    @Query("DELETE FROM tmdb_movie_cache WHERE lastSync < :cutoffTime")
    suspend fun deleteExpiredMovies(cutoffTime: LocalDateTime)
    
    @Query("DELETE FROM tmdb_movie_cache")
    suspend fun clearAllMovies()
    
    // =============== SERIES ===============
    @Query("SELECT * FROM tmdb_series_cache WHERE tmdbId = :tmdbId")
    suspend fun getSeriesById(tmdbId: String): TMDBSeriesCacheEntity?
    
    @Query("SELECT * FROM tmdb_series_cache WHERE tmdbId IN (:tmdbIds)")
    suspend fun getSeriesByIds(tmdbIds: List<String>): List<TMDBSeriesCacheEntity>
    
    @Query("SELECT * FROM tmdb_series_cache WHERE lastSync > :cutoffTime")
    suspend fun getValidSeries(cutoffTime: LocalDateTime): List<TMDBSeriesCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: TMDBSeriesCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesList(series: List<TMDBSeriesCacheEntity>)
    
    @Query("DELETE FROM tmdb_series_cache WHERE tmdbId = :tmdbId")
    suspend fun deleteSeries(tmdbId: String)
    
    @Query("DELETE FROM tmdb_series_cache WHERE lastSync < :cutoffTime")
    suspend fun deleteExpiredSeries(cutoffTime: LocalDateTime)
    
    @Query("DELETE FROM tmdb_series_cache")
    suspend fun clearAllSeries()
    
    // =============== SYNC METADATA ===============
    @Query("SELECT * FROM tmdb_sync_metadata WHERE id = 1")
    suspend fun getSyncMetadata(): TMDBSyncMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: TMDBSyncMetadataEntity)
    
    @Query("DELETE FROM tmdb_sync_metadata")
    suspend fun clearSyncMetadata()
    
    // =============== CACHE VALIDATION ===============
    @Query("SELECT COUNT(*) FROM tmdb_movie_cache WHERE lastSync > :cutoffTime")
    suspend fun getValidMoviesCount(cutoffTime: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM tmdb_series_cache WHERE lastSync > :cutoffTime")
    suspend fun getValidSeriesCount(cutoffTime: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM tmdb_movie_cache")
    suspend fun getTotalMoviesCount(): Int
    
    @Query("SELECT COUNT(*) FROM tmdb_series_cache")
    suspend fun getTotalSeriesCount(): Int
    
    // =============== CLEANUP OPERATIONS ===============
    @Transaction
    suspend fun cleanupExpiredData() {
        val cutoffTime = LocalDateTime.now().minusDays(7)
        deleteExpiredMovies(cutoffTime)
        deleteExpiredSeries(cutoffTime)
        
        // Actualizar metadata de limpieza
        val currentMetadata = getSyncMetadata()
        val newMetadata = TMDBSyncMetadataEntity(
            lastCleanup = LocalDateTime.now(),
            totalMovies = getTotalMoviesCount(),
            totalSeries = getTotalSeriesCount()
        )
        insertSyncMetadata(newMetadata)
    }
    
    // =============== BATCH OPERATIONS ===============
    @Transaction
    suspend fun updateMoviesCache(movies: List<TMDBMovieCacheEntity>) {
        insertMovies(movies)
    }
    
    @Transaction
    suspend fun updateSeriesCache(series: List<TMDBSeriesCacheEntity>) {
        insertSeriesList(series)
    }
    
    // =============== SEARCH OPERATIONS ===============
    @Query("SELECT * FROM tmdb_movie_cache WHERE title LIKE :query OR originalTitle LIKE :query ORDER BY popularity DESC")
    suspend fun searchMovies(query: String): List<TMDBMovieCacheEntity>
    
    @Query("SELECT * FROM tmdb_series_cache WHERE name LIKE :query OR originalName LIKE :query ORDER BY popularity DESC")
    suspend fun searchSeries(query: String): List<TMDBSeriesCacheEntity>
    
    // =============== POPULAR CONTENT ===============
    @Query("SELECT * FROM tmdb_movie_cache WHERE lastSync > :cutoffTime ORDER BY popularity DESC LIMIT :limit")
    suspend fun getPopularMovies(cutoffTime: LocalDateTime, limit: Int = 20): List<TMDBMovieCacheEntity>
    
    @Query("SELECT * FROM tmdb_series_cache WHERE lastSync > :cutoffTime ORDER BY popularity DESC LIMIT :limit")
    suspend fun getPopularSeries(cutoffTime: LocalDateTime, limit: Int = 20): List<TMDBSeriesCacheEntity>
    
    @Query("SELECT * FROM tmdb_movie_cache WHERE lastSync > :cutoffTime ORDER BY voteAverage DESC LIMIT :limit")
    suspend fun getTopRatedMovies(cutoffTime: LocalDateTime, limit: Int = 20): List<TMDBMovieCacheEntity>
    
    @Query("SELECT * FROM tmdb_series_cache WHERE lastSync > :cutoffTime ORDER BY voteAverage DESC LIMIT :limit")
    suspend fun getTopRatedSeries(cutoffTime: LocalDateTime, limit: Int = 20): List<TMDBSeriesCacheEntity>
}