package com.kybers.stream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

@Entity(tableName = "xtream_movies_cache")
data class XtreamMovieCacheEntity(
    @PrimaryKey val streamId: String,
    val name: String,
    val icon: String?,
    val categoryId: String,
    val rating: String?,
    val rating5Based: Double,
    val addedTimestamp: Long,
    val isAdult: Boolean,
    val containerExtension: String?,
    val tmdbId: String?,
    val userHash: String, // Para identificar a qué usuario pertenece
    val lastSync: LocalDateTime
)

@Entity(tableName = "xtream_series_cache")
data class XtreamSeriesCacheEntity(
    @PrimaryKey val seriesId: String,
    val name: String,
    val cover: String?,
    val categoryId: String,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val lastModified: Long,
    val rating: String?,
    val rating5Based: Double,
    val backdropPath: List<String>,
    val youtubeTrailer: String?,
    val episodeRunTime: String?,
    val tmdbId: String?,
    val userHash: String,
    val lastSync: LocalDateTime
)

@Entity(tableName = "xtream_channels_cache")
data class XtreamChannelCacheEntity(
    @PrimaryKey val streamId: String,
    val name: String,
    val icon: String?,
    val categoryId: String,
    val epgChannelId: String?,
    val isAdult: Boolean,
    val tvArchive: Boolean,
    val tvArchiveDuration: Int,
    val addedTimestamp: Long,
    val userHash: String,
    val lastSync: LocalDateTime
)

@Entity(tableName = "xtream_categories_cache")
data class XtreamCategoryCacheEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val type: String, // "live", "vod", "series"
    val userHash: String,
    val lastSync: LocalDateTime
)

@Entity(tableName = "xtream_sync_metadata")
data class XtreamSyncMetadataEntity(
    @PrimaryKey val userHash: String,
    val lastFullSync: LocalDateTime,
    val moviesCount: Int,
    val seriesCount: Int,
    val channelsCount: Int,
    val categoriesCount: Int,
    val isValid: Boolean
)

// Converters para listas
class XtreamCacheConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}