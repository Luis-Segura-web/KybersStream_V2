package com.kybers.stream.data.manager

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import com.kybers.stream.domain.manager.PlaybackManager
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.playback.SavePlaybackProgressUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val exoPlayerInstance: ExoPlayer,
    private val dataSourceFactory: DataSource.Factory,
    private val savePlaybackProgressUseCase: SavePlaybackProgressUseCase
) : PlaybackManager {

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    override val currentMedia: StateFlow<MediaInfo?> = _currentMedia.asStateFlow()

    private val _playbackPosition = MutableStateFlow(PlaybackPosition())
    override val playbackPosition: StateFlow<PlaybackPosition> = _playbackPosition.asStateFlow()

    override val exoPlayer: ExoPlayer = exoPlayerInstance

    private var positionUpdateJob: Job? = null
    private var currentMediaJob: Job? = null
    private var progressSaveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Configuración para el guardado de progreso
    private val progressSaveIntervalMs = 5000L // Guardar cada 5 segundos
    private val minProgressToSaveMs = 10000L // Mínimo 10 segundos para guardar

    init {
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val state = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.Idle
                    Player.STATE_BUFFERING -> PlaybackState.Buffering
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) PlaybackState.Playing else PlaybackState.Paused
                    Player.STATE_ENDED -> PlaybackState.Idle
                    else -> PlaybackState.Idle
                }
                _playbackState.value = state

                if (state == PlaybackState.Playing) {
                    startPositionUpdates()
                    startProgressSaving()
                } else {
                    stopPositionUpdates()
                    stopProgressSaving()
                    
                    // Guardar progreso al pausar
                    if (state == PlaybackState.Paused) {
                        scope.launch { saveProgressOnPause() }
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val errorMessage = when {
                    // Detectar errores HTTP específicos
                    error.cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException -> {
                        val httpError = error.cause as androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
                        when (httpError.responseCode) {
                            404 -> "Stream no encontrado (404). Verifique que el canal esté disponible."
                            401, 403 -> "Error de autenticación. Verifique sus credenciales."
                            500, 502, 503 -> "Error del servidor. Intente más tarde."
                            else -> "Error HTTP ${httpError.responseCode}: ${httpError.message}"
                        }
                    }
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Error de conexión de red"
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Tiempo de espera agotado"
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "Formato de archivo no soportado"
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> "Formato de manifiesto no soportado"
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "Error al inicializar el decodificador"
                    else -> "Error de reproducción: ${error.message}"
                }

                val errorCode = when (error.errorCode) {
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlaybackErrorCode.NETWORK_ERROR
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> PlaybackErrorCode.UNSUPPORTED_FORMAT
                    androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> PlaybackErrorCode.DECODER_ERROR
                    else -> PlaybackErrorCode.UNKNOWN
                }

                _playbackState.value = PlaybackState.Error(errorMessage, errorCode)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
        })
    }

    override suspend fun prepare(mediaInfo: MediaInfo) {
        withContext(Dispatchers.Main) {
            try {
                // Liberar recursos anteriores
                release()
                
                _currentMedia.value = mediaInfo
                _playbackState.value = PlaybackState.Buffering

                val mediaSource = createMediaSource(mediaInfo)
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()

                currentMediaJob = scope.launch {
                    // Monitorear la reproducción del medio actual
                }
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.Error("Error al preparar el medio: ${e.message}")
            }
        }
    }

    override suspend fun switchMedia(mediaInfo: MediaInfo) {
        // Implementar regla de una sola conexión activa
        stop()
        release()
        prepare(mediaInfo)
    }

    private fun createMediaSource(mediaInfo: MediaInfo): MediaSource {
        val uri = Uri.parse(mediaInfo.mediaUri)
        val mediaItem = createMediaItem(mediaInfo)

        return when (inferMediaType(mediaInfo.mediaUri)) {
            MediaSourceType.HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            MediaSourceType.DASH -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            MediaSourceType.PROGRESSIVE -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        }
    }

    private fun createMediaItem(mediaInfo: MediaInfo): MediaItem {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(mediaInfo.title)
            .setDescription(mediaInfo.description)
            .apply {
                mediaInfo.artworkUri?.let { setArtworkUri(Uri.parse(it)) }
            }
            .build()

        val builder = MediaItem.Builder()
            .setUri(mediaInfo.mediaUri)
            .setMediaMetadata(mediaMetadata)

        // Configurar tipo MIME basado en la URL
        val mimeType = inferMimeType(mediaInfo.mediaUri)
        if (mimeType != null) {
            builder.setMimeType(mimeType)
        }

        // Agregar subtítulos si están disponibles
        mediaInfo.subtitleUri?.let { subtitleUri ->
            val subtitle = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUri))
                .setMimeType(MimeTypes.TEXT_VTT) // Asumir VTT por defecto
                .setLanguage("es")
                .setSelectionFlags(0)
                .build()
            builder.setSubtitleConfigurations(listOf(subtitle))
        }

        return builder.build()
    }

    private fun inferMediaType(url: String): MediaSourceType {
        return when {
            url.contains(".m3u8") || url.contains("m3u8") -> MediaSourceType.HLS
            url.contains(".mpd") -> MediaSourceType.DASH
            url.contains(".ts") && !url.contains(".m3u8") -> MediaSourceType.PROGRESSIVE
            url.contains(".mp4") || url.contains(".mkv") || url.contains(".avi") || 
            url.contains(".m4v") || url.contains(".mov") -> MediaSourceType.PROGRESSIVE
            else -> MediaSourceType.HLS // Por defecto para IPTV
        }
    }

    private fun inferMimeType(url: String): String? {
        return when {
            url.contains(".m3u8") -> MimeTypes.APPLICATION_M3U8
            url.contains(".mpd") -> MimeTypes.APPLICATION_MPD
            url.contains(".mp4") -> MimeTypes.VIDEO_MP4
            url.contains(".mkv") -> MimeTypes.VIDEO_MATROSKA
            url.contains(".ts") -> MimeTypes.VIDEO_MP2T
            url.contains(".avi") -> "video/x-msvideo"
            url.contains(".m4v") -> "video/x-m4v"
            url.contains(".mov") -> MimeTypes.VIDEO_MP4
            else -> null
        }
    }

    override fun play() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun stop() {
        // Guardar progreso antes de parar
        scope.launch { saveProgressOnPause() }
        
        exoPlayer.stop()
        _playbackState.value = PlaybackState.Idle
        stopPositionUpdates()
        stopProgressSaving()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun release() {
        // Guardar progreso antes de liberar recursos
        scope.launch { saveProgressOnPause() }
        
        currentMediaJob?.cancel()
        stopPositionUpdates()
        stopProgressSaving()
        exoPlayer.clearMediaItems()
        _currentMedia.value = null
        _playbackState.value = PlaybackState.Idle
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive && exoPlayer.isPlaying) {
                _playbackPosition.value = PlaybackPosition(
                    currentPosition = exoPlayer.currentPosition,
                    duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L,
                    bufferedPosition = exoPlayer.bufferedPosition
                )
                delay(1000) // Actualizar cada segundo
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    private fun startProgressSaving() {
        stopProgressSaving()
        val currentMediaInfo = _currentMedia.value ?: return
        
        progressSaveJob = scope.launch {
            while (isActive && exoPlayer.isPlaying) {
                saveCurrentProgress(currentMediaInfo)
                delay(progressSaveIntervalMs)
            }
        }
    }
    
    private fun stopProgressSaving() {
        progressSaveJob?.cancel()
        progressSaveJob = null
    }
    
    private suspend fun saveCurrentProgress(mediaInfo: MediaInfo) {
        try {
            val currentPosition = exoPlayer.currentPosition
            val duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            
            // Solo guardar si hay progreso mínimo y no es TV en vivo
            if (currentPosition > minProgressToSaveMs && 
                mediaInfo.mediaType != MediaType.LIVE_TV &&
                duration > 0) {
                
                val contentType = when (mediaInfo.mediaType) {
                    MediaType.VOD_MOVIE -> ContentType.VOD
                    MediaType.SERIES_EPISODE -> ContentType.EPISODE
                    else -> return // No guardar progreso para TV en vivo
                }
                
                savePlaybackProgressUseCase(
                    contentId = mediaInfo.id,
                    contentType = contentType,
                    positionMs = currentPosition,
                    durationMs = duration
                )
            }
        } catch (e: Exception) {
            // Log error but don't interrupt playback
        }
    }
    
    private suspend fun saveProgressOnPause() {
        val currentMediaInfo = _currentMedia.value ?: return
        saveCurrentProgress(currentMediaInfo)
    }

    private enum class MediaSourceType {
        HLS, DASH, PROGRESSIVE
    }
}