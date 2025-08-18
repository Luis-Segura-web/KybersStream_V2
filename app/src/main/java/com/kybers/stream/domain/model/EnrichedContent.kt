package com.kybers.stream.domain.model

data class EnrichedMovie(
    // Datos básicos de Xtream
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
    
    // Datos enriquecidos de TMDB
    val tmdbData: TMDBMovieData? = null
)

data class EnrichedSeries(
    // Datos básicos de Xtream
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
    val tmdbId: String? = null,
    
    // Datos enriquecidos de TMDB
    val tmdbData: TMDBSeriesData? = null
)

data class TMDBMovieData(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val runtime: Int?,
    val genres: List<TMDBGenre> = emptyList(),
    val productionCompanies: List<TMDBProductionCompany> = emptyList(),
    val productionCountries: List<TMDBProductionCountry> = emptyList(),
    val spokenLanguages: List<TMDBSpokenLanguage> = emptyList(),
    val adult: Boolean,
    val budget: Long?,
    val revenue: Long?,
    val tagline: String?,
    val status: String?,
    val originalLanguage: String,
    val popularity: Double,
    val homepage: String?,
    val imdbId: String?,
    val credits: TMDBCredits? = null,
    val videos: List<TMDBVideo> = emptyList()
)

data class TMDBSeriesData(
    val id: Int,
    val name: String,
    val originalName: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<TMDBGenre> = emptyList(),
    val productionCompanies: List<TMDBProductionCompany> = emptyList(),
    val productionCountries: List<TMDBProductionCountry> = emptyList(),
    val spokenLanguages: List<TMDBSpokenLanguage> = emptyList(),
    val adult: Boolean,
    val createdBy: List<TMDBCreatedBy> = emptyList(),
    val episodeRunTime: List<Int> = emptyList(),
    val inProduction: Boolean,
    val numberOfEpisodes: Int,
    val numberOfSeasons: Int,
    val originalLanguage: String,
    val popularity: Double,
    val status: String?,
    val tagline: String?,
    val type: String?,
    val homepage: String?,
    val networks: List<TMDBNetwork> = emptyList(),
    val seasons: List<TMDBSeason> = emptyList(),
    val credits: TMDBCredits? = null,
    val videos: List<TMDBVideo> = emptyList()
)

data class TMDBGenre(
    val id: Int,
    val name: String
)

data class TMDBProductionCompany(
    val id: Int,
    val name: String,
    val logoPath: String?,
    val originCountry: String
)

data class TMDBProductionCountry(
    val iso31661: String,
    val name: String
)

data class TMDBSpokenLanguage(
    val iso6391: String,
    val englishName: String,
    val name: String
)

data class TMDBCreatedBy(
    val id: Int,
    val creditId: String,
    val name: String,
    val profilePath: String?
)

data class TMDBNetwork(
    val id: Int,
    val name: String,
    val logoPath: String?,
    val originCountry: String
)

data class TMDBSeason(
    val id: Int,
    val airDate: String?,
    val episodeCount: Int,
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val seasonNumber: Int
)

data class TMDBCredits(
    val cast: List<TMDBCast> = emptyList(),
    val crew: List<TMDBCrew> = emptyList()
) {
    fun getDirectors(): List<TMDBCrew> = crew.filter { it.job == "Director" }
    fun getMainCast(limit: Int = 10): List<TMDBCast> = cast.take(limit)
}

data class TMDBCast(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
    val order: Int
)

data class TMDBCrew(
    val id: Int,
    val name: String,
    val job: String,
    val department: String,
    val profilePath: String?
)

data class TMDBVideo(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: String?
) {
    val isTrailer: Boolean get() = type.equals("Trailer", ignoreCase = true)
    val isYoutube: Boolean get() = site.equals("YouTube", ignoreCase = true)
    val youtubeUrl: String? get() = if (isYoutube) "https://www.youtube.com/watch?v=$key" else null
}

// Nuevos modelos para contenido filtrado de TMDB
data class TMDBFilteredContent(
    val popularMovies: List<EnrichedTMDBMovie> = emptyList(),
    val trendingMovies: List<EnrichedTMDBMovie> = emptyList(),
    val topRatedMovies: List<EnrichedTMDBMovie> = emptyList(),
    val popularSeries: List<EnrichedTMDBSeries> = emptyList(),
    val trendingSeries: List<EnrichedTMDBSeries> = emptyList(),
    val topRatedSeries: List<EnrichedTMDBSeries> = emptyList()
) {
    val hasContent: Boolean get() = 
        popularMovies.isNotEmpty() || trendingMovies.isNotEmpty() || topRatedMovies.isNotEmpty() ||
        popularSeries.isNotEmpty() || trendingSeries.isNotEmpty() || topRatedSeries.isNotEmpty()
}

data class EnrichedTMDBMovie(
    val tmdbData: TMDBMovieData,
    val xtreamMovie: Movie
)

data class EnrichedTMDBSeries(
    val tmdbData: TMDBSeriesData,
    val xtreamSeries: Series
)