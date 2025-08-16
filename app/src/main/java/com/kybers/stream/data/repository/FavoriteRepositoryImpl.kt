package com.kybers.stream.data.repository

import com.kybers.stream.data.local.dao.FavoriteDao
import com.kybers.stream.data.local.entity.FavoriteEntity
import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {
    
    override fun getAllFavorites(): Flow<List<FavoriteItem>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getFavoritesByType(contentType: ContentType): Flow<List<FavoriteItem>> {
        return favoriteDao.getFavoritesByType(contentType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun isFavorite(contentId: String, contentType: ContentType): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(contentId, contentType)
    }
    
    override suspend fun addFavorite(favorite: FavoriteItem): Result<Unit> {
        return try {
            favoriteDao.insertFavorite(favorite.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeFavorite(contentId: String, contentType: ContentType): Result<Unit> {
        return try {
            favoriteDao.removeFavorite(contentId, contentType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllFavorites(): Result<Unit> {
        return try {
            favoriteDao.clearAllFavorites()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFavoritesCount(): Int {
        return favoriteDao.getFavoritesCount()
    }
}

private fun FavoriteEntity.toDomainModel(): FavoriteItem {
    return FavoriteItem(
        contentId = contentId,
        contentType = contentType,
        name = name,
        imageUrl = imageUrl,
        categoryId = categoryId,
        addedTimestamp = addedTimestamp
    )
}

private fun FavoriteItem.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        contentId = contentId,
        contentType = contentType,
        name = name,
        imageUrl = imageUrl,
        categoryId = categoryId,
        addedTimestamp = addedTimestamp
    )
}