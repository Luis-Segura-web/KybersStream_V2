package com.kybers.stream.domain.model

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    data class Error(val message: String, val code: PlaybackErrorCode = PlaybackErrorCode.UNKNOWN) : PlaybackState()
}

enum class PlaybackErrorCode {
    NETWORK_ERROR,
    UNSUPPORTED_FORMAT,
    CONNECTION_LIMIT_EXCEEDED,
    AUTHENTICATION_ERROR,
    DECODER_ERROR,
    UNKNOWN
}

data class MediaInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val artworkUri: String? = null,
    val mediaUri: String,
    val mediaType: MediaType,
    val subtitleUri: String? = null,
    val duration: Long = 0L
)

enum class MediaType {
    LIVE_TV,
    VOD_MOVIE,
    SERIES_EPISODE
}

data class PlaybackPosition(
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L
) {
    val progressPercentage: Float
        get() = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
}