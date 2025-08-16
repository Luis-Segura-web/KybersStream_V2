package com.kybers.stream.domain.usecase.favorites

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(
        contentId: String,
        contentType: ContentType,
        name: String,
        imageUrl: String? = null,
        categoryId: String? = null
    ): Result<Boolean> {
        return try {
            val isFavorite = favoriteRepository.isFavorite(contentId, contentType).first()
            
            if (isFavorite) {
                favoriteRepository.removeFavorite(contentId, contentType)
                Result.success(false)
            } else {
                val favoriteItem = FavoriteItem(
                    contentId = contentId,
                    contentType = contentType,
                    name = name,
                    imageUrl = imageUrl,
                    categoryId = categoryId,
                    addedTimestamp = System.currentTimeMillis()
                )
                favoriteRepository.addFavorite(favoriteItem)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}