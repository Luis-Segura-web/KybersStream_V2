package com.kybers.stream.domain.model

enum class ContentType {
    LIVE_TV,
    VOD,
    SERIES,
    EPISODE
}

data class FavoriteItem(
    val contentId: String,
    val contentType: ContentType,
    val name: String,
    val imageUrl: String?,
    val categoryId: String?,
    val addedTimestamp: Long
)

data class PlaybackProgress(
    val contentId: String,
    val contentType: ContentType,
    val positionMs: Long,
    val durationMs: Long,
    val lastUpdated: Long
) {
    val progressPercentage: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    
    val isNearEnd: Boolean
        get() = progressPercentage > 0.95f
}

data class UserPreferences(
    val theme: String = "system", // "light", "dark", "system"
    val preferredQuality: String = "auto", // "auto", "high", "medium", "low"
    val subtitlesEnabled: Boolean = false,
    val subtitleLanguage: String = "es",
    val audioLanguage: String = "es",
    val autoplayNextEpisode: Boolean = true,
    val keepScreenOn: Boolean = true,
    val epgTimeFormat: String = "24", // "12", "24"
    val epgTimezone: String = "auto",
    val showProgressBar: Boolean = true,
    val parentalControlEnabled: Boolean = false,
    val parentalPin: String? = null,
    val blockedCategories: Set<String> = emptySet(),
    val viewModeTv: String = "list", // "list", "grid"
    val viewModeMovies: String = "grid",
    val viewModeSeries: String = "grid"
)