package com.kybers.stream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.kybers.stream.domain.model.ContentType

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["contentId", "contentType"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contentId: String,
    val contentType: ContentType,
    val name: String,
    val imageUrl: String?,
    val categoryId: String?,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playback_progress",
    indices = [Index(value = ["contentId", "contentType"], unique = true)]
)
data class PlaybackProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contentId: String,
    val contentType: ContentType,
    val positionMs: Long,
    val durationMs: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val key: String,
    val value: String
)