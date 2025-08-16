package com.kybers.stream.data.repository

import com.kybers.stream.data.local.dao.EpgDao
import com.kybers.stream.data.local.entity.EpgProgramEntity
import com.kybers.stream.data.local.entity.EpgMetadataEntity
import com.kybers.stream.data.remote.api.XtreamApi
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpgRepositoryImpl @Inject constructor(
    private val epgDao: EpgDao,
    private val xtreamApi: XtreamApi
) : EpgRepository {

    override suspend fun getCurrentProgram(streamId: String): Result<EpgProgram?> {
        return try {
            val currentTime = LocalDateTime.now()
            val entity = epgDao.getCurrentProgram(streamId, currentTime)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextProgram(streamId: String): Result<EpgProgram?> {
        return try {
            val currentTime = LocalDateTime.now()
            val entity = epgDao.getNextProgram(streamId, currentTime)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChannelEpg(streamId: String): Result<ChannelEpg> {
        return try {
            val currentTime = LocalDateTime.now()
            val dayStart = currentTime.toLocalDate().atStartOfDay()
            val dayEnd = dayStart.plusDays(1)
            
            val currentProgram = epgDao.getCurrentProgram(streamId, currentTime)?.toDomain()
            val nextProgram = epgDao.getNextProgram(streamId, currentTime)?.toDomain()
            val todayPrograms = epgDao.getProgramsForDay(streamId, dayStart, dayEnd).map { it.toDomain() }
            
            val channelEpg = ChannelEpg(
                streamId = streamId,
                channelName = "", // Will be populated from channel data
                currentProgram = currentProgram,
                nextProgram = nextProgram,
                todayPrograms = todayPrograms
            )
            
            Result.success(channelEpg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentProgramsForChannels(streamIds: List<String>): Result<List<ChannelEpg>> {
        return try {
            val currentTime = LocalDateTime.now()
            val results = mutableListOf<ChannelEpg>()
            
            for (streamId in streamIds) {
                val currentProgram = epgDao.getCurrentProgram(streamId, currentTime)?.toDomain()
                val nextProgram = epgDao.getNextProgram(streamId, currentTime)?.toDomain()
                
                results.add(
                    ChannelEpg(
                        streamId = streamId,
                        channelName = "", // Will be populated from channel data
                        currentProgram = currentProgram,
                        nextProgram = nextProgram
                    )
                )
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProgramsForDay(streamId: String, date: LocalDateTime): Result<List<EpgProgram>> {
        return try {
            val dayStart = date.toLocalDate().atStartOfDay()
            val dayEnd = dayStart.plusDays(1)
            val entities = epgDao.getProgramsForDay(streamId, dayStart, dayEnd)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProgramsInTimeRange(
        streamId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Result<List<EpgProgram>> {
        return try {
            val entities = epgDao.getProgramsInTimeRange(streamId, startTime, endTime)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentProgramFlow(streamId: String): Flow<EpgProgram?> {
        val currentTime = LocalDateTime.now()
        return epgDao.getCurrentProgramFlow(streamId, currentTime).map { it?.toDomain() }
    }

    override fun getChannelEpgFlow(streamId: String): Flow<ChannelEpg> {
        return getCurrentProgramFlow(streamId).map { currentProgram ->
            ChannelEpg(
                streamId = streamId,
                channelName = "",
                currentProgram = currentProgram,
                nextProgram = null // Could be optimized to include next program
            )
        }
    }

    override suspend fun refreshEpgData(forceRefresh: Boolean): Result<EpgData> {
        return try {
            val metadata = epgDao.getMetadata(EpgSource.SHORT_EPG.name)
            val shouldRefresh = forceRefresh || metadata == null || 
                               metadata.lastUpdated.isBefore(LocalDateTime.now().minusHours(1))
            
            if (shouldRefresh) {
                // Fetch EPG data from API
                refreshFromApi()
            }
            
            val totalPrograms = epgDao.getTotalProgramCount()
            val epgData = EpgData(
                source = EpgSource.SHORT_EPG,
                lastUpdated = LocalDateTime.now(),
                channels = emptyList(), // Could be populated if needed
                totalPrograms = totalPrograms
            )
            
            Result.success(epgData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshChannelEpg(streamId: String): Result<Unit> {
        return try {
            // Fetch specific channel EPG from API
            refreshChannelFromApi(streamId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearOldEpgData(): Result<Unit> {
        return try {
            val cutoffTime = LocalDateTime.now().minusDays(1)
            epgDao.deleteOldPrograms(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllEpgData(): Result<Unit> {
        return try {
            epgDao.deleteAllPrograms()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEpgMetadata(): Result<EpgData> {
        return try {
            val metadata = epgDao.getAllMetadata()
            val totalPrograms = epgDao.getTotalProgramCount()
            
            val epgData = EpgData(
                source = EpgSource.SHORT_EPG,
                lastUpdated = metadata.firstOrNull()?.lastUpdated ?: LocalDateTime.now(),
                channels = emptyList(),
                totalPrograms = totalPrograms
            )
            
            Result.success(epgData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPrograms(
        query: String,
        timeRange: Pair<LocalDateTime, LocalDateTime>?
    ): Result<List<EpgProgram>> {
        return try {
            // Simple implementation - could be enhanced with FTS
            val allPrograms = if (timeRange != null) {
                // Get programs in time range for all channels
                val channelsWithEpg = epgDao.getChannelsWithEpg(timeRange.first, timeRange.second)
                channelsWithEpg.flatMap { streamId ->
                    epgDao.getProgramsInTimeRange(streamId, timeRange.first, timeRange.second)
                }
            } else {
                // For now, limit search to today's programs
                val today = LocalDateTime.now().toLocalDate().atStartOfDay()
                val tomorrow = today.plusDays(1)
                val channelsWithEpg = epgDao.getChannelsWithEpg(today, tomorrow)
                channelsWithEpg.flatMap { streamId ->
                    epgDao.getProgramsInTimeRange(streamId, today, tomorrow)
                }
            }
            
            val filteredPrograms = allPrograms
                .filter { it.title.contains(query, ignoreCase = true) || 
                         it.description?.contains(query, ignoreCase = true) == true }
                .map { it.toDomain() }
            
            Result.success(filteredPrograms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun refreshFromApi() {
        // Implementation would fetch EPG data from Xtream Codes API
        // For now, this is a placeholder
        val metadata = EpgMetadataEntity(
            source = EpgSource.SHORT_EPG.name,
            lastUpdated = LocalDateTime.now(),
            totalPrograms = 0
        )
        epgDao.insertMetadata(metadata)
    }

    private suspend fun refreshChannelFromApi(streamId: String) {
        // Implementation would fetch specific channel EPG from API
        // For now, this is a placeholder
    }

    private fun EpgProgramEntity.toDomain(): EpgProgram {
        return EpgProgram(
            id = id,
            streamId = streamId,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            category = category,
            rating = rating,
            language = language,
            isLive = isLive
        )
    }
}