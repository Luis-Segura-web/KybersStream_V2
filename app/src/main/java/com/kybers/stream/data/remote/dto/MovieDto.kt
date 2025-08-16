package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDto(
    @SerializedName("num")
    val num: Int = 0,
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("stream_type")
    val streamType: String = "",
    
    @SerializedName("stream_id")
    val streamId: String = "",
    
    @SerializedName("stream_icon")
    val streamIcon: String? = null,
    
    @SerializedName("rating")
    val rating: String? = null,
    
    @SerializedName("rating_5based")
    val rating5Based: Double = 0.0,
    
    @SerializedName("added")
    val added: String = "",
    
    @SerializedName("is_adult")
    val isAdult: String = "0",
    
    @SerializedName("category_id")
    val categoryId: String = "",
    
    @SerializedName("container_extension")
    val containerExtension: String? = null,
    
    @SerializedName("custom_sid")
    val customSid: String? = null,
    
    @SerializedName("direct_source")
    val directSource: String? = null,
    
    @SerializedName("tmdb_id")
    val tmdbId: String? = null
)

data class MovieDetailDto(
    @SerializedName("info")
    val info: MovieInfoDto,
    
    @SerializedName("movie_data")
    val movieData: MovieDataDto
)

data class MovieInfoDto(
    @SerializedName("movie_image")
    val movieImage: String? = null,
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("tmdb_id")
    val tmdbId: String? = null,
    
    @SerializedName("backdrop")
    val backdrop: String? = null,
    
    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,
    
    @SerializedName("genre")
    val genre: String? = null,
    
    @SerializedName("plot")
    val plot: String? = null,
    
    @SerializedName("cast")
    val cast: String? = null,
    
    @SerializedName("rating")
    val rating: String? = null,
    
    @SerializedName("director")
    val director: String? = null,
    
    @SerializedName("releasedate")
    val releaseDate: String? = null,
    
    @SerializedName("backdrop_path")
    val backdropPath: List<String> = emptyList(),
    
    @SerializedName("duration_secs")
    val durationSecs: Int = 0,
    
    @SerializedName("duration")
    val duration: String? = null
)

data class MovieDataDto(
    @SerializedName("stream_id")
    val streamId: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("added")
    val added: String = "",
    
    @SerializedName("category_id")
    val categoryId: String = "",
    
    @SerializedName("container_extension")
    val containerExtension: String? = null,
    
    @SerializedName("custom_sid")
    val customSid: String? = null,
    
    @SerializedName("direct_source")
    val directSource: String? = null
)