package com.kybers.stream.domain.model

import java.time.LocalDateTime

data class ContentCarousel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val items: List<ContentItem>,
    val type: CarouselType,
    val priority: Int = 0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    val isEmpty: Boolean
        get() = items.isEmpty()
}

enum class CarouselType {
    CONTINUE_WATCHING,
    RECENTLY_ADDED,
    RECOMMENDED,
    TRENDING,
    FAVORITES,
    BY_GENRE,
    NEW_EPISODES,
    POPULAR_MOVIES,
    POPULAR_SERIES
}

sealed class ContentItem {
    abstract val id: String
    abstract val title: String
    abstract val posterUrl: String?
    abstract val backdropUrl: String?
    abstract val contentType: ContentType
    abstract val year: String?
    abstract val rating: String?
    abstract val genre: String?
    abstract val quality: String?
    
    data class MovieItem(
        override val id: String,
        override val title: String,
        override val posterUrl: String?,
        override val backdropUrl: String?,
        override val year: String?,
        override val rating: String?,
        override val genre: String?,
        override val quality: String?,
        val duration: String? = null,
        val plot: String? = null
    ) : ContentItem() {
        override val contentType: ContentType = ContentType.VOD
    }
    
    data class SeriesItem(
        override val id: String,
        override val title: String,
        override val posterUrl: String?,
        override val backdropUrl: String?,
        override val year: String?,
        override val rating: String?,
        override val genre: String?,
        override val quality: String?,
        val totalSeasons: Int = 0,
        val totalEpisodes: Int = 0,
        val status: String? = null,
        val plot: String? = null
    ) : ContentItem() {
        override val contentType: ContentType = ContentType.SERIES
        
        val seasonsEpisodesDisplay: String
            get() = when {
                totalSeasons > 0 && totalEpisodes > 0 -> "$totalSeasons temp. • $totalEpisodes ep."
                totalSeasons > 0 -> "$totalSeasons temporadas"
                else -> "Serie"
            }
    }
    
    data class EpisodeItem(
        override val id: String,
        override val title: String,
        override val posterUrl: String?,
        override val backdropUrl: String?,
        override val year: String?,
        override val rating: String?,
        override val genre: String?,
        override val quality: String?,
        val seriesName: String,
        val seasonNumber: Int,
        val episodeNumber: Int,
        val runtime: String? = null,
        val airDate: String? = null
    ) : ContentItem() {
        override val contentType: ContentType = ContentType.EPISODE
        
        val displayTitle: String
            get() = "S${seasonNumber}E${episodeNumber} - $title"
        
        val seriesDisplayTitle: String
            get() = "$seriesName • S${seasonNumber}E${episodeNumber}"
    }
    
    data class ContinueWatchingItem(
        override val id: String,
        override val title: String,
        override val posterUrl: String?,
        override val backdropUrl: String?,
        override val year: String?,
        override val rating: String?,
        override val genre: String?,
        override val quality: String?,
        override val contentType: ContentType,
        val progress: PlaybackProgress,
        val seriesName: String? = null, // For episodes
        val seasonNumber: Int? = null,
        val episodeNumber: Int? = null
    ) : ContentItem() {
        val progressPercentage: Float
            get() = progress.progressPercentage
        
        val displayTitle: String
            get() = when (contentType) {
                ContentType.EPISODE -> "$seriesName • S${seasonNumber}E${episodeNumber}"
                else -> title
            }
        
        val isNearEnd: Boolean
            get() = progress.isNearEnd
    }
}

data class RecommendationScore(
    val contentId: String,
    val score: Float,
    val reasons: List<RecommendationReason>
)

enum class RecommendationReason {
    SIMILAR_GENRE,
    SIMILAR_RATING,
    SAME_ACTOR,
    SAME_DIRECTOR,
    POPULAR_IN_CATEGORY,
    TRENDING,
    NEW_RELEASE,
    FREQUENTLY_WATCHED_TOGETHER,
    USER_GENRE_PREFERENCE,
    HIGH_RATED
}

data class DiscoverySection(
    val id: String,
    val title: String,
    val carousels: List<ContentCarousel>,
    val isExpanded: Boolean = true
)

data class DiscoveryData(
    val sections: List<DiscoverySection>,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasContent: Boolean
        get() = sections.any { section -> section.carousels.any { !it.isEmpty } }
}