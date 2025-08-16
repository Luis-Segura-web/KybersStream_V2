package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.FavoriteItem
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<FavoriteItem>>
    fun getFavoritesByType(contentType: ContentType): Flow<List<FavoriteItem>>
    fun isFavorite(contentId: String, contentType: ContentType): Flow<Boolean>
    suspend fun addFavorite(favorite: FavoriteItem): Result<Unit>
    suspend fun removeFavorite(contentId: String, contentType: ContentType): Result<Unit>
    suspend fun clearAllFavorites(): Result<Unit>
    suspend fun getFavoritesCount(): Int
}