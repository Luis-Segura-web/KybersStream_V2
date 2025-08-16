package com.kybers.stream.domain.usecase.favorites

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(contentId: String, contentType: ContentType): Result<Unit> {
        return favoriteRepository.removeFavorite(contentId, contentType)
    }
}