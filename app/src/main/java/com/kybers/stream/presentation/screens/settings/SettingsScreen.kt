package com.kybers.stream.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kybers.stream.presentation.screens.settings.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle(
        initialValue = com.kybers.stream.domain.model.UserPreferences()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Apariencia
        SettingsSection(title = "Apariencia") {
            ThemeSettingItem(
                currentTheme = preferences.theme,
                onThemeChange = { viewModel.updateTheme(it) }
            )
        }
        
        // Reproducción
        SettingsSection(title = "Reproducción") {
            QualitySettingItem(
                currentQuality = preferences.preferredQuality,
                onQualityChange = { viewModel.updatePreferredQuality(it) }
            )
            
            SwitchSettingItem(
                title = "Subtítulos activados",
                subtitle = "Mostrar subtítulos por defecto",
                icon = Icons.Default.Subtitles,
                checked = preferences.subtitlesEnabled,
                onCheckedChange = { viewModel.updateSubtitlesEnabled(it) }
            )
            
            LanguageSettingItem(
                title = "Idioma de subtítulos",
                currentLanguage = preferences.subtitleLanguage,
                onLanguageChange = { viewModel.updateSubtitleLanguage(it) }
            )
            
            LanguageSettingItem(
                title = "Idioma de audio",
                currentLanguage = preferences.audioLanguage,
                onLanguageChange = { viewModel.updateAudioLanguage(it) }
            )
            
            SwitchSettingItem(
                title = "Autoplay siguiente episodio",
                subtitle = "Reproducir automáticamente el siguiente episodio",
                icon = Icons.Default.PlayArrow,
                checked = preferences.autoplayNextEpisode,
                onCheckedChange = { viewModel.updateAutoplayNextEpisode(it) }
            )
            
            SwitchSettingItem(
                title = "Mantener pantalla encendida",
                subtitle = "Durante la reproducción",
                icon = Icons.Default.ScreenLockPortrait,
                checked = preferences.keepScreenOn,
                onCheckedChange = { viewModel.updateKeepScreenOn(it) }
            )
        }
        
        // EPG
        SettingsSection(title = "Guía de programación") {
            TimeFormatSettingItem(
                currentFormat = preferences.epgTimeFormat,
                onFormatChange = { viewModel.updateEpgTimeFormat(it) }
            )
            
            SwitchSettingItem(
                title = "Mostrar barra de progreso",
                subtitle = "En programas en curso",
                icon = Icons.Default.LinearScale,
                checked = preferences.showProgressBar,
                onCheckedChange = { viewModel.updateShowProgressBar(it) }
            )
        }
        
        // Control parental
        SettingsSection(title = "Control parental") {
            ParentalControlSettingItem(
                enabled = preferences.parentalControlEnabled,
                onToggle = { viewModel.toggleParentalControl() },
                onShowSetPin = { viewModel.showSetPinDialog() }
            )
        }
        
        // Almacenamiento
        SettingsSection(title = "Almacenamiento") {
            ActionSettingItem(
                title = "Limpiar caché de imágenes",
                subtitle = "Liberar espacio en disco",
                icon = Icons.Default.CleaningServices,
                onClick = { viewModel.clearImageCache() }
            )
            
            ActionSettingItem(
                title = "Limpiar datos de reproducción",
                subtitle = "Borrar historial de reproducción",
                icon = Icons.Default.History,
                onClick = { viewModel.clearPlaybackData() }
            )
        }
        
        // Usuario
        SettingsSection(title = "Usuario") {
            ActionSettingItem(
                title = "Cambiar usuario",
                subtitle = "Gestionar perfiles de usuario",
                icon = Icons.Default.AccountCircle,
                onClick = { viewModel.changeUser() }
            )
            
            ActionSettingItem(
                title = "Cerrar sesión",
                subtitle = "Salir de la aplicación",
                icon = Icons.Default.Logout,
                onClick = { 
                    viewModel.logout()
                    onLogout()
                }
            )
        }
        
        // Información
        SettingsSection(title = "Información") {
            ActionSettingItem(
                title = "Acerca de",
                subtitle = "KybersStream v1.0.0",
                icon = Icons.Default.Info,
                onClick = { viewModel.showAboutDialog() }
            )
        }
    }
    
    // Diálogos
    if (uiState.showSetPinDialog) {
        SetPinDialog(
            onDismiss = { viewModel.dismissSetPinDialog() },
            onConfirm = { pin -> viewModel.setParentalPin(pin) }
        )
    }
    
    if (uiState.showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.dismissAboutDialog() }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            content()
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ActionSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingItem(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.bodyLarge
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when (currentTheme) {
                        "light" -> "Claro"
                        "dark" -> "Oscuro"
                        else -> "Sistema"
                    },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sistema") },
                        onClick = {
                            onThemeChange("system")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Claro") },
                        onClick = {
                            onThemeChange("light")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Oscuro") },
                        onClick = {
                            onThemeChange("dark")
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}