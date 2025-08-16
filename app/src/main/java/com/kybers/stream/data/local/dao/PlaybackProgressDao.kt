package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.PlaybackProgressEntity
import com.kybers.stream.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackProgressDao {
    
    @Query("SELECT * FROM playback_progress ORDER BY lastUpdated DESC")
    fun getAllProgress(): Flow<List<PlaybackProgressEntity>>
    
    @Query("SELECT * FROM playback_progress WHERE contentType IN (:contentTypes) ORDER BY lastUpdated DESC LIMIT :limit")
    fun getContinueWatchingItems(contentTypes: List<ContentType>, limit: Int = 20): Flow<List<PlaybackProgressEntity>>
    
    @Query("SELECT * FROM playback_progress WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun getProgress(contentId: String, contentType: ContentType): PlaybackProgressEntity?
    
    @Query("SELECT * FROM playback_progress WHERE contentId = :contentId AND contentType = :contentType")
    fun getProgressFlow(contentId: String, contentType: ContentType): Flow<PlaybackProgressEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: PlaybackProgressEntity): Long
    
    @Update
    suspend fun updateProgress(progress: PlaybackProgressEntity)
    
    @Query("DELETE FROM playback_progress WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun removeProgress(contentId: String, contentType: ContentType): Int
    
    @Query("DELETE FROM playback_progress WHERE positionMs / CAST(durationMs AS REAL) > 0.95")
    suspend fun removeCompletedProgress()
    
    @Query("DELETE FROM playback_progress WHERE lastUpdated < :cutoffTime")
    suspend fun removeOldProgress(cutoffTime: Long)
    
    @Query("DELETE FROM playback_progress")
    suspend fun clearAllProgress()
}