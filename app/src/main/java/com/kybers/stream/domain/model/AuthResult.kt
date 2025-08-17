package com.kybers.stream.domain.model

sealed class AuthResult {
    data class Success(val userProfile: UserProfile) : AuthResult()
    data class Error(val message: String, val code: AuthErrorCode = AuthErrorCode.UNKNOWN) : AuthResult()
    object Loading : AuthResult()
}

enum class AuthErrorCode {
    INVALID_CREDENTIALS,
    NETWORK_ERROR,
    SERVER_ERROR,
    INVALID_URL,
    TIMEOUT,
    ACCOUNT_EXPIRED,
    UNKNOWN
}

data class LoginRequest(
    val server: String,
    val username: String,
    val password: String
)