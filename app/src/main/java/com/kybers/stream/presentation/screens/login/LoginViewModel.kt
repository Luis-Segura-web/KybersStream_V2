package com.kybers.stream.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.AuthResult
import com.kybers.stream.domain.model.AuthErrorCode
import com.kybers.stream.domain.model.LoginRequest
import com.kybers.stream.domain.model.UserProfile
import com.kybers.stream.domain.usecase.GetSavedProfilesUseCase
import com.kybers.stream.domain.usecase.LoginUserUseCase
import com.kybers.stream.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val getSavedProfilesUseCase: GetSavedProfilesUseCase,
    private val syncManager: SyncManager
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
                    val userProfile = result.userProfile
                    
                    // Verificar si necesita sincronización
                    val needsSync = loginUserUseCase.needsSync(userProfile)
                    
                    if (needsSync) {
                        // Mostrar diálogo de sincronización y proceder
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            authenticatedUser = userProfile,
                            syncUiState = SyncUiState(
                                isVisible = true,
                                isLoading = true,
                                currentStep = SyncStep.STARTING
                            )
                        )
                        performDataSync()
                    } else {
                        // Caché válido, ir directamente a home
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    }
                }
                is AuthResult.Error -> {
                    val errorMessage = when (result.code) {
                        AuthErrorCode.ACCOUNT_EXPIRED -> "Tu suscripción ha expirado. Por favor contacta a tu proveedor de servicios para renovarla."
                        AuthErrorCode.INVALID_CREDENTIALS -> "Credenciales incorrectas. Verifica tu usuario y contraseña."
                        AuthErrorCode.NETWORK_ERROR -> "Sin conexión a internet. Verifica tu conexión e inténtalo nuevamente."
                        AuthErrorCode.SERVER_ERROR -> "Error del servidor. Inténtalo más tarde."
                        AuthErrorCode.TIMEOUT -> "Tiempo de espera agotado. Verifica tu conexión."
                        else -> result.message
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        errorCode = result.code,
                        isLoginSuccessful = false,
                        showExpiredAccountDialog = result.code == AuthErrorCode.ACCOUNT_EXPIRED
                    )
                }
                is AuthResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private suspend fun performDataSync() {
        try {
            // Realizar la sincronización con callbacks de progreso
            val syncResult = syncManager.performInitialSyncWithCallback { step, progress ->
                val syncStep = when {
                    step.contains("categorías", ignoreCase = true) -> SyncStep.CATEGORIES
                    step.contains("canales", ignoreCase = true) -> SyncStep.CHANNELS  
                    step.contains("películas", ignoreCase = true) -> SyncStep.MOVIES
                    step.contains("series", ignoreCase = true) -> SyncStep.SERIES
                    step.contains("Finalizando", ignoreCase = true) -> SyncStep.FINISHING
                    step.contains("completada", ignoreCase = true) -> SyncStep.COMPLETED
                    else -> SyncStep.STARTING
                }
                
                updateSyncProgress(syncStep, progress)
            }
            
            if (syncResult.isSuccess) {
                updateSyncProgress(SyncStep.COMPLETED, 1.0f)
                delay(1500) // Mostrar completado un momento
                
                // Ocultar diálogo y navegar a home
                _uiState.value = _uiState.value.copy(
                    syncUiState = SyncUiState(isVisible = false),
                    isLoginSuccessful = true
                )
            } else {
                updateSyncProgress(SyncStep.ERROR, 0f)
                _uiState.value = _uiState.value.copy(
                    syncUiState = _uiState.value.syncUiState.copy(
                        errorMessage = "Error al sincronizar datos: ${syncResult.exceptionOrNull()?.message}"
                    )
                )
            }
            
        } catch (e: Exception) {
            updateSyncProgress(SyncStep.ERROR, 0f)
            _uiState.value = _uiState.value.copy(
                syncUiState = _uiState.value.syncUiState.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            )
        }
    }

    private fun updateSyncProgress(step: SyncStep, progress: Float) {
        _uiState.value = _uiState.value.copy(
            syncUiState = _uiState.value.syncUiState.copy(
                currentStep = step,
                progress = progress,
                isLoading = step != SyncStep.COMPLETED && step != SyncStep.ERROR
            )
        )
    }

    fun onSyncRetry() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                syncUiState = _uiState.value.syncUiState.copy(
                    isLoading = true,
                    currentStep = SyncStep.STARTING,
                    errorMessage = null
                )
            )
            performDataSync()
        }
    }

    fun onSyncCancel() {
        _uiState.value = _uiState.value.copy(
            syncUiState = SyncUiState(isVisible = false),
            errorMessage = "Sincronización cancelada"
        )
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
    
    fun dismissExpiredAccountDialog() {
        _uiState.value = _uiState.value.copy(showExpiredAccountDialog = false)
    }
    
    fun onContactProvider() {
        // TODO: Implement contact provider functionality
        // For now, just dismiss the dialog
        dismissExpiredAccountDialog()
    }
    
    fun onLogoutFromExpiredDialog() {
        dismissExpiredAccountDialog()
        // Clear form data
        _uiState.value = LoginUiState()
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(
            isLoginSuccessful = false,
            syncUiState = SyncUiState()
        )
    }

    fun onRememberUserChanged(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberUser = remember)
    }
}