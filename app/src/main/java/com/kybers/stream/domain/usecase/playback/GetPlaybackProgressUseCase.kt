package com.kybers.stream.domain.usecase.playback

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaybackProgressUseCase @Inject constructor(
    private val playbackProgressRepository: PlaybackProgressRepository
) {
    operator fun invoke(contentId: String, contentType: ContentType): Flow<PlaybackProgress?> {
        return playbackProgressRepository.getProgress(contentId, contentType)
    }
}