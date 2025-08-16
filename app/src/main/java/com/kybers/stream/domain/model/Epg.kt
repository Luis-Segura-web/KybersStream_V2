package com.kybers.stream.domain.model

data class EpgListing(
    val id: String,
    val title: String,
    val start: String,
    val stop: String,
    val description: String? = null,
    val channelId: String
)

data class EpgResponse(
    val epgListings: List<EpgListing>
)