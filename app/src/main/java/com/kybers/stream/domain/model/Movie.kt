package com.kybers.stream.domain.model

data class Movie(
    val streamId: String,
    val name: String,
    val icon: String?,
    val categoryId: String,
    val rating: String?,
    val rating5Based: Double = 0.0,
    val addedTimestamp: Long = 0L,
    val isAdult: Boolean = false,
    val containerExtension: String?,
    val tmdbId: String? = null,
    val poster: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val plot: String? = null,
    val quality: String? = null
)

data class MovieInfo(
    val movieImage: String?,
    val name: String,
    val tmdbId: String?,
    val backdrop: String?,
    val youtubeTrailer: String?,
    val genre: String?,
    val plot: String?,
    val cast: String?,
    val rating: String?,
    val director: String?,
    val releaseDate: String?,
    val backdropPath: List<String> = emptyList(),
    val durationSecs: Int = 0,
    val duration: String?
)

data class MovieData(
    val streamId: String,
    val name: String,
    val addedTimestamp: Long,
    val categoryId: String,
    val containerExtension: String?,
    val customSid: String?,
    val directSource: String?
)