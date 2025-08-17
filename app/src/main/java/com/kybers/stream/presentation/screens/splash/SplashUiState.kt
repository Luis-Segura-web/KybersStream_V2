package com.kybers.stream.presentation.screens.splash

sealed class SplashUiState {
    data class Loading(
        val isMigrating: Boolean = false,
        val isValidatingSession: Boolean = false,
        val progress: Float? = null
    ) : SplashUiState()
    
    object NavigateToLogin : SplashUiState()
    object NavigateToHome : SplashUiState()
}