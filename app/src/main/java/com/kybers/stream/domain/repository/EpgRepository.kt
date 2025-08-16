package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface EpgRepository {
    
    // Consultas principales
    suspend fun getCurrentProgram(streamId: String): Result<EpgProgram?>
    suspend fun getNextProgram(streamId: String): Result<EpgProgram?>
    suspend fun getChannelEpg(streamId: String): Result<ChannelEpg>
    suspend fun getCurrentProgramsForChannels(streamIds: List<String>): Result<List<ChannelEpg>>
    
    // Consultas por tiempo
    suspend fun getProgramsForDay(streamId: String, date: LocalDateTime): Result<List<EpgProgram>>
    suspend fun getProgramsInTimeRange(
        streamId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Result<List<EpgProgram>>
    
    // Flujos reactivos
    fun getCurrentProgramFlow(streamId: String): Flow<EpgProgram?>
    fun getChannelEpgFlow(streamId: String): Flow<ChannelEpg>
    
    // Actualización de datos
    suspend fun refreshEpgData(forceRefresh: Boolean = false): Result<EpgData>
    suspend fun refreshChannelEpg(streamId: String): Result<Unit>
    
    // Gestión de datos locales
    suspend fun clearOldEpgData(): Result<Unit>
    suspend fun clearAllEpgData(): Result<Unit>
    suspend fun getEpgMetadata(): Result<EpgData>
    
    // Búsqueda
    suspend fun searchPrograms(
        query: String,
        timeRange: Pair<LocalDateTime, LocalDateTime>? = null
    ): Result<List<EpgProgram>>
}