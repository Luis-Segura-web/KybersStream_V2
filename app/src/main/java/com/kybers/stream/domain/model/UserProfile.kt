package com.kybers.stream.domain.model

data class UserProfile(
    val id: String = "",
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val isActive: Boolean = false,
    val lastLoginTime: Long = 0L,
    val serverInfo: ServerInfo? = null
)

data class ServerInfo(
    val serverProtocol: String,
    val serverVersion: String,
    val timestampNow: String,
    val timeNow: String,
    val allowedOutputFormats: List<String> = emptyList()
)