package com.kybers.stream.domain.usecase.playback

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.PlaybackProgress
import com.kybers.stream.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

class SavePlaybackProgressUseCase @Inject constructor(
    private val playbackProgressRepository: PlaybackProgressRepository
) {
    suspend operator fun invoke(
        contentId: String,
        contentType: ContentType,
        positionMs: Long,
        durationMs: Long
    ): Result<Unit> {
        val progress = PlaybackProgress(
            contentId = contentId,
            contentType = contentType,
            positionMs = positionMs,
            durationMs = durationMs,
            lastUpdated = System.currentTimeMillis()
        )
        
        return playbackProgressRepository.saveProgress(progress)
    }
}