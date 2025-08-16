package com.kybers.stream.di

import android.content.Context
import androidx.room.Room
import com.kybers.stream.data.local.dao.FavoriteDao
import com.kybers.stream.data.local.dao.PlaybackProgressDao
import com.kybers.stream.data.local.dao.UserPreferencesDao
import com.kybers.stream.data.local.database.KybersStreamDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideKybersStreamDatabase(
        @ApplicationContext context: Context
    ): KybersStreamDatabase {
        return KybersStreamDatabase.create(context)
    }
    
    @Provides
    fun provideFavoriteDao(database: KybersStreamDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    @Provides
    fun providePlaybackProgressDao(database: KybersStreamDatabase): PlaybackProgressDao {
        return database.playbackProgressDao()
    }
    
    @Provides
    fun provideUserPreferencesDao(database: KybersStreamDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
}