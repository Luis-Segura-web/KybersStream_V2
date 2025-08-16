package com.kybers.stream.domain.usecase.epg

import com.kybers.stream.domain.model.ChannelEpg
import com.kybers.stream.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChannelEpgUseCase @Inject constructor(
    private val epgRepository: EpgRepository
) {
    suspend operator fun invoke(streamId: String): Result<ChannelEpg> {
        return epgRepository.getChannelEpg(streamId)
    }
    
    fun flow(streamId: String): Flow<ChannelEpg> {
        return epgRepository.getChannelEpgFlow(streamId)
    }
}