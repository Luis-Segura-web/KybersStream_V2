package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    
    // Carruseles principales
    suspend fun getContinueWatchingCarousel(): Result<ContentCarousel>
    suspend fun getRecentlyAddedCarousel(contentType: ContentType? = null): Result<ContentCarousel>
    suspend fun getRecommendedCarousel(): Result<ContentCarousel>
    suspend fun getTrendingCarousel(): Result<ContentCarousel>
    suspend fun getFavoritesCarousel(): Result<ContentCarousel>
    
    // Carruseles por género
    suspend fun getGenreCarousel(genre: String, contentType: ContentType? = null): Result<ContentCarousel>
    suspend fun getPopularByGenre(genre: String): Result<ContentCarousel>
    
    // Carruseles específicos de contenido
    suspend fun getNewEpisodesCarousel(): Result<ContentCarousel>
    suspend fun getPopularMoviesCarousel(): Result<ContentCarousel>
    suspend fun getPopularSeriesCarousel(): Result<ContentCarousel>
    
    // Datos completos de descubrimiento
    suspend fun getDiscoveryData(forceRefresh: Boolean = false): Result<DiscoveryData>
    fun getDiscoveryDataFlow(): Flow<DiscoveryData>
    
    // Recomendaciones personalizadas
    suspend fun getPersonalizedRecommendations(limit: Int = 20): Result<List<ContentItem>>
    suspend fun getRecommendationScore(contentId: String): Result<RecommendationScore>
    
    // Actualización de datos
    suspend fun refreshDiscoveryData(): Result<Unit>
    suspend fun refreshCarousel(carouselType: CarouselType): Result<ContentCarousel>
    
    // Gestión de cache
    suspend fun clearDiscoveryCache(): Result<Unit>
    suspend fun getLastUpdateTime(): Result<java.time.LocalDateTime?>
}