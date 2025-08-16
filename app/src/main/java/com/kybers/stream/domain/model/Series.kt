package com.kybers.stream.domain.model

data class Series(
    val seriesId: String,
    val name: String,
    val cover: String?,
    val categoryId: String,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val lastModified: Long = 0L,
    val rating: String?,
    val rating5Based: Double = 0.0,
    val backdropPath: List<String> = emptyList(),
    val youtubeTrailer: String?,
    val episodeRunTime: String?,
    val tmdbId: String? = null
)

data class SeriesInfo(
    val name: String,
    val cover: String?,
    val youtubeTrailer: String?,
    val genre: String?,
    val releaseDate: String?,
    val plot: String?,
    val cast: String?,
    val rating: String?,
    val rating5Based: Double = 0.0,
    val director: String?,
    val backdropPath: List<String> = emptyList(),
    val lastModified: Long = 0L,
    val episodeRunTime: String?,
    val categoryId: String
)

data class EpisodeInfo(
    val movieImage: String?,
    val releaseDate: String?,
    val youtubeTrailer: String?,
    val plot: String?,
    val cast: String?,
    val rating: Int = 0,
    val rating5Based: Double = 0.0,
    val director: String?,
    val durationSecs: Int = 0,
    val duration: String?
)