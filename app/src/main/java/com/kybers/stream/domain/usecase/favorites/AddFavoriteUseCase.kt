package com.kybers.stream.domain.usecase.favorites

import com.kybers.stream.domain.model.FavoriteItem
import com.kybers.stream.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(favorite: FavoriteItem): Result<Unit> {
        return favoriteRepository.addFavorite(favorite)
    }
}