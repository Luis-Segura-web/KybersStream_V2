package com.kybers.stream.presentation.components.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Modifier para efecto de shimmer/skeleton
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE3E3E3),
                Color(0xFFF5F5F5),
                Color(0xFFE3E3E3),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

/**
 * Skeleton para elementos de texto
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp,
    width: androidx.compose.ui.unit.Dp? = null
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .shimmerEffect()
    )
}

/**
 * Skeleton para títulos (texto más grande)
 */
@Composable
fun SkeletonTitle(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null
) {
    SkeletonText(
        modifier = modifier,
        height = 24.dp,
        width = width
    )
}

/**
 * Skeleton para imágenes rectangulares
 */
@Composable
fun SkeletonImage(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 120.dp,
    height: androidx.compose.ui.unit.Dp = 80.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

/**
 * Skeleton para imágenes circulares (avatares, logos)
 */
@Composable
fun SkeletonCircleImage(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

/**
 * Skeleton para pósters de películas/series
 */
@Composable
fun SkeletonPoster(
    modifier: Modifier = Modifier
) {
    SkeletonImage(
        modifier = modifier,
        width = 160.dp,
        height = 240.dp,
        cornerRadius = 12.dp
    )
}

/**
 * Skeleton para tarjetas de contenido
 */
@Composable
fun SkeletonContentCard(
    modifier: Modifier = Modifier,
    showPoster: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showPoster) {
                SkeletonImage(
                    width = 80.dp,
                    height = 120.dp
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonTitle(width = 200.dp)
                SkeletonText(width = 120.dp)
                SkeletonText(width = 160.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonText(width = 80.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Skeleton para elemento de lista de canales
 */
@Composable
fun SkeletonChannelItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo del canal
        SkeletonCircleImage(size = 48.dp)
        
        // Información del canal
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SkeletonText(width = 140.dp, height = 18.dp)
            SkeletonText(width = 200.dp, height = 14.dp)
            SkeletonText(width = 180.dp, height = 14.dp)
        }
        
        // Botón de favorito
        SkeletonCircleImage(size = 24.dp)
    }
}

/**
 * Skeleton para grid de películas/series
 */
@Composable
fun SkeletonGridItem(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonPoster()
        SkeletonText(height = 16.dp)
        SkeletonText(width = 100.dp, height = 12.dp)
    }
}

/**
 * Skeleton para reproductor de video
 */
@Composable
fun SkeletonVideoPlayer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect(),
        contentAlignment = Alignment.Center
    ) {
        // Botón de play en el centro
        SkeletonCircleImage(size = 64.dp)
    }
}

/**
 * Skeleton para carrusel/banner
 */
@Composable
fun SkeletonCarouselItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .shimmerEffect()
    ) {
        // Overlay con información
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonText(
                width = 250.dp,
                height = 24.dp
            )
            SkeletonText(
                width = 180.dp,
                height = 16.dp
            )
        }
    }
}

/**
 * Skeleton para elementos de búsqueda
 */
@Composable
fun SkeletonSearchItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonImage(
            width = 60.dp,
            height = 60.dp,
            cornerRadius = 8.dp
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonText(width = 180.dp, height = 16.dp)
            SkeletonText(width = 120.dp, height = 14.dp)
            SkeletonText(width = 90.dp, height = 12.dp)
        }
    }
}

/**
 * Skeleton para estadísticas o métricas
 */
@Composable
fun SkeletonStatsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonText(width = 80.dp, height = 32.dp)
            SkeletonText(width = 100.dp, height = 16.dp)
        }
    }
}

/**
 * Lista de skeletons para diferentes pantallas
 */
@Composable
fun SkeletonChannelList(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(itemCount) {
            SkeletonChannelItem()
        }
    }
}

@Composable
fun SkeletonMovieGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    itemCount: Int = 8
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(itemCount) {
            SkeletonGridItem()
        }
    }
}

@Composable
fun SkeletonSearchResults(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemCount) {
            SkeletonSearchItem()
        }
    }
}

/**
 * Presets de skeleton para pantallas completas
 */
@Composable
fun SkeletonHomeScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Banner/Carrusel
        item {
            SkeletonCarouselItem()
        }
        
        // Sección de continuar viendo
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonTitle(width = 150.dp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        SkeletonGridItem()
                    }
                }
            }
        }
        
        // Sección de recomendados
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonTitle(width = 120.dp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        SkeletonGridItem()
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonTvScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Reproductor
        SkeletonVideoPlayer(
            modifier = Modifier.padding(16.dp)
        )
        
        // Lista de canales
        SkeletonChannelList(
            modifier = Modifier.weight(1f)
        )
    }
}