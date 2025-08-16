package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.FavoriteEntity
import com.kybers.stream.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites ORDER BY addedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE contentType = :contentType ORDER BY addedTimestamp DESC")
    fun getFavoritesByType(contentType: ContentType): Flow<List<FavoriteEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId AND contentType = :contentType)")
    suspend fun isFavorite(contentId: String, contentType: ContentType): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId AND contentType = :contentType)")
    fun isFavoriteFlow(contentId: String, contentType: ContentType): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long
    
    @Query("DELETE FROM favorites WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun removeFavorite(contentId: String, contentType: ContentType): Int
    
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
    
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoritesCount(): Int
}