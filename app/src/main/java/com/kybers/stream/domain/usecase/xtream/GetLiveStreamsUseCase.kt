package com.kybers.stream.domain.usecase.xtream

import com.kybers.stream.domain.model.Channel
import com.kybers.stream.domain.model.XtreamResult
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLiveStreamsUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Channel>>> = flow {
        when (val result = xtreamRepository.getLiveStreams(categoryId)) {
            is XtreamResult.Success -> emit(Result.success(result.data))
            is XtreamResult.Error -> emit(Result.failure(Exception(result.message)))
            is XtreamResult.Loading -> {} // Loading state handled by UI
        }
    }
}