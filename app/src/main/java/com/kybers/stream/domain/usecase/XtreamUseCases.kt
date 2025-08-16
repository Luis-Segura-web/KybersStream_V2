package com.kybers.stream.domain.usecase

import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
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
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(categoryId: String? = null): Flow<XtreamResult<List<Series>>> = flow {
        emit(XtreamResult.Loading)
        emit(xtreamRepository.getSeries(categoryId))
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