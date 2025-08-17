package com.kybers.stream.domain.model

data class Channel(
    val streamId: String,
    val name: String,
    val icon: String?,
    val categoryId: String,
    val epgChannelId: String?,
    val isAdult: Boolean = false,
    val tvArchive: Boolean = false,
    val tvArchiveDuration: Int = 0,
    val addedTimestamp: Long = 0L,
    val categoryName: String? = null
)