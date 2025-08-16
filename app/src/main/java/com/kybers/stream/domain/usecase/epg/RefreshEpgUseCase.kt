package com.kybers.stream.domain.usecase.epg

import com.kybers.stream.domain.model.EpgData
import com.kybers.stream.domain.repository.EpgRepository
import javax.inject.Inject

class RefreshEpgUseCase @Inject constructor(
    private val epgRepository: EpgRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<EpgData> {
        return epgRepository.refreshEpgData(forceRefresh)
    }
    
    suspend fun refreshChannel(streamId: String): Result<Unit> {
        return epgRepository.refreshChannelEpg(streamId)
    }
}