package com.kybers.stream.domain.usecase.playback

import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContinueWatchingUseCase @Inject constructor(
    private val playbackProgressRepository: PlaybackProgressRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<PlaybackProgress>> {
        return playbackProgressRepository.getContinueWatchingItems(limit)
    }
}