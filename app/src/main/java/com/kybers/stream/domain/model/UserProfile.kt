package com.kybers.stream.domain.model

data class UserProfile(
    val id: String = "",
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val isActive: Boolean = false,
    val lastLoginTime: Long = 0L,
    val userInfo: UserInfo? = null,
    val serverInfo: ServerInfo? = null
)

data class UserInfo(
    val username: String,
    val password: String,
    val message: String,
    val auth: Int,
    val status: String,
    val expDate: String?,
    val isTrial: String,
    val activeCons: String,
    val createdAt: String,
    val maxConnections: String,
    val allowedOutputFormats: List<String> = emptyList()
)

data class ServerInfo(
    val url: String,
    val port: String,
    val httpsPort: String,
    val serverProtocol: String,
    val rtmpPort: String,
    val timezone: String,
    val timestampNow: Long,
    val timeNow: String,
    val process: Boolean = true
)