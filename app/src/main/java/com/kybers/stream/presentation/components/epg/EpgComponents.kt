package com.kybers.stream.presentation.components.epg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kybers.stream.domain.model.EpgProgram
import com.kybers.stream.domain.model.ChannelEpg
import java.time.ZoneId

@Composable
fun EpgNowNextDisplay(
    channelEpg: ChannelEpg,
    use24HourFormat: Boolean = true,
    timeZone: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Programa actual
        channelEpg.currentProgram?.let { currentProgram ->
            EpgProgramItem(
                program = currentProgram,
                label = "Ahora",
                isLive = true,
                use24HourFormat = use24HourFormat,
                timeZone = timeZone,
                showProgress = true
            )
        }
        
        // Programa siguiente
        channelEpg.nextProgram?.let { nextProgram ->
            Spacer(modifier = Modifier.height(4.dp))
            EpgProgramItem(
                program = nextProgram,
                label = "Siguiente",
                isLive = false,
                use24HourFormat = use24HourFormat,
                timeZone = timeZone,
                showProgress = false
            )
        }
        
        // Si no hay información de EPG
        if (channelEpg.currentProgram == null && channelEpg.nextProgram == null) {
            Text(
                text = "Sin información de programación",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EpgProgramItem(
    program: EpgProgram,
    label: String,
    isLive: Boolean,
    use24HourFormat: Boolean = true,
    timeZone: ZoneId = ZoneId.systemDefault(),
    showProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = program.getFormattedTime(use24HourFormat, timeZone),
                style = MaterialTheme.typography.bodySmall,
                color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
            )
            
            if (isLive) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = program.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        // Barra de progreso para programa actual
        if (showProgress && isLive && program.progressPercentage > 0) {
            LinearProgressIndicator(
                progress = { program.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun EpgLoadingPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Simular programa actual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(6.dp)
                    )
            )
            
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(top = 4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp)
                )
        )
        
        // Simular programa siguiente
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(12.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp)
                )
        )
        
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
                .padding(top = 4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(7.dp)
                )
        )
    }
}

@Composable
fun EpgErrorDisplay(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error al cargar EPG",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Reintentar",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}