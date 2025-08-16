package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit>
    suspend fun updateTheme(theme: String): Result<Unit>
    suspend fun updatePreferredQuality(quality: String): Result<Unit>
    suspend fun updateSubtitleSettings(enabled: Boolean, language: String): Result<Unit>
    suspend fun updateAudioLanguage(language: String): Result<Unit>
    suspend fun updateAutoplayNextEpisode(enabled: Boolean): Result<Unit>
    suspend fun updateKeepScreenOn(enabled: Boolean): Result<Unit>
    suspend fun updateEpgSettings(timeFormat: String, timezone: String): Result<Unit>
    suspend fun updateParentalControl(enabled: Boolean, pin: String?, blockedCategories: Set<String>): Result<Unit>
    suspend fun updateViewMode(section: String, viewMode: String): Result<Unit>
    suspend fun clearPreferences(): Result<Unit>
}