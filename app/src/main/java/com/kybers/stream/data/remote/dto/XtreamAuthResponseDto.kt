package com.kybers.stream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XtreamAuthResponseDto(
    @SerialName("user_info")
    val userInfo: UserInfoDto? = null,
    
    @SerialName("server_info")
    val serverInfo: ServerInfoDto? = null,
    
    @SerialName("message")
    val message: String? = null,
    
    @SerialName("status")
    val status: String? = null
)

@Serializable
data class UserInfoDto(
    @SerialName("username")
    val username: String = "",
    
    @SerialName("password")
    val password: String = "",
    
    @SerialName("message")
    val message: String = "",
    
    @SerialName("auth")
    val auth: Int = 0,
    
    @SerialName("status")
    val status: String = "",
    
    @SerialName("exp_date")
    val expDate: String? = null,
    
    @SerialName("is_trial")
    val isTrial: String = "0",
    
    @SerialName("active_cons")
    val activeCons: String = "0",
    
    @SerialName("created_at")
    val createdAt: String = "",
    
    @SerialName("max_connections")
    val maxConnections: String = "1"
)

@Serializable
data class ServerInfoDto(
    @SerialName("url")
    val url: String = "",
    
    @SerialName("port")
    val port: String = "",
    
    @SerialName("https_port")
    val httpsPort: String = "",
    
    @SerialName("server_protocol")
    val serverProtocol: String = "",
    
    @SerialName("rtmp_port")
    val rtmpPort: String = "",
    
    @SerialName("timezone")
    val timezone: String = "",
    
    @SerialName("timestamp_now")
    val timestampNow: String = "",
    
    @SerialName("time_now")
    val timeNow: String = "",
    
    @SerialName("process")
    val process: String = ""
)