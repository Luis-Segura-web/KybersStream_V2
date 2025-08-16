package com.kybers.stream.domain.usecase.favorites

import com.kybers.stream.domain.model.ContentType
import com.kybers.stream.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(contentId: String, contentType: ContentType): Flow<Boolean> {
        return favoriteRepository.isFavorite(contentId, contentType)
    }
}