package com.kybers.stream.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["streamId"]),
        Index(value = ["startTime", "endTime"]),
        Index(value = ["streamId", "startTime"]),
        Index(value = ["streamId", "endTime"])
    ]
)
data class EpgProgramEntity(
    @PrimaryKey
    val id: String,
    val streamId: String,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val category: String? = null,
    val rating: String? = null,
    val language: String? = null,
    val isLive: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity(
    tableName = "epg_metadata",
    indices = [
        Index(value = ["source"]),
        Index(value = ["lastUpdated"])
    ]
)
data class EpgMetadataEntity(
    @PrimaryKey
    val source: String, // EpgSource.name
    val lastUpdated: LocalDateTime,
    val totalPrograms: Int,
    val lastFullSync: LocalDateTime? = null,
    val nextScheduledSync: LocalDateTime? = null
)