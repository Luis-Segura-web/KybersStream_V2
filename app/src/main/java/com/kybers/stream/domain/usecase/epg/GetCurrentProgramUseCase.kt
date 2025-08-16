package com.kybers.stream.domain.usecase.epg

import com.kybers.stream.domain.model.EpgProgram
import com.kybers.stream.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentProgramUseCase @Inject constructor(
    private val epgRepository: EpgRepository
) {
    suspend operator fun invoke(streamId: String): Result<EpgProgram?> {
        return epgRepository.getCurrentProgram(streamId)
    }
    
    fun flow(streamId: String): Flow<EpgProgram?> {
        return epgRepository.getCurrentProgramFlow(streamId)
    }
}