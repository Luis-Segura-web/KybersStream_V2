package com.kybers.stream.domain.usecase

import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLiveCategoriesUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(): Flow<XtreamResult<List<Category>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getLiveCategories())
    }
}

class GetVodCategoriesUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(): Flow<XtreamResult<List<Category>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getVodCategories())
    }
}

class GetSeriesCategoriesUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(): Flow<XtreamResult<List<Category>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getSeriesCategories())
    }
}

class GetLiveStreamsUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(categoryId: String? = null): Flow<XtreamResult<List<Channel>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getLiveStreams(categoryId))
    }
}

class GetVodStreamsUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(categoryId: String? = null): Flow<XtreamResult<List<Movie>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getVodStreams(categoryId))
    }
}

class GetSeriesUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val databaseCacheManager: DatabaseCacheManager,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(categoryId: String? = null): Flow<XtreamResult<List<Series>>> = flow {
        emit(XtreamResult.Loading)
        
        try {
            val user = userRepository.getCurrentUser().first()
            if (user == null) {
                emit(XtreamResult.Error("Usuario no autenticado", XtreamErrorCode.INVALID_CREDENTIALS))
                return@flow
            }
            
            val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
            
            // Intentar obtener del cache primero
            val cachedSeries = databaseCacheManager.getCachedXtreamSeries(userHash)
            if (cachedSeries.isNotEmpty()) {
                emit(XtreamResult.Success(cachedSeries))
                return@flow
            }
            
            // Si no hay cache, obtener del repositorio
            val result = xtreamRepository.getSeries(categoryId)
            if (result is XtreamResult.Success) {
                // Cache the data
                databaseCacheManager.cacheXtreamSeries(result.data, userHash)
            }
            emit(result)
        } catch (e: Exception) {
            emit(XtreamResult.Error("Error inesperado: ${e.message}", XtreamErrorCode.UNKNOWN))
        }
    }
}

class GetVodInfoUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(vodId: String): Flow<XtreamResult<MovieDetail>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getVodInfo(vodId))
    }
}

class GetSeriesInfoUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(seriesId: String): Flow<XtreamResult<SeriesDetail>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getSeriesInfo(seriesId))
    }
}

class GetEpgUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(streamId: String, limit: Int = 10): Flow<XtreamResult<EpgResponse>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getShortEpg(streamId, limit))
    }
}