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
import com.kybers.stream.data.local.entity.FavoriteEntity
import com.kybers.stream.data.local.entity.PlaybackProgressEntity
import com.kybers.stream.data.local.entity.UserPreferencesEntity
import com.kybers.stream.domain.model.ContentType

@TypeConverters(Converters::class)
@Database(
    entities = [
        FavoriteEntity::class,
        PlaybackProgressEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KybersStreamDatabase : RoomDatabase() {
    
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    companion object {
        const val DATABASE_NAME = "kybers_stream_database"
        
        fun create(context: Context): KybersStreamDatabase {
            return Room.databaseBuilder(
                context,
                KybersStreamDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
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
}