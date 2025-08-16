package com.kybers.stream.domain.usecase.favorites

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<FavoriteItem>> {
        return favoriteRepository.getAllFavorites()
    }
    
    operator fun invoke(contentType: ContentType): Flow<List<FavoriteItem>> {
        return favoriteRepository.getFavoritesByType(contentType)
    }
}