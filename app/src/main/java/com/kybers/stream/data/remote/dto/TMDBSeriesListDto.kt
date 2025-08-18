package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TMDBSeriesListDto(
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String,
    @SerializedName("last_air_date")
    val lastAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("genre_ids")
    val genreIds: List<Int>,
    val adult: Boolean,
    @SerializedName("original_language")
    val originalLanguage: String,
    val popularity: Double
)

data class TMDBSeriesListResponseDto(
    val page: Int,
    val results: List<TMDBSeriesListDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)
