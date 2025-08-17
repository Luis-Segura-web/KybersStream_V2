package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TMDBMovieDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("overview")
    val overview: String?,
    
    @SerializedName("poster_path")
    val posterPath: String?,
    
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    
    @SerializedName("release_date")
    val releaseDate: String?,
    
    @SerializedName("vote_average")
    val voteAverage: Double,
    
    @SerializedName("vote_count")
    val voteCount: Int,
    
    @SerializedName("runtime")
    val runtime: Int?,
    
    @SerializedName("genres")
    val genres: List<TMDBGenreDto> = emptyList(),
    
    @SerializedName("production_companies")
    val productionCompanies: List<TMDBProductionCompanyDto> = emptyList(),
    
    @SerializedName("production_countries")
    val productionCountries: List<TMDBProductionCountryDto> = emptyList(),
    
    @SerializedName("spoken_languages")
    val spokenLanguages: List<TMDBSpokenLanguageDto> = emptyList(),
    
    @SerializedName("adult")
    val adult: Boolean,
    
    @SerializedName("budget")
    val budget: Long?,
    
    @SerializedName("revenue")
    val revenue: Long?,
    
    @SerializedName("tagline")
    val tagline: String?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("original_title")
    val originalTitle: String,
    
    @SerializedName("original_language")
    val originalLanguage: String,
    
    @SerializedName("popularity")
    val popularity: Double,
    
    @SerializedName("video")
    val video: Boolean,
    
    @SerializedName("homepage")
    val homepage: String?,
    
    @SerializedName("imdb_id")
    val imdbId: String?
)

data class TMDBSeriesDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("overview")
    val overview: String?,
    
    @SerializedName("poster_path")
    val posterPath: String?,
    
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    
    @SerializedName("last_air_date")
    val lastAirDate: String?,
    
    @SerializedName("vote_average")
    val voteAverage: Double,
    
    @SerializedName("vote_count")
    val voteCount: Int,
    
    @SerializedName("genres")
    val genres: List<TMDBGenreDto> = emptyList(),
    
    @SerializedName("production_companies")
    val productionCompanies: List<TMDBProductionCompanyDto> = emptyList(),
    
    @SerializedName("production_countries")
    val productionCountries: List<TMDBProductionCountryDto> = emptyList(),
    
    @SerializedName("spoken_languages")
    val spokenLanguages: List<TMDBSpokenLanguageDto> = emptyList(),
    
    @SerializedName("adult")
    val adult: Boolean,
    
    @SerializedName("created_by")
    val createdBy: List<TMDBCreatedByDto> = emptyList(),
    
    @SerializedName("episode_run_time")
    val episodeRunTime: List<Int> = emptyList(),
    
    @SerializedName("in_production")
    val inProduction: Boolean,
    
    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int,
    
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int,
    
    @SerializedName("original_name")
    val originalName: String,
    
    @SerializedName("original_language")
    val originalLanguage: String,
    
    @SerializedName("popularity")
    val popularity: Double,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("tagline")
    val tagline: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("homepage")
    val homepage: String?,
    
    @SerializedName("networks")
    val networks: List<TMDBNetworkDto> = emptyList(),
    
    @SerializedName("seasons")
    val seasons: List<TMDBSeasonDto> = emptyList()
)

data class TMDBGenreDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String
)

data class TMDBProductionCompanyDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("logo_path")
    val logoPath: String?,
    
    @SerializedName("origin_country")
    val originCountry: String
)

data class TMDBProductionCountryDto(
    @SerializedName("iso_3166_1")
    val iso31661: String,
    
    @SerializedName("name")
    val name: String
)

data class TMDBSpokenLanguageDto(
    @SerializedName("iso_639_1")
    val iso6391: String,
    
    @SerializedName("english_name")
    val englishName: String,
    
    @SerializedName("name")
    val name: String
)

data class TMDBCreatedByDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("credit_id")
    val creditId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("profile_path")
    val profilePath: String?
)

data class TMDBNetworkDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("logo_path")
    val logoPath: String?,
    
    @SerializedName("origin_country")
    val originCountry: String
)

data class TMDBSeasonDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("air_date")
    val airDate: String?,
    
    @SerializedName("episode_count")
    val episodeCount: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("overview")
    val overview: String?,
    
    @SerializedName("poster_path")
    val posterPath: String?,
    
    @SerializedName("season_number")
    val seasonNumber: Int
)

data class TMDBCreditsDto(
    @SerializedName("cast")
    val cast: List<TMDBCastDto> = emptyList(),
    
    @SerializedName("crew")
    val crew: List<TMDBCrewDto> = emptyList()
)

data class TMDBCastDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("character")
    val character: String,
    
    @SerializedName("profile_path")
    val profilePath: String?,
    
    @SerializedName("order")
    val order: Int
)

data class TMDBCrewDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("job")
    val job: String,
    
    @SerializedName("department")
    val department: String,
    
    @SerializedName("profile_path")
    val profilePath: String?
)

data class TMDBVideosDto(
    @SerializedName("results")
    val results: List<TMDBVideoDto> = emptyList()
)

data class TMDBVideoDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("key")
    val key: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("site")
    val site: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("official")
    val official: Boolean,
    
    @SerializedName("published_at")
    val publishedAt: String?
)