package com.kybers.stream.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.*
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToParentalControls: () -> Unit,
    onLogout: () -> Unit,
    viewModel: EnhancedSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Pantalla de configuración con secciones organizadas" }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                EnhancedSettingsHeader(
                    currentUser = currentUser,
                    onNavigateBack = onNavigateBack,
                    onNavigateToProfile = onNavigateToProfile,
                    isTablet = isTablet
                )
            }
            
            // Cuenta y perfil
            item {
                EnhancedSettingsSection(
                    title = "Cuenta y Perfil",
                    icon = Icons.Default.Person,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsItem(
                        title = "Gestionar perfiles",
                        subtitle = "Administrar usuarios y perfiles familiares",
                        icon = Icons.Default.People,
                        onClick = onNavigateToProfile,
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsItem(
                        title = "Cambiar servidor",
                        subtitle = "Conectar a un servidor diferente",
                        icon = Icons.Default.Storage,
                        onClick = { viewModel.showServerDialog() },
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsItem(
                        title = "Información de la cuenta",
                        subtitle = currentUser?.server ?: "No definido",
                        icon = Icons.Default.Info,
                        onClick = { viewModel.showAccountInfo() },
                        isTablet = isTablet
                    )
                }
            }
            
            // Reproducción
            item {
                EnhancedSettingsSection(
                    title = "Reproducción",
                    icon = Icons.Default.PlayArrow,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsToggleItem(
                        title = "Reproducción automática",
                        subtitle = "Continuar reproduciendo el siguiente contenido",
                        icon = Icons.Default.PlayArrow,
                        checked = uiState.autoPlay,
                        onCheckedChange = { viewModel.updateAutoPlay(it) },
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsSliderItem(
                        title = "Calidad preferida",
                        subtitle = "Ajustar calidad de video según conexión",
                        icon = Icons.Default.HighQuality,
                        value = uiState.preferredQuality,
                        onValueChange = { viewModel.updatePreferredQuality(it) },
                        options = listOf("Auto", "720p", "1080p", "4K"),
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsToggleItem(
                        title = "Mostrar subtítulos",
                        subtitle = "Activar subtítulos por defecto",
                        icon = Icons.Default.Subtitles,
                        checked = uiState.showSubtitles,
                        onCheckedChange = { viewModel.updateShowSubtitles(it) },
                        isTablet = isTablet
                    )
                }
            }
            
            // Interfaz
            item {
                EnhancedSettingsSection(
                    title = "Interfaz",
                    icon = Icons.Default.Palette,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsSliderItem(
                        title = "Tema",
                        subtitle = "Personalizar la apariencia",
                        icon = Icons.Default.DarkMode,
                        value = when (uiState.themeMode) {
                            ThemeMode.LIGHT -> 0
                            ThemeMode.DARK -> 1
                            ThemeMode.SYSTEM -> 2
                        },
                        onValueChange = { 
                            val theme = when (it) {
                                0 -> ThemeMode.LIGHT
                                1 -> ThemeMode.DARK
                                else -> ThemeMode.SYSTEM
                            }
                            viewModel.updateThemeMode(theme)
                        },
                        options = listOf("Claro", "Oscuro", "Sistema"),
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsToggleItem(
                        title = "Color dinámico",
                        subtitle = "Usar colores del sistema (Android 12+)",
                        icon = Icons.Default.Colorize,
                        checked = uiState.useDynamicColor,
                        onCheckedChange = { viewModel.updateDynamicColor(it) },
                        isTablet = isTablet
                    )
                }
            }
            
            // Control parental
            item {
                EnhancedSettingsSection(
                    title = "Control Parental",
                    icon = Icons.Default.Security,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsToggleItem(
                        title = "Control parental activo",
                        subtitle = "Restringir contenido según edad",
                        icon = Icons.Default.ChildCare,
                        checked = uiState.parentalControlEnabled,
                        onCheckedChange = { 
                            if (it) {
                                onNavigateToParentalControls()
                            } else {
                                viewModel.disableParentalControl()
                            }
                        },
                        isTablet = isTablet
                    )
                    
                    if (uiState.parentalControlEnabled) {
                        EnhancedSettingsItem(
                            title = "Configurar restricciones",
                            subtitle = "Gestionar límites y PIN",
                            icon = Icons.Default.Lock,
                            onClick = onNavigateToParentalControls,
                            isTablet = isTablet
                        )
                    }
                }
            }
            
            // Almacenamiento
            item {
                EnhancedSettingsSection(
                    title = "Almacenamiento",
                    icon = Icons.Default.Storage,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsItem(
                        title = "Caché utilizado",
                        subtitle = uiState.cacheSize,
                        icon = Icons.Default.Folder,
                        onClick = { viewModel.showCacheDetails() },
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsItem(
                        title = "Limpiar caché",
                        subtitle = "Liberar espacio de almacenamiento",
                        icon = Icons.Default.CleaningServices,
                        onClick = { 
                            scope.launch {
                                viewModel.clearCache()
                            }
                        },
                        isTablet = isTablet
                    )
                }
            }
            
            // Información
            item {
                EnhancedSettingsSection(
                    title = "Información",
                    icon = Icons.Default.Info,
                    isTablet = isTablet
                ) {
                    EnhancedSettingsItem(
                        title = "Versión",
                        subtitle = uiState.appVersion,
                        icon = Icons.Default.AppShortcut,
                        onClick = { viewModel.showVersionInfo() },
                        isTablet = isTablet
                    )
                    
                    EnhancedSettingsItem(
                        title = "Soporte",
                        subtitle = "Obtener ayuda y reportar problemas",
                        icon = Icons.Default.Support,
                        onClick = { viewModel.showSupport() },
                        isTablet = isTablet
                    )
                }
            }
            
            // Cerrar sesión
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { viewModel.showLogoutDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AdaptiveText(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Dialogs
        if (uiState.showLogoutDialog) {
            EnhancedConfirmLogoutDialog(
                onConfirm = {
                    viewModel.hideLogoutDialog()
                    onLogout()
                },
                onDismiss = { viewModel.hideLogoutDialog() }
            )
        }
    }
}

@Composable
fun EnhancedSettingsHeader(
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    AdaptiveText(
                        text = "Configuración",
                        style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    currentUser?.let { user ->
                        AdaptiveText(
                            text = "Usuario: ${user.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Avatar/Profile button
            Surface(
                onClick = onNavigateToProfile,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (currentUser?.avatar != null) {
                    AsyncImage(
                        model = currentUser.avatar,
                        contentDescription = "Avatar del usuario",
                        modifier = Modifier
                            .size(if (isTablet) 56.dp else 48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isTablet) 56.dp else 48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AdaptiveText(
                            text = currentUser?.username?.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSettingsSection(
    title: String,
    icon: ImageVector,
    isTablet: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand_rotation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Section header
            Surface(
                onClick = { isExpanded = !isExpanded },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        AdaptiveText(
                            text = title,
                            style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Section content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun EnhancedSettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AdaptiveText(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                subtitle?.let { sub ->
                    AdaptiveText(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EnhancedSettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AdaptiveText(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                subtitle?.let { sub ->
                    AdaptiveText(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun EnhancedSettingsSliderItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    value: Int,
    onValueChange: (Int) -> Unit,
    options: List<String>? = null,
    valueRange: IntRange = 0..(options?.size?.minus(1) ?: 2),
    steps: Int = 0,
    valueFormatter: (Int) -> String = { options?.getOrNull(it) ?: it.toString() },
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AdaptiveText(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    subtitle?.let { sub ->
                        AdaptiveText(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                AdaptiveText(
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun EnhancedConfirmLogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AdaptiveText(
                text = "Cerrar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            AdaptiveText(
                text = "¿Estás seguro de que quieres cerrar sesión? Tendrás que volver a ingresar tus credenciales.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ViewModel y clases de datos
class EnhancedSettingsViewModel : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(EnhancedSettingsUiState())
    val uiState: StateFlow<EnhancedSettingsUiState> = _uiState
    
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser
    
    fun loadSettings() {
        // TODO: Implementar carga de configuración
    }
    
    fun updateAutoPlay(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoPlay = enabled)
    }
    
    fun updatePreferredQuality(quality: Int) {
        _uiState.value = _uiState.value.copy(preferredQuality = quality)
    }
    
    fun updateShowSubtitles(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSubtitles = show)
    }
    
    fun updateThemeMode(theme: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = theme)
    }
    
    fun updateDynamicColor(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useDynamicColor = enabled)
    }
    
    fun disableParentalControl() {
        _uiState.value = _uiState.value.copy(parentalControlEnabled = false)
    }
    
    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }
    
    fun hideLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }
    
    // Placeholder methods
    fun showServerDialog() { /* TODO */ }
    fun showAccountInfo() { /* TODO */ }
    fun showCacheDetails() { /* TODO */ }
    suspend fun clearCache() { _uiState.value = _uiState.value.copy(cacheSize = "0 MB") }
    fun showVersionInfo() { /* TODO */ }
    fun showSupport() { /* TODO */ }
}

data class EnhancedSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val autoPlay: Boolean = true,
    val preferredQuality: Int = 0,
    val showSubtitles: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val parentalControlEnabled: Boolean = false,
    val cacheSize: String = "125 MB",
    val appVersion: String = "2.0.0",
    val showLogoutDialog: Boolean = false
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class UserProfile(
    val id: String,
    val username: String,
    val server: String? = null,
    val avatar: String? = null
)