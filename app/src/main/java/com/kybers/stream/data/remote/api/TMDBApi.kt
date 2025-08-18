package com.kybers.stream.data.remote.api

import com.kybers.stream.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApi {
    
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): Response<TMDBMovieDto>
    
    @GET("tv/{tv_id}")
    suspend fun getSeriesDetails(
        @Path("tv_id") tvId: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): Response<TMDBSeriesDto>
    
    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): Response<TMDBCreditsDto>
    
    @GET("tv/{tv_id}/credits")
    suspend fun getSeriesCredits(
        @Path("tv_id") tvId: String,
        @Query("api_key") apiKey: String
    ): Response<TMDBCreditsDto>
    
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<TMDBVideosDto>
    
    @GET("tv/{tv_id}/videos")
    suspend fun getSeriesVideos(
        @Path("tv_id") tvId: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<TMDBVideosDto>
    
    // Nuevas funciones para contenido popular
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<TMDBMovieListResponseDto>
    
    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("api_key") apiKey: String
    ): Response<TMDBMovieListResponseDto>
    
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<TMDBMovieListResponseDto>
    
    @GET("tv/popular")
    suspend fun getPopularSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<TMDBSeriesListResponseDto>
    
    @GET("trending/tv/{time_window}")
    suspend fun getTrendingSeries(
        @Path("time_window") timeWindow: String = "week",
        @Query("api_key") apiKey: String
    ): Response<TMDBSeriesListResponseDto>
    
    @GET("tv/top_rated")
    suspend fun getTopRatedSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<TMDBSeriesListResponseDto>
    
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val POSTER_SIZE_W500 = "w500"
        const val BACKDROP_SIZE_W1280 = "w1280"
        const val PROFILE_SIZE_W185 = "w185"
        
        fun getImageUrl(path: String?, size: String = POSTER_SIZE_W500): String? {
            return if (path != null) "$IMAGE_BASE_URL$size$path" else null
        }
    }
}