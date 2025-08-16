package com.kybers.stream.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EpgResponseDto(
    @SerializedName("epg_listings")
    val epgListings: List<EpgListingDto> = emptyList()
)

data class EpgListingDto(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("start")
    val start: String = "",
    
    @SerializedName("stop")
    val stop: String = "",
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("channel_id")
    val channelId: String = ""
)