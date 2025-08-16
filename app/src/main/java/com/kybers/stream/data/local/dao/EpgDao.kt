package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.EpgProgramEntity
import com.kybers.stream.data.local.entity.EpgMetadataEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EpgDao {
    
    // Consultas para programas EPG
    @Query("SELECT * FROM epg_programs WHERE streamId = :streamId AND startTime <= :currentTime AND endTime > :currentTime LIMIT 1")
    suspend fun getCurrentProgram(streamId: String, currentTime: LocalDateTime): EpgProgramEntity?
    
    @Query("SELECT * FROM epg_programs WHERE streamId = :streamId AND startTime > :currentTime ORDER BY startTime ASC LIMIT 1")
    suspend fun getNextProgram(streamId: String, currentTime: LocalDateTime): EpgProgramEntity?
    
    @Query("""
        SELECT * FROM epg_programs 
        WHERE streamId = :streamId 
        AND startTime >= :dayStart 
        AND startTime < :dayEnd 
        ORDER BY startTime ASC
    """)
    suspend fun getProgramsForDay(
        streamId: String, 
        dayStart: LocalDateTime, 
        dayEnd: LocalDateTime
    ): List<EpgProgramEntity>
    
    @Query("""
        SELECT * FROM epg_programs 
        WHERE streamId = :streamId 
        AND startTime >= :timeStart 
        AND startTime <= :timeEnd 
        ORDER BY startTime ASC
    """)
    suspend fun getProgramsInTimeRange(
        streamId: String,
        timeStart: LocalDateTime,
        timeEnd: LocalDateTime
    ): List<EpgProgramEntity>
    
    @Query("""
        SELECT * FROM epg_programs 
        WHERE streamId IN (:streamIds) 
        AND startTime <= :currentTime 
        AND endTime > :currentTime
        ORDER BY streamId ASC
    """)
    suspend fun getCurrentProgramsForChannels(
        streamIds: List<String>,
        currentTime: LocalDateTime
    ): List<EpgProgramEntity>
    
    @Query("""
        SELECT DISTINCT streamId FROM epg_programs 
        WHERE startTime >= :timeStart 
        AND startTime <= :timeEnd
    """)
    suspend fun getChannelsWithEpg(
        timeStart: LocalDateTime,
        timeEnd: LocalDateTime
    ): List<String>
    
    // Flow para actualizaciones en tiempo real
    @Query("""
        SELECT * FROM epg_programs 
        WHERE streamId = :streamId 
        AND startTime <= :currentTime 
        AND endTime > :currentTime 
        LIMIT 1
    """)
    fun getCurrentProgramFlow(streamId: String, currentTime: LocalDateTime): Flow<EpgProgramEntity?>
    
    // Operaciones de escritura
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: EpgProgramEntity)
    
    @Update
    suspend fun updateProgram(program: EpgProgramEntity)
    
    @Delete
    suspend fun deleteProgram(program: EpgProgramEntity)
    
    // Limpieza
    @Query("DELETE FROM epg_programs WHERE endTime < :cutoffTime")
    suspend fun deleteOldPrograms(cutoffTime: LocalDateTime): Int
    
    @Query("DELETE FROM epg_programs WHERE streamId = :streamId")
    suspend fun deleteProgramsForChannel(streamId: String): Int
    
    @Query("DELETE FROM epg_programs")
    suspend fun deleteAllPrograms(): Int
    
    // Metadatos
    @Query("SELECT * FROM epg_metadata")
    suspend fun getAllMetadata(): List<EpgMetadataEntity>
    
    @Query("SELECT * FROM epg_metadata WHERE source = :source")
    suspend fun getMetadata(source: String): EpgMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: EpgMetadataEntity)
    
    @Query("DELETE FROM epg_metadata WHERE source = :source")
    suspend fun deleteMetadata(source: String): Int
    
    // Estadísticas
    @Query("SELECT COUNT(*) FROM epg_programs")
    suspend fun getTotalProgramCount(): Int
    
    @Query("SELECT COUNT(*) FROM epg_programs WHERE streamId = :streamId")
    suspend fun getProgramCountForChannel(streamId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM epg_programs 
        WHERE startTime >= :timeStart 
        AND startTime <= :timeEnd
    """)
    suspend fun getProgramCountInTimeRange(
        timeStart: LocalDateTime,
        timeEnd: LocalDateTime
    ): Int
}