package com.kybers.stream.domain.usecase.preferences

import com.kybers.stream.domain.model.UserPreferences
import com.kybers.stream.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> {
        return userPreferencesRepository.getUserPreferences()
    }
}