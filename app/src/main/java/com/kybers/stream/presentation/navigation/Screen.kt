package com.kybers.stream.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    
    // Pantallas de detalle con argumentos
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: String) = "movie_detail/$movieId"
    }
    
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: String) = "series_detail/$seriesId"
    }
    
    // Pantallas adicionales
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Player : Screen("player/{contentId}/{contentType}") {
        fun createRoute(contentId: String, contentType: String) = "player/$contentId/$contentType"
    }
    
    // Pantallas de listas completas
    object Movies : Screen("movies")
    object Series : Screen("series")
    object TV : Screen("tv")
    object EPG : Screen("epg/{channelId}") {
        fun createRoute(channelId: String) = "epg/$channelId"
    }
}