package com.kybers.stream.presentation.components.empty

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tipos de empty states
 */
sealed class EmptyStateType {
    object NoContent : EmptyStateType()
    object NoConnection : EmptyStateType()
    object NoSearchResults : EmptyStateType()
    object NoFavorites : EmptyStateType()
    object ServerError : EmptyStateType()
    object Unauthorized : EmptyStateType()
    object Maintenance : EmptyStateType()
    object ParentalBlocked : EmptyStateType()
    data class Custom(
        val icon: ImageVector,
        val title: String,
        val description: String,
        val actionText: String? = null
    ) : EmptyStateType()
}

/**
 * Configuración de empty state
 */
data class EmptyStateConfig(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val actionText: String? = null,
    val secondaryActionText: String? = null,
    val showAnimation: Boolean = true
)

/**
 * Configuraciones predefinidas
 */
object EmptyStateConfigs {
    
    fun getConfig(type: EmptyStateType): EmptyStateConfig = when (type) {
        is EmptyStateType.NoContent -> EmptyStateConfig(
            icon = Icons.Default.PlayCircleOutline,
            title = "No hay contenido disponible",
            description = "Parece que no hay contenido para mostrar en este momento. Inténtalo más tarde o verifica tu conexión.",
            actionText = "Reintentar"
        )
        
        is EmptyStateType.NoConnection -> EmptyStateConfig(
            icon = Icons.Default.CloudOff,
            title = "Sin conexión a internet",
            description = "Verifica tu conexión Wi-Fi o datos móviles e inténtalo nuevamente.",
            actionText = "Reintentar",
            secondaryActionText = "Configurar red"
        )
        
        is EmptyStateType.NoSearchResults -> EmptyStateConfig(
            icon = Icons.Default.SearchOff,
            title = "Sin resultados",
            description = "No encontramos nada que coincida con tu búsqueda. Intenta con otros términos.",
            actionText = "Limpiar búsqueda"
        )
        
        is EmptyStateType.NoFavorites -> EmptyStateConfig(
            icon = Icons.Default.FavoriteBorder,
            title = "No tienes favoritos",
            description = "Añade contenido a favoritos para acceder rápidamente a tus canales y programas preferidos.",
            actionText = "Explorar contenido"
        )
        
        is EmptyStateType.ServerError -> EmptyStateConfig(
            icon = Icons.Default.Error,
            title = "Error del servidor",
            description = "Hubo un problema con el servidor. Nuestro equipo ya está trabajando para solucionarlo.",
            actionText = "Reintentar",
            secondaryActionText = "Reportar problema"
        )
        
        is EmptyStateType.Unauthorized -> EmptyStateConfig(
            icon = Icons.Default.Lock,
            title = "Acceso no autorizado",
            description = "Tus credenciales han expirado o no tienes permisos para ver este contenido.",
            actionText = "Iniciar sesión",
            secondaryActionText = "Contactar soporte"
        )
        
        is EmptyStateType.Maintenance -> EmptyStateConfig(
            icon = Icons.Default.Build,
            title = "Mantenimiento programado",
            description = "El servicio no está disponible temporalmente debido a mantenimiento. Volveremos pronto.",
            actionText = "Verificar estado"
        )
        
        is EmptyStateType.ParentalBlocked -> EmptyStateConfig(
            icon = Icons.Default.ChildCare,
            title = "Contenido bloqueado",
            description = "Este contenido está restringido por el control parental. Contacta al administrador.",
            actionText = "Desbloquear",
            secondaryActionText = "Ver otros"
        )
        
        is EmptyStateType.Custom -> EmptyStateConfig(
            icon = type.icon,
            title = type.title,
            description = type.description,
            actionText = type.actionText
        )
    }
}

/**
 * Componente principal de empty state
 */
@Composable
fun EmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    config: EmptyStateConfig = EmptyStateConfigs.getConfig(type),
    onPrimaryAction: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono con animación
        EmptyStateIcon(
            icon = config.icon,
            showAnimation = config.showAnimation
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Título
        Text(
            text = config.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Descripción
        Text(
            text = config.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Acciones
        EmptyStateActions(
            primaryText = config.actionText,
            secondaryText = config.secondaryActionText,
            onPrimaryAction = onPrimaryAction,
            onSecondaryAction = onSecondaryAction
        )
    }
}

/**
 * Icono animado para empty state
 */
@Composable
private fun EmptyStateIcon(
    icon: ImageVector,
    showAnimation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state_animation")
    
    val alpha by if (showAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon_alpha"
        )
    } else {
        remember { mutableStateOf(0.6f) }
    }
    
    val scale by if (showAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier
            .size(96.dp)
            .alpha(alpha)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    )
}

/**
 * Botones de acción para empty state
 */
@Composable
private fun EmptyStateActions(
    primaryText: String?,
    secondaryText: String?,
    onPrimaryAction: (() -> Unit)?,
    onSecondaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Acción primaria
        primaryText?.let { text ->
            Button(
                onClick = { onPrimaryAction?.invoke() },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(text = text)
            }
        }
        
        // Acción secundaria
        secondaryText?.let { text ->
            OutlinedButton(
                onClick = { onSecondaryAction?.invoke() },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(text = text)
            }
        }
    }
}

/**
 * Empty state compacto para usar en listas
 */
@Composable
fun CompactEmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    val config = EmptyStateConfigs.getConfig(type)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = config.title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = config.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        config.actionText?.let { actionText ->
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { onAction?.invoke() }
            ) {
                Text(text = actionText)
            }
        }
    }
}

/**
 * Empty state específico para búsquedas
 */
@Composable
fun SearchEmptyState(
    query: String,
    modifier: Modifier = Modifier,
    onClearSearch: (() -> Unit)? = null,
    onSearchSuggestion: ((String) -> Unit)? = null
) {
    val suggestions = remember {
        listOf("película", "acción", "comedia", "drama", "terror")
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "No se encontraron resultados",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No hay resultados para \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onClearSearch?.invoke() }
        ) {
            Text("Limpiar búsqueda")
        }
        
        if (onSearchSuggestion != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Prueba buscando:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onSearchSuggestion(suggestion) },
                        label = { Text(suggestion) }
                    )
                }
            }
        }
    }
}

/**
 * Empty state para contenido restringido
 */
@Composable
fun RestrictedContentEmptyState(
    reason: String,
    modifier: Modifier = Modifier,
    onUnlock: (() -> Unit)? = null,
    onContactSupport: (() -> Unit)? = null
) {
    EmptyState(
        type = EmptyStateType.Custom(
            icon = Icons.Default.Lock,
            title = "Contenido restringido",
            description = reason,
            actionText = if (onUnlock != null) "Desbloquear" else null
        ),
        modifier = modifier,
        onPrimaryAction = onUnlock,
        onSecondaryAction = onContactSupport
    )
}

/**
 * Empty state para errores de red específicos
 */
@Composable
fun NetworkErrorEmptyState(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onCheckConnection: (() -> Unit)? = null
) {
    val config = when {
        error.contains("timeout", ignoreCase = true) -> EmptyStateConfig(
            icon = Icons.Default.AccessTime,
            title = "Tiempo de espera agotado",
            description = "La conexión tardó demasiado. Verifica tu velocidad de internet.",
            actionText = "Reintentar"
        )
        error.contains("404", ignoreCase = true) -> EmptyStateConfig(
            icon = Icons.Default.QuestionMark,
            title = "Contenido no encontrado",
            description = "El contenido que buscas no está disponible en este momento.",
            actionText = "Volver atrás"
        )
        else -> EmptyStateConfigs.getConfig(EmptyStateType.NoConnection)
    }
    
    EmptyState(
        type = EmptyStateType.Custom(
            icon = config.icon,
            title = config.title,
            description = config.description,
            actionText = config.actionText
        ),
        modifier = modifier,
        config = config,
        onPrimaryAction = onRetry,
        onSecondaryAction = onCheckConnection
    )
}