package com.kybers.stream.domain.usecase.xtream

import com.kybers.stream.domain.model.Series
import com.kybers.stream.domain.model.XtreamResult
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSeriesStreamsUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Series>>> = flow {
        when (val result = xtreamRepository.getSeries(categoryId)) {
            is XtreamResult.Success -> emit(Result.success(result.data))
            is XtreamResult.Error -> emit(Result.failure(Exception(result.message)))
            is XtreamResult.Loading -> {} // Loading state handled by UI
        }
    }
}