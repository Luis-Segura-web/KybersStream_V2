package com.kybers.stream.data.repository

import com.kybers.stream.data.local.dao.PlaybackProgressDao
import com.kybers.stream.data.local.entity.PlaybackProgressEntity
import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackProgressRepositoryImpl @Inject constructor(
    private val playbackProgressDao: PlaybackProgressDao
) : PlaybackProgressRepository {
    
    override fun getAllProgress(): Flow<List<PlaybackProgress>> {
        return playbackProgressDao.getAllProgress().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getContinueWatchingItems(limit: Int): Flow<List<PlaybackProgress>> {
        // Solo incluir VOD y SERIES/EPISODES para "continuar viendo"
        val contentTypes = listOf(ContentType.VOD, ContentType.SERIES, ContentType.EPISODE)
        return playbackProgressDao.getContinueWatchingItems(contentTypes, limit).map { entities ->
            entities
                .filter { it.positionMs > 0 && !it.isNearEnd() } // Filtrar contenido no iniciado o casi terminado
                .map { it.toDomainModel() }
        }
    }
    
    override fun getProgress(contentId: String, contentType: ContentType): Flow<PlaybackProgress?> {
        return playbackProgressDao.getProgressFlow(contentId, contentType).map { entity ->
            entity?.toDomainModel()
        }
    }
    
    override suspend fun saveProgress(progress: PlaybackProgress): Result<Unit> {
        return try {
            val entity = progress.toEntity()
            playbackProgressDao.insertProgress(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeProgress(contentId: String, contentType: ContentType): Result<Unit> {
        return try {
            playbackProgressDao.removeProgress(contentId, contentType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeCompletedProgress(): Result<Unit> {
        return try {
            playbackProgressDao.removeCompletedProgress()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeOldProgress(daysToKeep: Int): Result<Unit> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            playbackProgressDao.removeOldProgress(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllProgress(): Result<Unit> {
        return try {
            playbackProgressDao.clearAllProgress()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun PlaybackProgressEntity.toDomainModel(): PlaybackProgress {
    return PlaybackProgress(
        contentId = contentId,
        contentType = contentType,
        positionMs = positionMs,
        durationMs = durationMs,
        lastUpdated = lastUpdated
    )
}

private fun PlaybackProgress.toEntity(): PlaybackProgressEntity {
    return PlaybackProgressEntity(
        contentId = contentId,
        contentType = contentType,
        positionMs = positionMs,
        durationMs = durationMs,
        lastUpdated = lastUpdated
    )
}

private fun PlaybackProgressEntity.isNearEnd(): Boolean {
    return if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()) > 0.95f
    } else false
}