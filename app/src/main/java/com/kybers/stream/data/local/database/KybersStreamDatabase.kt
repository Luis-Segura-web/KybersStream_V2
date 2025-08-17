package com.kybers.stream.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.kybers.stream.data.local.dao.FavoriteDao
import com.kybers.stream.data.local.dao.PlaybackProgressDao
import com.kybers.stream.data.local.dao.UserPreferencesDao
import com.kybers.stream.data.local.dao.EpgDao
import com.kybers.stream.data.local.dao.XtreamCacheDao
import com.kybers.stream.data.local.dao.TMDBCacheDao
import com.kybers.stream.data.local.entity.FavoriteEntity
import com.kybers.stream.data.local.entity.PlaybackProgressEntity
import com.kybers.stream.data.local.entity.UserPreferencesEntity
import com.kybers.stream.data.local.entity.EpgProgramEntity
import com.kybers.stream.data.local.entity.EpgMetadataEntity
import com.kybers.stream.data.local.entity.XtreamMovieCacheEntity
import com.kybers.stream.data.local.entity.XtreamSeriesCacheEntity
import com.kybers.stream.data.local.entity.XtreamChannelCacheEntity
import com.kybers.stream.data.local.entity.XtreamCategoryCacheEntity
import com.kybers.stream.data.local.entity.XtreamSyncMetadataEntity
import com.kybers.stream.data.local.entity.TMDBMovieCacheEntity
import com.kybers.stream.data.local.entity.TMDBSeriesCacheEntity
import com.kybers.stream.data.local.entity.TMDBSyncMetadataEntity
import com.kybers.stream.data.local.entity.XtreamCacheConverters
import com.kybers.stream.data.local.entity.TMDBCacheConverters
import com.kybers.stream.domain.model.ContentType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@TypeConverters(Converters::class, XtreamCacheConverters::class, TMDBCacheConverters::class)
@Database(
    entities = [
        FavoriteEntity::class,
        PlaybackProgressEntity::class,
        UserPreferencesEntity::class,
        EpgProgramEntity::class,
        EpgMetadataEntity::class,
        XtreamMovieCacheEntity::class,
        XtreamSeriesCacheEntity::class,
        XtreamChannelCacheEntity::class,
        XtreamCategoryCacheEntity::class,
        XtreamSyncMetadataEntity::class,
        TMDBMovieCacheEntity::class,
        TMDBSeriesCacheEntity::class,
        TMDBSyncMetadataEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KybersStreamDatabase : RoomDatabase() {
    
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun epgDao(): EpgDao
    abstract fun xtreamCacheDao(): XtreamCacheDao
    abstract fun tmdbCacheDao(): TMDBCacheDao
    
    companion object {
        const val DATABASE_NAME = "kybers_stream_database"
        
        fun create(context: Context): KybersStreamDatabase {
            return Room.databaseBuilder(
                context,
                KybersStreamDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}

class Converters {
    @TypeConverter
    fun fromContentType(contentType: ContentType): String {
        return contentType.name
    }
    
    @TypeConverter
    fun toContentType(contentType: String): ContentType {
        return ContentType.valueOf(contentType)
    }
    
    // Converters para LocalDateTime (para EPG)
    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime?): String? {
        return localDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }
}