package com.kybers.stream.domain.model

sealed class XtreamResult<out T> {
    data class Success<T>(val data: T) : XtreamResult<T>()
    data class Error(val message: String, val code: XtreamErrorCode = XtreamErrorCode.UNKNOWN) : XtreamResult<Nothing>()
    object Loading : XtreamResult<Nothing>()
}

enum class XtreamErrorCode {
    INVALID_CREDENTIALS,
    NETWORK_ERROR,
    SERVER_ERROR,
    CONNECTION_LIMIT_EXCEEDED,
    INVALID_URL,
    TIMEOUT,
    UNKNOWN
}