package com.kybers.stream.presentation.components.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Button accesible con tamaño mínimo de toque
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        content = content
    )
}

/**
 * IconButton accesible con descripción de contenido
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

/**
 * Texto adaptativo al escalado del sistema
 */
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight? = null,
    isHeading: Boolean = false
) {
    val context = LocalContext.current
    val fontScale = context.resources.configuration.fontScale
    
    // Ajustar el tamaño según la escala del sistema
    val adaptedStyle = when {
        fontScale >= 1.3f -> style.copy(
            fontSize = style.fontSize * 0.9f,
            lineHeight = style.lineHeight * 0.95f
        )
        fontScale >= 1.15f -> style.copy(
            fontSize = style.fontSize * 0.95f
        )
        else -> style
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = adaptedStyle.copy(fontWeight = fontWeight),
        maxLines = if (fontScale >= 1.3f) maxLines + 1 else maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Switch accesible con etiquetas claras
 */
@Composable
fun AccessibleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AdaptiveText(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            
            description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                AdaptiveText(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/**
 * Indicador de carga accesible
 */
@Composable
fun AccessibleLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Cargando contenido",
    isVisible: Boolean = true
) {
    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            AdaptiveText(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Utilidades de accesibilidad
 */
object AccessibilityUtils {
    
    @Composable
    fun isLargeFontScale(): Boolean {
        val context = LocalContext.current
        return context.resources.configuration.fontScale >= 1.3f
    }
    
    @Composable
    fun getAdaptedSpacing(): Dp {
        return if (isLargeFontScale()) 20.dp else 16.dp
    }
    
    @Composable
    fun getAdaptedIconSize(): Dp {
        return if (isLargeFontScale()) 28.dp else 24.dp
    }
}