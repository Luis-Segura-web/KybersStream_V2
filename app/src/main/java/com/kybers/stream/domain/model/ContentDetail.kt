package com.kybers.stream.domain.model

data class MovieDetail(
    val id: String,
    val name: String,
    val streamId: String,
    val year: String? = null,
    val rating: String? = null,
    val duration: String? = null,
    val quality: String? = null,
    val genre: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val trailerUrl: String? = null,
    val imdbRating: String? = null,
    val tmdbRating: String? = null,
    val language: String? = null,
    val country: String? = null,
    val releaseDate: String? = null,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val qualityOptions: List<QualityOption> = emptyList()
) {
    val hasExtendedInfo: Boolean
        get() = plot != null || cast != null || director != null
    
    val ratingDisplay: String?
        get() = when {
            imdbRating != null && imdbRating != "0" -> "IMDb: $imdbRating"
            tmdbRating != null && tmdbRating != "0" -> "TMDb: $tmdbRating"
            rating != null && rating != "0" -> rating
            else -> null
        }
    
    val durationDisplay: String?
        get() = duration?.let { 
            when {
                it.contains("min") -> it
                it.toIntOrNull() != null -> "${it} min"
                else -> it
            }
        }
}

data class SeriesDetail(
    val id: String,
    val name: String,
    val seriesId: String,
    val year: String? = null,
    val rating: String? = null,
    val genre: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val imdbRating: String? = null,
    val tmdbRating: String? = null,
    val language: String? = null,
    val country: String? = null,
    val releaseDate: String? = null,
    val seasons: List<Season> = emptyList(),
    val totalSeasons: Int = 0,
    val totalEpisodes: Int = 0,
    val status: String? = null // "Ended", "Continuing", etc.
) {
    val hasExtendedInfo: Boolean
        get() = plot != null || cast != null || director != null
    
    val ratingDisplay: String?
        get() = when {
            imdbRating != null && imdbRating != "0" -> "IMDb: $imdbRating"
            tmdbRating != null && tmdbRating != "0" -> "TMDb: $tmdbRating"
            rating != null && rating != "0" -> rating
            else -> null
        }
    
    val seasonsEpisodesDisplay: String
        get() = when {
            totalSeasons > 0 && totalEpisodes > 0 -> "$totalSeasons temporadas • $totalEpisodes episodios"
            seasons.isNotEmpty() -> "${seasons.size} temporadas"
            else -> "Serie"
        }
}

data class Season(
    val seasonNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    val airDate: String? = null,
    val poster: String? = null,
    val episodes: List<Episode> = emptyList(),
    val episodeCount: Int = 0
) {
    val displayName: String
        get() = name ?: "Temporada $seasonNumber"
    
    val episodeCountDisplay: String
        get() = when {
            episodeCount > 0 -> "$episodeCount episodios"
            episodes.isNotEmpty() -> "${episodes.size} episodios"
            else -> "Temporada"
        }
}

data class Episode(
    val id: String,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String? = null,
    val runtime: String? = null,
    val airDate: String? = null,
    val still: String? = null,
    val rating: String? = null,
    val streamUrl: String? = null,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList()
) {
    val displayTitle: String
        get() = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')} - $name"
    
    val shortTitle: String
        get() = "S${seasonNumber}E${episodeNumber}"
    
    val runtimeDisplay: String?
        get() = runtime?.let { 
            when {
                it.contains("min") -> it
                it.toIntOrNull() != null -> "${it} min"
                else -> it
            }
        }
}

data class SubtitleTrack(
    val id: String,
    val language: String,
    val languageCode: String,
    val url: String,
    val format: String = "vtt" // vtt, srt, ass, etc.
)

data class AudioTrack(
    val id: String,
    val language: String,
    val languageCode: String,
    val codec: String? = null,
    val channels: String? = null
)

data class QualityOption(
    val id: String,
    val quality: String, // "1080p", "720p", etc.
    val url: String,
    val bitrate: String? = null,
    val codec: String? = null
)

enum class ContentStatus {
    AVAILABLE,
    UNAVAILABLE,
    COMING_SOON,
    EXPIRED
}

enum class WatchStatus {
    NOT_WATCHED,
    WATCHING,
    COMPLETED,
    DROPPED
}