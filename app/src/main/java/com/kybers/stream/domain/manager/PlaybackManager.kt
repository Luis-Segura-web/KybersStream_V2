package com.kybers.stream.domain.manager

import androidx.media3.exoplayer.ExoPlayer
import com.kybers.stream.domain.model.MediaInfo
import com.kybers.stream.domain.model.PlaybackPosition
import com.kybers.stream.domain.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow

interface PlaybackManager {
    val playbackState: StateFlow<PlaybackState>
    val currentMedia: StateFlow<MediaInfo?>
    val playbackPosition: StateFlow<PlaybackPosition>
    
    val exoPlayer: ExoPlayer
    
    suspend fun prepare(mediaInfo: MediaInfo)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
    
    fun setPlayWhenReady(playWhenReady: Boolean)
    fun isPlaying(): Boolean
    
    // Regla de una sola conexión activa
    suspend fun switchMedia(mediaInfo: MediaInfo)
}