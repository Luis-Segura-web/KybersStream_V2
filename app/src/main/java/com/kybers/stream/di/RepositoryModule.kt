package com.kybers.stream.di

import com.kybers.stream.data.repository.FavoriteRepositoryImpl
import com.kybers.stream.data.repository.PlaybackProgressRepositoryImpl
import com.kybers.stream.data.repository.UserPreferencesRepositoryImpl
import com.kybers.stream.data.repository.UserRepositoryImpl
import com.kybers.stream.data.repository.XtreamRepositoryImpl
import com.kybers.stream.domain.repository.FavoriteRepository
import com.kybers.stream.domain.repository.PlaybackProgressRepository
import com.kybers.stream.domain.repository.UserPreferencesRepository
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindXtreamRepository(
        xtreamRepositoryImpl: XtreamRepositoryImpl
    ): XtreamRepository
    
    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
    
    @Binds
    @Singleton
    abstract fun bindPlaybackProgressRepository(
        playbackProgressRepositoryImpl: PlaybackProgressRepositoryImpl
    ): PlaybackProgressRepository
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}