package com.kybers.stream.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kybers.stream.domain.model.UserPreferences
import com.kybers.stream.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    
    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val PREFERRED_QUALITY = stringPreferencesKey("preferred_quality")
        private val SUBTITLES_ENABLED = booleanPreferencesKey("subtitles_enabled")
        private val SUBTITLE_LANGUAGE = stringPreferencesKey("subtitle_language")
        private val AUDIO_LANGUAGE = stringPreferencesKey("audio_language")
        private val AUTOPLAY_NEXT_EPISODE = booleanPreferencesKey("autoplay_next_episode")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val EPG_TIME_FORMAT = stringPreferencesKey("epg_time_format")
        private val EPG_TIMEZONE = stringPreferencesKey("epg_timezone")
        private val SHOW_PROGRESS_BAR = booleanPreferencesKey("show_progress_bar")
        private val PARENTAL_CONTROL_ENABLED = booleanPreferencesKey("parental_control_enabled")
        private val PARENTAL_PIN = stringPreferencesKey("parental_pin")
        private val BLOCKED_CATEGORIES = stringSetPreferencesKey("blocked_categories")
        private val VIEW_MODE_TV = stringPreferencesKey("view_mode_tv")
        private val VIEW_MODE_MOVIES = stringPreferencesKey("view_mode_movies")
        private val VIEW_MODE_SERIES = stringPreferencesKey("view_mode_series")
    }
    
    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            UserPreferences(
                theme = preferences[THEME] ?: "system",
                preferredQuality = preferences[PREFERRED_QUALITY] ?: "auto",
                subtitlesEnabled = preferences[SUBTITLES_ENABLED] ?: false,
                subtitleLanguage = preferences[SUBTITLE_LANGUAGE] ?: "es",
                audioLanguage = preferences[AUDIO_LANGUAGE] ?: "es",
                autoplayNextEpisode = preferences[AUTOPLAY_NEXT_EPISODE] ?: true,
                keepScreenOn = preferences[KEEP_SCREEN_ON] ?: true,
                epgTimeFormat = preferences[EPG_TIME_FORMAT] ?: "24",
                epgTimezone = preferences[EPG_TIMEZONE] ?: "auto",
                showProgressBar = preferences[SHOW_PROGRESS_BAR] ?: true,
                parentalControlEnabled = preferences[PARENTAL_CONTROL_ENABLED] ?: false,
                parentalPin = preferences[PARENTAL_PIN],
                blockedCategories = preferences[BLOCKED_CATEGORIES] ?: emptySet(),
                viewModeTv = preferences[VIEW_MODE_TV] ?: "list",
                viewModeMovies = preferences[VIEW_MODE_MOVIES] ?: "grid",
                viewModeSeries = preferences[VIEW_MODE_SERIES] ?: "grid"
            )
        }
    }
    
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[THEME] = preferences.theme
                prefs[PREFERRED_QUALITY] = preferences.preferredQuality
                prefs[SUBTITLES_ENABLED] = preferences.subtitlesEnabled
                prefs[SUBTITLE_LANGUAGE] = preferences.subtitleLanguage
                prefs[AUDIO_LANGUAGE] = preferences.audioLanguage
                prefs[AUTOPLAY_NEXT_EPISODE] = preferences.autoplayNextEpisode
                prefs[KEEP_SCREEN_ON] = preferences.keepScreenOn
                prefs[EPG_TIME_FORMAT] = preferences.epgTimeFormat
                prefs[EPG_TIMEZONE] = preferences.epgTimezone
                prefs[SHOW_PROGRESS_BAR] = preferences.showProgressBar
                prefs[PARENTAL_CONTROL_ENABLED] = preferences.parentalControlEnabled
                preferences.parentalPin?.let { prefs[PARENTAL_PIN] = it }
                prefs[BLOCKED_CATEGORIES] = preferences.blockedCategories
                prefs[VIEW_MODE_TV] = preferences.viewModeTv
                prefs[VIEW_MODE_MOVIES] = preferences.viewModeMovies
                prefs[VIEW_MODE_SERIES] = preferences.viewModeSeries
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTheme(theme: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[THEME] = theme
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePreferredQuality(quality: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PREFERRED_QUALITY] = quality
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSubtitleSettings(enabled: Boolean, language: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[SUBTITLES_ENABLED] = enabled
                preferences[SUBTITLE_LANGUAGE] = language
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAudioLanguage(language: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[AUDIO_LANGUAGE] = language
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAutoplayNextEpisode(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[AUTOPLAY_NEXT_EPISODE] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateKeepScreenOn(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[KEEP_SCREEN_ON] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateEpgSettings(timeFormat: String, timezone: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[EPG_TIME_FORMAT] = timeFormat
                preferences[EPG_TIMEZONE] = timezone
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateParentalControl(enabled: Boolean, pin: String?, blockedCategories: Set<String>): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PARENTAL_CONTROL_ENABLED] = enabled
                pin?.let { preferences[PARENTAL_PIN] = it }
                preferences[BLOCKED_CATEGORIES] = blockedCategories
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateViewMode(section: String, viewMode: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                when (section) {
                    "tv" -> preferences[VIEW_MODE_TV] = viewMode
                    "movies" -> preferences[VIEW_MODE_MOVIES] = viewMode
                    "series" -> preferences[VIEW_MODE_SERIES] = viewMode
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearPreferences(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}