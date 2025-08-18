package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TMDBMovieListDto(
    val id: Int,
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
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

data class TMDBMovieListResponseDto(
    val page: Int,
    val results: List<TMDBMovieListDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)
