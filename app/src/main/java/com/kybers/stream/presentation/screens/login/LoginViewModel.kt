package com.kybers.stream.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.AuthResult
import com.kybers.stream.domain.model.LoginRequest
import com.kybers.stream.domain.model.UserProfile
import com.kybers.stream.domain.usecase.GetSavedProfilesUseCase
import com.kybers.stream.domain.usecase.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val getSavedProfilesUseCase: GetSavedProfilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadSavedProfiles()
    }

    private fun loadSavedProfiles() {
        viewModelScope.launch {
            getSavedProfilesUseCase().collect { profiles ->
                _uiState.value = _uiState.value.copy(savedProfiles = profiles)
            }
        }
    }

    fun onServerChanged(server: String) {
        _uiState.value = _uiState.value.copy(
            server = server,
            serverError = null,
            errorMessage = null
        )
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onProfileSelected(profile: UserProfile) {
        _uiState.value = _uiState.value.copy(
            selectedProfile = profile,
            server = profile.server,
            username = profile.username,
            password = profile.password,
            serverError = null,
            usernameError = null,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onLoginClicked() {
        if (!validateInput()) return

        val currentState = _uiState.value
        val loginRequest = LoginRequest(
            server = currentState.server.trim(),
            username = currentState.username.trim(),
            password = currentState.password.trim()
        )

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = loginUserUseCase(loginRequest)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        errorMessage = null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isLoginSuccessful = false
                    )
                }
                is AuthResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        var hasErrors = false

        val serverError = when {
            currentState.server.isBlank() -> {
                hasErrors = true
                "El servidor es obligatorio"
            }
            !isValidUrl(currentState.server) -> {
                hasErrors = true
                "URL del servidor no válida"
            }
            else -> null
        }

        val usernameError = if (currentState.username.isBlank()) {
            hasErrors = true
            "El usuario es obligatorio"
        } else null

        val passwordError = if (currentState.password.isBlank()) {
            hasErrors = true
            "La contraseña es obligatoria"
        } else null

        _uiState.value = currentState.copy(
            serverError = serverError,
            usernameError = usernameError,
            passwordError = passwordError
        )

        return !hasErrors
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    fun onRememberUserChanged(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberUser = remember)
    }
}