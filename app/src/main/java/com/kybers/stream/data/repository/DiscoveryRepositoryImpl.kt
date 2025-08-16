package com.kybers.stream.data.repository

import com.kybers.stream.data.local.dao.FavoriteDao
import com.kybers.stream.data.local.dao.PlaybackProgressDao
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val playbackProgressDao: PlaybackProgressDao
) : DiscoveryRepository {

    private var cachedDiscoveryData: DiscoveryData? = null
    private var lastUpdateTime: LocalDateTime? = null
    private val cacheValidityHours = 1L

    override suspend fun getContinueWatchingCarousel(): Result<ContentCarousel> {
        return try {
            // Get first emission from flow
            var progressList = emptyList<com.kybers.stream.data.local.entity.PlaybackProgressEntity>()
            playbackProgressDao.getAllProgress().collect { list ->
                progressList = list
                    .filter { progressEntity ->
                        val progressPercentage = if (progressEntity.durationMs > 0) {
                            progressEntity.positionMs.toFloat() / progressEntity.durationMs.toFloat()
                        } else 0f
                        progressPercentage < 0.95f // Not near end
                    }
                    .sortedByDescending { it.lastUpdated }
                    .take(10)
                return@collect // Exit after first emission
            }
            
            val items = progressList.map { progressEntity ->
                ContentItem.ContinueWatchingItem(
                    id = progressEntity.contentId,
                    title = "Contenido ${progressEntity.contentId}", // TODO: Get real title from content repository
                    posterUrl = null, // TODO: Get real poster URL
                    backdropUrl = null,
                    year = null,
                    rating = null,
                    genre = null,
                    quality = null,
                    contentType = progressEntity.contentType,
                    progress = PlaybackProgress(
                        contentId = progressEntity.contentId,
                        contentType = progressEntity.contentType,
                        positionMs = progressEntity.positionMs,
                        durationMs = progressEntity.durationMs,
                        lastUpdated = progressEntity.lastUpdated
                    )
                )
            }
            
            val carousel = ContentCarousel(
                id = "continue_watching",
                title = "Continuar viendo",
                subtitle = if (items.isNotEmpty()) "${items.size} elementos" else null,
                items = items,
                type = CarouselType.CONTINUE_WATCHING,
                priority = 1
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentlyAddedCarousel(contentType: ContentType?): Result<ContentCarousel> {
        return try {
            // Mock implementation - in real app, this would come from API
            val items = generateMockMovies(10)
            
            val carousel = ContentCarousel(
                id = "recently_added",
                title = "Agregados recientemente",
                subtitle = "Nuevo contenido cada semana",
                items = items,
                type = CarouselType.RECENTLY_ADDED,
                priority = 2
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedCarousel(): Result<ContentCarousel> {
        return try {
            // Get user's favorite genres from favorites
            var favorites = emptyList<com.kybers.stream.data.local.entity.FavoriteEntity>()
            favoriteDao.getAllFavorites().collect { list ->
                favorites = list
                return@collect // Exit after first emission
            }
            val userGenres = extractGenresFromFavorites(favorites)
            
            // Generate recommendations based on user preferences
            val items = generateRecommendationsBasedOnPreferences(userGenres)
            
            val carousel = ContentCarousel(
                id = "recommended",
                title = "Recomendado para ti",
                subtitle = "Basado en tu historial",
                items = items,
                type = CarouselType.RECOMMENDED,
                priority = 3
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingCarousel(): Result<ContentCarousel> {
        return try {
            val items = generateMockTrendingContent()
            
            val carousel = ContentCarousel(
                id = "trending",
                title = "Tendencias",
                subtitle = "Lo más popular ahora",
                items = items,
                type = CarouselType.TRENDING,
                priority = 4
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavoritesCarousel(): Result<ContentCarousel> {
        return try {
            var allFavorites = emptyList<com.kybers.stream.data.local.entity.FavoriteEntity>()
            favoriteDao.getAllFavorites().collect { list ->
                allFavorites = list
                return@collect // Exit after first emission
            }
            
            val favorites = allFavorites
                .sortedByDescending { it.addedTimestamp }
                .take(10)
            
            val items = favorites.map { favoriteEntity ->
                when (favoriteEntity.contentType) {
                    ContentType.VOD -> ContentItem.MovieItem(
                        id = favoriteEntity.contentId,
                        title = favoriteEntity.name,
                        posterUrl = favoriteEntity.imageUrl,
                        backdropUrl = null,
                        year = null,
                        rating = null,
                        genre = null,
                        quality = null
                    )
                    ContentType.SERIES -> ContentItem.SeriesItem(
                        id = favoriteEntity.contentId,
                        title = favoriteEntity.name,
                        posterUrl = favoriteEntity.imageUrl,
                        backdropUrl = null,
                        year = null,
                        rating = null,
                        genre = null,
                        quality = null
                    )
                    else -> ContentItem.MovieItem(
                        id = favoriteEntity.contentId,
                        title = favoriteEntity.name,
                        posterUrl = favoriteEntity.imageUrl,
                        backdropUrl = null,
                        year = null,
                        rating = null,
                        genre = null,
                        quality = null
                    )
                }
            }
            
            val carousel = ContentCarousel(
                id = "favorites",
                title = "Tus favoritos",
                subtitle = if (items.isNotEmpty()) "${items.size} elementos favoritos" else null,
                items = items,
                type = CarouselType.FAVORITES,
                priority = 5
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGenreCarousel(genre: String, contentType: ContentType?): Result<ContentCarousel> {
        return try {
            val items = generateMockContentByGenre(genre, contentType)
            
            val carousel = ContentCarousel(
                id = "genre_$genre",
                title = genre,
                subtitle = "Lo mejor del género",
                items = items,
                type = CarouselType.BY_GENRE,
                priority = 6
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPopularByGenre(genre: String): Result<ContentCarousel> {
        return getGenreCarousel(genre)
    }

    override suspend fun getNewEpisodesCarousel(): Result<ContentCarousel> {
        return try {
            val items = generateMockNewEpisodes()
            
            val carousel = ContentCarousel(
                id = "new_episodes",
                title = "Nuevos episodios",
                subtitle = "Últimos capítulos de tus series",
                items = items,
                type = CarouselType.NEW_EPISODES,
                priority = 7
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPopularMoviesCarousel(): Result<ContentCarousel> {
        return try {
            val items = generateMockMovies(12)
            
            val carousel = ContentCarousel(
                id = "popular_movies",
                title = "Películas populares",
                subtitle = "Las más vistas de la semana",
                items = items,
                type = CarouselType.POPULAR_MOVIES,
                priority = 8
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPopularSeriesCarousel(): Result<ContentCarousel> {
        return try {
            val items = generateMockSeries(12)
            
            val carousel = ContentCarousel(
                id = "popular_series",
                title = "Series populares",
                subtitle = "Las más seguidas del momento",
                items = items,
                type = CarouselType.POPULAR_SERIES,
                priority = 9
            )
            
            Result.success(carousel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscoveryData(forceRefresh: Boolean): Result<DiscoveryData> {
        return try {
            val now = LocalDateTime.now()
            val shouldRefresh = forceRefresh || 
                               cachedDiscoveryData == null || 
                               lastUpdateTime?.isBefore(now.minusHours(cacheValidityHours)) == true

            if (shouldRefresh) {
                refreshDiscoveryData()
            }

            Result.success(cachedDiscoveryData ?: DiscoveryData(emptyList()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDiscoveryDataFlow(): Flow<DiscoveryData> = flow {
        while (true) {
            val result = getDiscoveryData()
            result.getOrNull()?.let { emit(it) }
            kotlinx.coroutines.delay(30000) // Update every 30 seconds
        }
    }

    override suspend fun getPersonalizedRecommendations(limit: Int): Result<List<ContentItem>> {
        return try {
            var favorites = emptyList<com.kybers.stream.data.local.entity.FavoriteEntity>()
            favoriteDao.getAllFavorites().collect { list ->
                favorites = list
                return@collect // Exit after first emission
            }
            val userGenres = extractGenresFromFavorites(favorites)
            val recommendations = generateRecommendationsBasedOnPreferences(userGenres).take(limit)
            
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendationScore(contentId: String): Result<RecommendationScore> {
        return try {
            // Mock implementation
            val score = RecommendationScore(
                contentId = contentId,
                score = Random.nextFloat() * 10,
                reasons = listOf(
                    RecommendationReason.SIMILAR_GENRE,
                    RecommendationReason.HIGH_RATED
                )
            )
            Result.success(score)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshDiscoveryData(): Result<Unit> {
        return try {
            val carousels = mutableListOf<ContentCarousel>()
            
            // Add non-empty carousels
            getContinueWatchingCarousel().getOrNull()?.let { if (!it.isEmpty) carousels.add(it) }
            getRecentlyAddedCarousel().getOrNull()?.let { carousels.add(it) }
            getRecommendedCarousel().getOrNull()?.let { carousels.add(it) }
            getTrendingCarousel().getOrNull()?.let { carousels.add(it) }
            getFavoritesCarousel().getOrNull()?.let { if (!it.isEmpty) carousels.add(it) }
            getPopularMoviesCarousel().getOrNull()?.let { carousels.add(it) }
            getPopularSeriesCarousel().getOrNull()?.let { carousels.add(it) }
            getNewEpisodesCarousel().getOrNull()?.let { carousels.add(it) }
            
            val mainSection = DiscoverySection(
                id = "main",
                title = "Descubrir",
                carousels = carousels.sortedBy { it.priority }
            )
            
            cachedDiscoveryData = DiscoveryData(
                sections = listOf(mainSection),
                lastUpdated = LocalDateTime.now()
            )
            lastUpdateTime = LocalDateTime.now()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCarousel(carouselType: CarouselType): Result<ContentCarousel> {
        return when (carouselType) {
            CarouselType.CONTINUE_WATCHING -> getContinueWatchingCarousel()
            CarouselType.RECENTLY_ADDED -> getRecentlyAddedCarousel()
            CarouselType.RECOMMENDED -> getRecommendedCarousel()
            CarouselType.TRENDING -> getTrendingCarousel()
            CarouselType.FAVORITES -> getFavoritesCarousel()
            CarouselType.NEW_EPISODES -> getNewEpisodesCarousel()
            CarouselType.POPULAR_MOVIES -> getPopularMoviesCarousel()
            CarouselType.POPULAR_SERIES -> getPopularSeriesCarousel()
            CarouselType.BY_GENRE -> Result.failure(IllegalArgumentException("Genre carousels require genre parameter"))
        }
    }

    override suspend fun clearDiscoveryCache(): Result<Unit> {
        return try {
            cachedDiscoveryData = null
            lastUpdateTime = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastUpdateTime(): Result<LocalDateTime?> {
        return Result.success(lastUpdateTime)
    }

    // Helper methods for mock data generation
    private fun extractGenresFromFavorites(favorites: List<com.kybers.stream.data.local.entity.FavoriteEntity>): List<String> {
        // Mock implementation - in real app, would analyze favorite genres
        return listOf("Acción", "Drama", "Comedia", "Thriller", "Ciencia Ficción")
    }

    private fun generateRecommendationsBasedOnPreferences(genres: List<String>): List<ContentItem> {
        return generateMockMovies(8) + generateMockSeries(4)
    }

    private fun generateMockMovies(count: Int): List<ContentItem.MovieItem> {
        val titles = listOf(
            "Aventura Épica", "Drama Histórico", "Comedia Romántica", "Thriller Psicológico",
            "Acción Explosiva", "Ciencia Ficción", "Horror Sobrenatural", "Biografía Inspiradora"
        )
        val genres = listOf("Acción", "Drama", "Comedia", "Thriller", "Ciencia Ficción", "Terror", "Romance")
        val years = listOf("2023", "2022", "2021", "2020")
        val qualities = listOf("4K UHD", "HD", "1080p", "720p")
        
        return (1..count).map { i ->
            ContentItem.MovieItem(
                id = "movie_$i",
                title = "${titles.random()} $i",
                posterUrl = "https://example.com/poster$i.jpg",
                backdropUrl = "https://example.com/backdrop$i.jpg",
                year = years.random(),
                rating = String.format("%.1f", Random.nextFloat() * 10),
                genre = genres.random(),
                quality = qualities.random(),
                duration = "${Random.nextInt(90, 180)} min"
            )
        }
    }

    private fun generateMockSeries(count: Int): List<ContentItem.SeriesItem> {
        val titles = listOf(
            "Serie de Fantasía", "Drama Médico", "Comedia Familiar", "Thriller Criminal",
            "Acción Militar", "Ciencia Ficción", "Horror Psicológico", "Documental Histórico"
        )
        val genres = listOf("Fantasía", "Drama", "Comedia", "Thriller", "Acción", "Terror", "Documental")
        val years = listOf("2023", "2022", "2021", "2020")
        
        return (1..count).map { i ->
            ContentItem.SeriesItem(
                id = "series_$i",
                title = "${titles.random()} $i",
                posterUrl = "https://example.com/series_poster$i.jpg",
                backdropUrl = "https://example.com/series_backdrop$i.jpg",
                year = years.random(),
                rating = String.format("%.1f", Random.nextFloat() * 10),
                genre = genres.random(),
                quality = "HD",
                totalSeasons = Random.nextInt(1, 6),
                totalEpisodes = Random.nextInt(10, 100),
                status = if (Random.nextBoolean()) "Continuing" else "Ended"
            )
        }
    }

    private fun generateMockTrendingContent(): List<ContentItem> {
        return generateMockMovies(6) + generateMockSeries(6)
    }

    private fun generateMockNewEpisodes(): List<ContentItem.EpisodeItem> {
        val seriesNames = listOf(
            "La Casa de Dragones", "Stranger Things", "The Mandalorian", "Breaking Bad",
            "Game of Thrones", "The Office", "Friends", "Lost"
        )
        
        return (1..8).map { i ->
            ContentItem.EpisodeItem(
                id = "episode_$i",
                title = "Episodio ${Random.nextInt(1, 13)}",
                posterUrl = "https://example.com/episode$i.jpg",
                backdropUrl = null,
                year = "2023",
                rating = String.format("%.1f", Random.nextFloat() * 10),
                genre = "Drama",
                quality = "HD",
                seriesName = seriesNames.random(),
                seasonNumber = Random.nextInt(1, 5),
                episodeNumber = Random.nextInt(1, 13),
                runtime = "${Random.nextInt(40, 60)} min",
                airDate = "2023-12-${Random.nextInt(1, 31)}"
            )
        }
    }

    private fun generateMockContentByGenre(genre: String, contentType: ContentType?): List<ContentItem> {
        return when (contentType) {
            ContentType.VOD -> generateMockMovies(8).map { it.copy(genre = genre) }
            ContentType.SERIES -> generateMockSeries(8).map { it.copy(genre = genre) }
            else -> (generateMockMovies(4).map { it.copy(genre = genre) } + 
                    generateMockSeries(4).map { it.copy(genre = genre) })
        }
    }
}