package com.kybers.stream.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kybers.stream.presentation.screens.splash.SplashScreen
import com.kybers.stream.presentation.screens.login.LoginScreen
import com.kybers.stream.presentation.screens.home.HomeScreen
import com.kybers.stream.presentation.screens.moviedetail.MovieDetailScreen
import com.kybers.stream.presentation.screens.seriesdetail.SeriesDetailScreen
import com.kybers.stream.presentation.screens.search.GlobalSearchScreen
import com.kybers.stream.presentation.screens.settings.EnhancedSettingsScreen
import com.kybers.stream.presentation.screens.movies.MoviesScreen
import com.kybers.stream.presentation.screens.series.SeriesScreen
import com.kybers.stream.presentation.screens.tv.TvScreen
import com.kybers.stream.presentation.screens.player.FullscreenPlayerScreen
import com.kybers.stream.presentation.screens.epg.EPGTimelineScreen
import com.kybers.stream.domain.model.ContentType

@Composable
fun KybersStreamNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToMovieDetail = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
                },
                onNavigateToSeriesDetail = { seriesId ->
                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                },
                onNavigateToMovies = {
                    navController.navigate(Screen.Movies.route)
                },
                onNavigateToSeries = {
                    navController.navigate(Screen.Series.route)
                },
                onNavigateToTV = {
                    navController.navigate(Screen.TV.route)
                }
            )
        }
        
        // Pantalla de detalles de película
        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: return@composable
            MovieDetailScreen(
                movieId = movieId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlay = { movieDetail ->
                    navController.navigate(
                        Screen.Player.createRoute(movieDetail.streamId, "movie")
                    )
                }
            )
        }
        
        // Pantalla de detalles de serie
        composable(
            route = Screen.SeriesDetail.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: return@composable
            SeriesDetailScreen(
                seriesId = seriesId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlayEpisode = { episode ->
                    navController.navigate(
                        Screen.Player.createRoute(episode.id, "episode")
                    )
                }
            )
        }
        
        // Pantalla de búsqueda
        composable(Screen.Search.route) {
            GlobalSearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMovieSelected = { movie ->
                    navController.navigate(Screen.MovieDetail.createRoute(movie.streamId))
                },
                onSeriesSelected = { series ->
                    navController.navigate(Screen.SeriesDetail.createRoute(series.seriesId))
                },
                onChannelSelected = { channel ->
                    navController.navigate(Screen.Player.createRoute(channel.streamId, "channel"))
                }
            )
        }
        
        // Pantalla de configuraciones
        composable(Screen.Settings.route) {
            EnhancedSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    // TODO: Implement profile management screen
                },
                onNavigateToParentalControls = {
                    // TODO: Implement parental controls screen
                }
            )
        }
        
        // Pantallas de listas completas
        composable(Screen.Movies.route) {
            MoviesScreen(
                onNavigateToMovieDetail = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
                }
            )
        }
        
        composable(Screen.Series.route) {
            SeriesScreen(
                onNavigateToSeriesDetail = { seriesId ->
                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                }
            )
        }
        
        composable(Screen.TV.route) {
            TvScreen()
        }
        
        // Reproductor de video
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.StringType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: return@composable
            val contentType = backStackEntry.arguments?.getString("contentType") ?: return@composable
            
            FullscreenPlayerScreen(
                contentId = contentId,
                contentType = when (contentType) {
                    "movie" -> ContentType.VOD
                    "series" -> ContentType.SERIES
                    "episode" -> ContentType.EPISODE
                    "channel" -> ContentType.LIVE_TV
                    else -> ContentType.VOD
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // EPG Timeline
        composable(
            route = Screen.EPG.route,
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: return@composable
            EPGTimelineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChannelSelected = { channel ->
                    navController.navigate(Screen.Player.createRoute(channel.streamId, "channel"))
                },
                onProgramSelected = { channel, program ->
                    navController.navigate(Screen.Player.createRoute(channel.streamId, "channel"))
                }
            )
        }
    }
}