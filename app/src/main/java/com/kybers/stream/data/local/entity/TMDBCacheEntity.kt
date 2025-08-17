package com.kybers.stream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kybers.stream.domain.model.*
import java.time.LocalDateTime

@Entity(tableName = "tmdb_movie_cache")
data class TMDBMovieCacheEntity(
    @PrimaryKey val tmdbId: String,
    val title: String,
    val originalTitle: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val runtime: Int?,
    val genres: List<String>, // JSON serializado
    val adult: Boolean,
    val budget: Long?,
    val revenue: Long?,
    val tagline: String?,
    val status: String?,
    val originalLanguage: String,
    val popularity: Double,
    val homepage: String?,
    val imdbId: String?,
    val lastSync: LocalDateTime
)

@Entity(tableName = "tmdb_series_cache")
data class TMDBSeriesCacheEntity(
    @PrimaryKey val tmdbId: String,
    val name: String,
    val originalName: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<String>, // JSON serializado
    val adult: Boolean,
    val episodeRunTime: List<Int>,
    val inProduction: Boolean,
    val numberOfEpisodes: Int,
    val numberOfSeasons: Int,
    val originalLanguage: String,
    val popularity: Double,
    val status: String?,
    val tagline: String?,
    val type: String?,
    val homepage: String?,
    val lastSync: LocalDateTime
)

@Entity(tableName = "tmdb_sync_metadata")
data class TMDBSyncMetadataEntity(
    @PrimaryKey val id: Int = 1, // Solo un registro
    val lastCleanup: LocalDateTime,
    val totalMovies: Int,
    val totalSeries: Int
)

// Converters para datos TMDB
class TMDBCacheConverters {
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}