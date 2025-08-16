package com.kybers.stream.data.remote.api

import com.kybers.stream.data.remote.dto.XtreamAuthResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApi {
    
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<XtreamAuthResponseDto>
    
    companion object {
        const val PLAYER_API_PATH = "player_api.php"
    }
}