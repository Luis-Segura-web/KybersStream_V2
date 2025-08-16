package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class XtreamAuthResponseDto(
    @SerializedName("user_info")
    val userInfo: UserInfoDto? = null,
    
    @SerializedName("server_info")
    val serverInfo: ServerInfoDto? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("status")
    val status: String? = null
)

data class UserInfoDto(
    @SerializedName("username")
    val username: String = "",
    
    @SerializedName("password")
    val password: String = "",
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("auth")
    val auth: Int = 0,
    
    @SerializedName("status")
    val status: String = "",
    
    @SerializedName("exp_date")
    val expDate: String? = null,
    
    @SerializedName("is_trial")
    val isTrial: String = "0",
    
    @SerializedName("active_cons")
    val activeCons: String = "0",
    
    @SerializedName("created_at")
    val createdAt: String = "",
    
    @SerializedName("max_connections")
    val maxConnections: String = "1",
    
    @SerializedName("allowed_output_formats")
    val allowedOutputFormats: List<String> = emptyList()
)

data class ServerInfoDto(
    @SerializedName("url")
    val url: String = "",
    
    @SerializedName("port")
    val port: String = "",
    
    @SerializedName("https_port")
    val httpsPort: String = "",
    
    @SerializedName("server_protocol")
    val serverProtocol: String = "",
    
    @SerializedName("rtmp_port")
    val rtmpPort: String = "",
    
    @SerializedName("timezone")
    val timezone: String = "",
    
    @SerializedName("timestamp_now")
    val timestampNow: Long = 0L,
    
    @SerializedName("time_now")
    val timeNow: String = "",
    
    @SerializedName("process")
    val process: Boolean = true
)