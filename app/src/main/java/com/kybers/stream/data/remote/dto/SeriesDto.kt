package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SeriesDto(
    @SerializedName("num")
    val num: Int = 0,
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("series_id")
    val seriesId: String = "",
    
    @SerializedName("cover")
    val cover: String? = null,
    
    @SerializedName("plot")
    val plot: String? = null,
    
    @SerializedName("cast")
    val cast: String? = null,
    
    @SerializedName("director")
    val director: String? = null,
    
    @SerializedName("genre")
    val genre: String? = null,
    
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    
    @SerializedName("last_modified")
    val lastModified: String = "",
    
    @SerializedName("rating")
    val rating: String? = null,
    
    @SerializedName("rating_5based")
    val rating5Based: Double = 0.0,
    
    @SerializedName("backdrop_path")
    val backdropPath: List<String> = emptyList(),
    
    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,
    
    @SerializedName("episode_run_time")
    val episodeRunTime: String? = null,
    
    @SerializedName("category_id")
    val categoryId: String = "",
    
    @SerializedName("tmdb_id")
    val tmdbId: String? = null
)

data class SeriesDetailDto(
    @SerializedName("seasons")
    val seasons: List<SeasonDto> = emptyList(),
    
    @SerializedName("info")
    val info: SeriesInfoDto,
    
    @SerializedName("episodes")
    val episodes: Map<String, List<EpisodeDto>> = emptyMap()
)

data class SeasonDto(
    @SerializedName("air_date")
    val airDate: String? = null,
    
    @SerializedName("episode_count")
    val episodeCount: Int = 0,
    
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("season_number")
    val seasonNumber: String = "",
    
    @SerializedName("overview")
    val overview: String? = null,
    
    @SerializedName("cover")
    val cover: String? = null,
    
    @SerializedName("cover_big")
    val coverBig: String? = null
)

data class SeriesInfoDto(
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("cover")
    val cover: String? = null,
    
    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,
    
    @SerializedName("genre")
    val genre: String? = null,
    
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    
    @SerializedName("plot")
    val plot: String? = null,
    
    @SerializedName("cast")
    val cast: String? = null,
    
    @SerializedName("rating")
    val rating: String? = null,
    
    @SerializedName("rating_5based")
    val rating5Based: Double = 0.0,
    
    @SerializedName("director")
    val director: String? = null,
    
    @SerializedName("backdrop_path")
    val backdropPath: List<String> = emptyList(),
    
    @SerializedName("last_modified")
    val lastModified: String = "",
    
    @SerializedName("episode_run_time")
    val episodeRunTime: String? = null,
    
    @SerializedName("category_id")
    val categoryId: String = ""
)

data class EpisodeDto(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("episode_num")
    val episodeNum: Int = 0,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("container_extension")
    val containerExtension: String? = null,
    
    @SerializedName("added")
    val added: String = "",
    
    @SerializedName("info")
    val info: EpisodeInfoDto,
    
    @SerializedName("season")
    val season: Int = 0,
    
    @SerializedName("custom_sid")
    val customSid: String? = null,
    
    @SerializedName("direct_source")
    val directSource: String? = null
)

data class EpisodeInfoDto(
    @SerializedName("movie_image")
    val movieImage: String? = null,
    
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    
    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,
    
    @SerializedName("plot")
    val plot: String? = null,
    
    @SerializedName("cast")
    val cast: String? = null,
    
    @SerializedName("rating")
    val rating: Int = 0,
    
    @SerializedName("rating_5based")
    val rating5Based: Double = 0.0,
    
    @SerializedName("director")
    val director: String? = null,
    
    @SerializedName("duration_secs")
    val durationSecs: Int = 0,
    
    @SerializedName("duration")
    val duration: String? = null
)