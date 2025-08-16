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

data class SeriesDetail(
    val seasons: List<Season>,
    val info: SeriesInfo,
    val episodes: Map<String, List<Episode>>
)

data class Season(
    val airDate: String?,
    val episodeCount: Int,
    val id: String,
    val name: String,
    val seasonNumber: String,
    val overview: String?,
    val cover: String?,
    val coverBig: String?
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

data class Episode(
    val id: String,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String?,
    val addedTimestamp: Long,
    val info: EpisodeInfo,
    val season: Int,
    val customSid: String?,
    val directSource: String?
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