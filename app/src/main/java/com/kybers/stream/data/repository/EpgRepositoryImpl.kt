package com.kybers.stream.data.repository

import com.kybers.stream.data.local.dao.EpgDao
import com.kybers.stream.data.local.entity.EpgProgramEntity
import com.kybers.stream.data.local.entity.EpgMetadataEntity
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.EpgRepository
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpgRepositoryImpl @Inject constructor(
    private val epgDao: EpgDao,
    private val xtreamRepository: XtreamRepository
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
                // Update metadata first to indicate refresh started
                val newMetadata = EpgMetadataEntity(
                    source = EpgSource.SHORT_EPG.name,
                    lastUpdated = LocalDateTime.now(),
                    totalPrograms = 0
                )
                epgDao.insertMetadata(newMetadata)
            }
            
            val totalPrograms = epgDao.getTotalProgramCount()
            val epgData = EpgData(
                source = EpgSource.SHORT_EPG,
                lastUpdated = metadata?.lastUpdated ?: LocalDateTime.now(),
                channels = emptyList(),
                totalPrograms = totalPrograms
            )
            
            Result.success(epgData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // New method to refresh EPG for a list of channels
    suspend fun refreshEpgForChannels(streamIds: List<String>): Result<Unit> {
        return try {
            // Refresh EPG for each channel
            streamIds.forEach { streamId ->
                refreshChannelFromApi(streamId)
            }
            
            // Update metadata with total program count
            val totalPrograms = epgDao.getTotalProgramCount()
            val metadata = EpgMetadataEntity(
                source = EpgSource.SHORT_EPG.name,
                lastUpdated = LocalDateTime.now(),
                totalPrograms = totalPrograms
            )
            epgDao.insertMetadata(metadata)
            
            Result.success(Unit)
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
        // Fetch EPG data from Xtream Codes API using XtreamRepository
        val metadata = EpgMetadataEntity(
            source = EpgSource.SHORT_EPG.name,
            lastUpdated = LocalDateTime.now(),
            totalPrograms = 0
        )
        epgDao.insertMetadata(metadata)
    }

    private suspend fun refreshChannelFromApi(streamId: String) {
        // Fetch specific channel EPG from Xtream API
        when (val result = xtreamRepository.getShortEpg(streamId, limit = 50)) {
            is XtreamResult.Success -> {
                val epgPrograms = result.data.epgListings.mapNotNull { listing ->
                    parseEpgListing(listing, streamId)
                }
                
                if (epgPrograms.isNotEmpty()) {
                    // Delete old programs for this channel
                    epgDao.deleteProgramsForChannel(streamId)
                    // Insert new programs
                    epgDao.insertPrograms(epgPrograms)
                }
            }
            is XtreamResult.Error -> {
                // Log error but don't throw exception to avoid breaking the entire refresh
                println("Error fetching EPG for stream $streamId: ${result.message}")
            }
            is XtreamResult.Loading -> {
                // Should not happen in this context
            }
        }
    }
    
    private fun parseEpgListing(listing: EpgListing, streamId: String): EpgProgramEntity? {
        return try {
            // Parse datetime strings - Xtream Codes typically uses Unix timestamps or formatted dates
            val startTime = parseDateTime(listing.start)
            val endTime = parseDateTime(listing.stop)
            
            if (startTime != null && endTime != null) {
                EpgProgramEntity(
                    id = "${streamId}_${listing.id}_${listing.start}",
                    streamId = streamId,
                    title = listing.title.ifEmpty { "Sin título" },
                    description = listing.description?.takeIf { it.isNotBlank() },
                    startTime = startTime,
                    endTime = endTime,
                    category = null,
                    rating = null,
                    language = null,
                    isLive = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error parsing EPG listing: ${e.message}")
            null
        }
    }
    
    private fun parseDateTime(dateTimeStr: String): LocalDateTime? {
        if (dateTimeStr.isBlank()) return null
        
        return try {
            // Try parsing as Unix timestamp first
            val timestamp = dateTimeStr.toLongOrNull()
            if (timestamp != null && timestamp > 0) {
                java.time.Instant.ofEpochSecond(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            } else {
                // Try common datetime formats
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                
                for (formatter in formatters) {
                    try {
                        return LocalDateTime.parse(dateTimeStr, formatter)
                    } catch (e: DateTimeParseException) {
                        continue
                    }
                }
                null
            }
        } catch (e: Exception) {
            null
        }
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