package com.kybers.stream.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.UserPreferences
import com.kybers.stream.domain.usecase.LogoutUserUseCase
import com.kybers.stream.domain.usecase.preferences.GetUserPreferencesUseCase
import com.kybers.stream.domain.usecase.preferences.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val showSetPinDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val userPreferences: Flow<UserPreferences> = getUserPreferencesUseCase()

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updateTheme(theme)
        }
    }

    fun updatePreferredQuality(quality: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updatePreferredQuality(quality)
        }
    }

    fun updateSubtitlesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.firstOrNull()?.let { prefs ->
                updateUserPreferencesUseCase.updateSubtitleSettings(enabled, prefs.subtitleLanguage)
            }
        }
    }

    fun updateSubtitleLanguage(language: String) {
        viewModelScope.launch {
            userPreferences.firstOrNull()?.let { prefs ->
                updateUserPreferencesUseCase.updateSubtitleSettings(prefs.subtitlesEnabled, language)
            }
        }
    }

    fun updateAudioLanguage(language: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updateAudioLanguage(language)
        }
    }

    fun updateAutoplayNextEpisode(enabled: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updateAutoplayNextEpisode(enabled)
        }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updateKeepScreenOn(enabled)
        }
    }

    fun updateEpgTimeFormat(format: String) {
        viewModelScope.launch {
            userPreferences.firstOrNull()?.let { prefs ->
                updateUserPreferencesUseCase.updateEpgSettings(format, prefs.epgTimezone)
            }
        }
    }

    fun updateShowProgressBar(enabled: Boolean) {
        viewModelScope.launch {
            // We need to get current preferences and update the UserPreferences
            userPreferences.firstOrNull()?.let { prefs ->
                updateUserPreferencesUseCase(prefs.copy(showProgressBar = enabled))
            }
        }
    }

    fun toggleParentalControl() {
        viewModelScope.launch {
            userPreferences.firstOrNull()?.let { prefs ->
                if (prefs.parentalControlEnabled) {
                    updateUserPreferencesUseCase.updateParentalControl(false, null, emptySet())
                } else {
                    showSetPinDialog()
                }
            }
        }
    }

    fun setParentalPin(pin: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.updateParentalControl(true, pin, emptySet())
            dismissSetPinDialog()
        }
    }

    fun showSetPinDialog() {
        _uiState.value = _uiState.value.copy(showSetPinDialog = true)
    }

    fun dismissSetPinDialog() {
        _uiState.value = _uiState.value.copy(showSetPinDialog = false)
    }

    fun showAboutDialog() {
        _uiState.value = _uiState.value.copy(showAboutDialog = true)
    }

    fun dismissAboutDialog() {
        _uiState.value = _uiState.value.copy(showAboutDialog = false)
    }

    fun clearImageCache() {
        viewModelScope.launch {
            // TODO: Implement image cache clearing
        }
    }

    fun clearPlaybackData() {
        viewModelScope.launch {
            // TODO: Implement playback data clearing
        }
    }

    fun changeUser() {
        viewModelScope.launch {
            // TODO: Navigate to login/profile selection
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUserUseCase()
        }
    }
}