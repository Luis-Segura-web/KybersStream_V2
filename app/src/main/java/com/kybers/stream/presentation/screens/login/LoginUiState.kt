package com.kybers.stream.presentation.screens.login

import com.kybers.stream.domain.model.UserProfile

data class LoginUiState(
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val rememberUser: Boolean = false,
    val isLoading: Boolean = false,
    val isValidatingCredentials: Boolean = false,
    val isConnectingToServer: Boolean = false,
    val errorMessage: String? = null,
    val savedProfiles: List<UserProfile> = emptyList(),
    val selectedProfile: UserProfile? = null,
    val isLoginSuccessful: Boolean = false,
    val serverError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val syncUiState: SyncUiState = SyncUiState(),
    val authenticatedUser: UserProfile? = null
) {
    val isFormValid: Boolean
        get() = server.isNotBlank() && 
                username.isNotBlank() && 
                password.isNotBlank() &&
                serverError == null &&
                usernameError == null &&
                passwordError == null
}