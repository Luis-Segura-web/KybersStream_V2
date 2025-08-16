package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.PlaybackProgress
import kotlinx.coroutines.flow.Flow

interface PlaybackProgressRepository {
    fun getAllProgress(): Flow<List<PlaybackProgress>>
    fun getContinueWatchingItems(limit: Int = 20): Flow<List<PlaybackProgress>>
    fun getProgress(contentId: String, contentType: ContentType): Flow<PlaybackProgress?>
    suspend fun saveProgress(progress: PlaybackProgress): Result<Unit>
    suspend fun removeProgress(contentId: String, contentType: ContentType): Result<Unit>
    suspend fun removeCompletedProgress(): Result<Unit>
    suspend fun removeOldProgress(daysToKeep: Int = 30): Result<Unit>
    suspend fun clearAllProgress(): Result<Unit>
}