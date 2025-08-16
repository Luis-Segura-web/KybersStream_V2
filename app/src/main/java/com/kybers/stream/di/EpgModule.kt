package com.kybers.stream.di

import com.kybers.stream.data.repository.EpgRepositoryImpl
import com.kybers.stream.domain.repository.EpgRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EpgModule {
    
    @Binds
    @Singleton
    abstract fun bindEpgRepository(
        epgRepositoryImpl: EpgRepositoryImpl
    ): EpgRepository
}