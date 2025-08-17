package com.kybers.stream.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.usecase.IsUserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            // Mostrar splash por al menos 1.5 segundos
            delay(1500)
            
            try {
                val isLoggedIn = isUserLoggedInUseCase().first()
                _uiState.value = if (isLoggedIn) {
                    SplashUiState.NavigateToHome
                } else {
                    SplashUiState.NavigateToLogin
                }
            } catch (e: Exception) {
                // En caso de error, navegar al login
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }
}