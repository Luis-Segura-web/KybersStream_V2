package com.kybers.stream.domain.usecase.preferences

import com.kybers.stream.domain.model.UserPreferences
import com.kybers.stream.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateUserPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(preferences: UserPreferences): Result<Unit> {
        return userPreferencesRepository.updateUserPreferences(preferences)
    }
    
    suspend fun updateTheme(theme: String): Result<Unit> {
        return userPreferencesRepository.updateTheme(theme)
    }
    
    suspend fun updateViewMode(section: String, viewMode: String): Result<Unit> {
        return userPreferencesRepository.updateViewMode(section, viewMode)
    }
    
    suspend fun updatePreferredQuality(quality: String): Result<Unit> {
        return userPreferencesRepository.updatePreferredQuality(quality)
    }
    
    suspend fun updateSubtitleSettings(enabled: Boolean, language: String): Result<Unit> {
        return userPreferencesRepository.updateSubtitleSettings(enabled, language)
    }
    
    suspend fun updateParentalControl(enabled: Boolean, pin: String?, blockedCategories: Set<String>): Result<Unit> {
        return userPreferencesRepository.updateParentalControl(enabled, pin, blockedCategories)
    }
}