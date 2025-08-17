package com.kybers.stream.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ViewMode {
    LIST, GRID
}

@Composable
fun ViewModeToggle(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            val newMode = if (currentMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            onModeChange(newMode)
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (currentMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
            contentDescription = if (currentMode == ViewMode.LIST) "Cambiar a vista cuadrícula" else "Cambiar a vista lista",
            modifier = Modifier.size(24.dp)
        )
    }
}