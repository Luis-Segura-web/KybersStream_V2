package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChannelDto(
    @SerializedName("num")
    val num: Int = 0,
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("stream_type")
    val streamType: String = "",
    
    @SerializedName("stream_id")
    val streamId: String = "",
    
    @SerializedName("stream_icon")
    val streamIcon: String? = null,
    
    @SerializedName("epg_channel_id")
    val epgChannelId: String? = null,
    
    @SerializedName("added")
    val added: String = "",
    
    @SerializedName("is_adult")
    val isAdult: String = "0",
    
    @SerializedName("category_id")
    val categoryId: String = "",
    
    @SerializedName("custom_sid")
    val customSid: String? = null,
    
    @SerializedName("tv_archive")
    val tvArchive: Int = 0,
    
    @SerializedName("direct_source")
    val directSource: String? = null,
    
    @SerializedName("tv_archive_duration")
    val tvArchiveDuration: Int = 0
)