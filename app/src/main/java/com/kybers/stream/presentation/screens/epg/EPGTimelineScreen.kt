package com.kybers.stream.presentation.screens.epg

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kybers.stream.domain.model.*
import com.kybers.stream.presentation.components.accessibility.AdaptiveText
import com.kybers.stream.presentation.components.loading.SkeletonComponents
import com.kybers.stream.presentation.viewmodels.EPGTimelineViewModel
import com.kybers.stream.presentation.viewmodels.ChannelEPG
import com.kybers.stream.presentation.viewmodels.Program
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EPGTimelineScreen(
    onNavigateBack: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onProgramSelected: (Channel, Program) -> Unit,
    viewModel: EPGTimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedTime by viewModel.selectedTime.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showChannelFilter by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadEPGData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Guía de programación electrónica con grid temporal" }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con controles de navegación
            EPGHeader(
                selectedDate = selectedDate,
                onNavigateBack = onNavigateBack,
                onShowDatePicker = { showDatePicker = true },
                onShowChannelFilter = { showChannelFilter = true },
                isTablet = isTablet
            )
            
            when {
                uiState.isLoading -> {
                    EPGLoadingState(
                        isTablet = isTablet
                    )
                }
                
                uiState.error != null -> {
                    EPGErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadEPGData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.epgData.isNotEmpty() -> {
                    // Timeline principal
                    EPGTimelineContent(
                        epgData = uiState.epgData,
                        categories = uiState.categories,
                        selectedDate = selectedDate,
                        selectedTime = selectedTime,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = category
                            viewModel.filterByCategory(category)
                        },
                        onTimeSelected = { time -> viewModel.selectTime(time) },
                        onChannelSelected = onChannelSelected,
                        onProgramSelected = onProgramSelected,
                        isTablet = isTablet,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                else -> {
                    EPGEmptyState(
                        onRetry = { viewModel.loadEPGData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Date picker overlay
        if (showDatePicker) {
            EPGDatePickerOverlay(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
        
        // Channel filter overlay
        if (showChannelFilter) {
            ChannelFilterOverlay(
                categories = uiState.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    viewModel.filterByCategory(category)
                    showChannelFilter = false
                },
                onDismiss = { showChannelFilter = false },
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun EPGHeader(
    selectedDate: LocalDateTime,
    onNavigateBack: () -> Unit,
    onShowDatePicker: () -> Unit,
    onShowChannelFilter: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                
                Column {
                    AdaptiveText(
                        text = "Guía de Programación",
                        style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    AdaptiveText(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro de canales
                OutlinedButton(
                    onClick = onShowChannelFilter,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (isTablet) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filtros")
                    }
                }
                
                // Selector de fecha
                OutlinedButton(
                    onClick = onShowDatePicker,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (isTablet) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fecha")
                    }
                }
            }
        }
    }
}

@Composable
fun EPGTimelineContent(
    epgData: List<ChannelEPG>,
    categories: List<Category>,
    selectedDate: LocalDateTime,
    selectedTime: LocalTime?,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onProgramSelected: (Channel, Program) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Filtros de categoría
        if (categories.isNotEmpty()) {
            CategoryFilters(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Timeline principal
        EPGTimelineGrid(
            epgData = epgData,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            onTimeSelected = onTimeSelected,
            onChannelSelected = onChannelSelected,
            onProgramSelected = onProgramSelected,
            isTablet = isTablet,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FilterChip(
                onClick = { onCategorySelected("") },
                label = { Text("Todos") },
                selected = selectedCategory.isEmpty(),
                leadingIcon = if (selectedCategory.isEmpty()) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
        
        items(categories.take(8)) { category ->
            FilterChip(
                onClick = { onCategorySelected(category.categoryName) },
                label = { Text(category.categoryName) },
                selected = selectedCategory == category.categoryName,
                leadingIcon = if (selectedCategory == category.categoryName) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun EPGTimelineGrid(
    epgData: List<ChannelEPG>,
    selectedDate: LocalDateTime,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onProgramSelected: (Channel, Program) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val timeSlots = generateTimeSlots()
    val channelListState = rememberLazyListState()
    val timelineScrollState = rememberScrollState()
    
    // Synchronize scroll position with current time
    LaunchedEffect(selectedTime) {
        selectedTime?.let { time ->
            val hourIndex = timeSlots.indexOfFirst { it.hour == time.hour }
            if (hourIndex >= 0) {
                timelineScrollState.animateScrollTo(hourIndex * 120) // 120dp per hour
            }
        }
    }
    
    Box(modifier = modifier) {
        Row {
            // Channel list (fixed left column)
            LazyColumn(
                state = channelListState,
                modifier = Modifier
                    .width(if (isTablet) 200.dp else 150.dp)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    // Header spacer for time slots
                    Spacer(modifier = Modifier.height(56.dp))
                }
                
                items(epgData) { channelEpg ->
                    ChannelHeader(
                        channel = channelEpg.channel,
                        onClick = { onChannelSelected(channelEpg.channel) },
                        isTablet = isTablet
                    )
                }
            }
            
            // Timeline grid (scrollable content)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(timelineScrollState)
            ) {
                // Time slots header
                TimelineHeader(
                    timeSlots = timeSlots,
                    selectedTime = selectedTime,
                    onTimeSelected = onTimeSelected
                )
                
                // Programs grid
                epgData.forEach { channelEpg ->
                    ProgramsRow(
                        channelEpg = channelEpg,
                        timeSlots = timeSlots,
                        selectedTime = selectedTime,
                        onProgramSelected = { program ->
                            onProgramSelected(channelEpg.channel, program)
                        },
                        isTablet = isTablet
                    )
                }
            }
        }
        
        // Current time indicator
        selectedTime?.let { time ->
            CurrentTimeIndicator(
                time = time,
                timeSlots = timeSlots,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(start = if (isTablet) 200.dp else 150.dp)
            )
        }
    }
}

@Composable
fun ChannelHeader(
    channel: Channel,
    onClick: () -> Unit,
    isTablet: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.icon,
                contentDescription = "Logo de ${channel.name}",
                modifier = Modifier
                    .size(if (isTablet) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            AdaptiveText(
                text = channel.name,
                style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TimelineHeader(
    timeSlots: List<LocalTime>,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        timeSlots.forEach { time ->
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clickable { onTimeSelected(time) }
                    .background(
                        if (selectedTime?.hour == time.hour) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            Color.Transparent
                    )
                    .border(
                        1.dp, 
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AdaptiveText(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedTime?.hour == time.hour) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTime?.hour == time.hour) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ProgramsRow(
    channelEpg: ChannelEPG,
    timeSlots: List<LocalTime>,
    selectedTime: LocalTime?,
    onProgramSelected: (Program) -> Unit,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        timeSlots.forEach { timeSlot ->
            val program = channelEpg.getProgramAt(timeSlot)
            
            ProgramCell(
                program = program,
                timeSlot = timeSlot,
                isSelected = selectedTime?.hour == timeSlot.hour,
                onClick = { program?.let { onProgramSelected(it) } },
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun ProgramCell(
    program: Program?,
    timeSlot: LocalTime,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTablet: Boolean
) {
    Card(
        onClick = if (program != null) onClick else { },
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .padding(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                program == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                program.isLive -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (program != null) 2.dp else 0.dp
        )
    ) {
        if (program != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AdaptiveText(
                    text = program.title,
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                    fontWeight = if (program.isLive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    AdaptiveText(
                        text = program.getFormattedTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (program.isLive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AdaptiveText(
                    text = "Sin programa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CurrentTimeIndicator(
    time: LocalTime,
    timeSlots: List<LocalTime>,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        label = "timeIndicatorAlpha"
    )
    
    Box(
        modifier = modifier
            .width(2.dp)
            .fillMaxHeight()
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.error)
    )
}

@Composable
fun EPGLoadingState(
    isTablet: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(6) {
                SkeletonComponents.SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Timeline skeleton
        Row {
            // Channel column
            Column(
                modifier = Modifier.width(if (isTablet) 200.dp else 150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(8) {
                    SkeletonComponents.SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Programs grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(8) { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(12) { col ->
                            SkeletonComponents.SkeletonBox(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EPGErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = "Error al cargar la guía",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
fun EPGEmptyState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdaptiveText(
            text = "No hay programación disponible",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdaptiveText(
            text = "La guía de programación no está disponible para esta fecha",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Actualizar")
        }
    }
}

@Composable
fun EPGDatePickerOverlay(
    selectedDate: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) { },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AdaptiveText(
                    text = "Seleccionar fecha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date picker content would go here
                // For now, showing placeholder
                AdaptiveText(
                    text = "Selector de fecha en desarrollo...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = { onDateSelected(selectedDate) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Seleccionar")
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelFilterOverlay(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isTablet: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(if (isTablet) 0.6f else 0.9f)
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) { },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdaptiveText(
                        text = "Filtrar canales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn {
                    item {
                        FilterChip(
                            onClick = { onCategorySelected("") },
                            label = { Text("Todos los canales") },
                            selected = selectedCategory.isEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                    
                    items(categories) { category ->
                        FilterChip(
                            onClick = { onCategorySelected(category.categoryName) },
                            label = { Text(category.categoryName) },
                            selected = selectedCategory == category.categoryName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper functions
private fun generateTimeSlots(): List<LocalTime> {
    return (0..23).map { hour ->
        LocalTime.of(hour, 0)
    }
}

// Extension function for ChannelEPG
private fun ChannelEPG.getProgramAt(time: LocalTime): Program? {
    return programs.find { program ->
        val programStart = program.startTime?.toLocalTime()
        val programEnd = program.endTime?.toLocalTime()
        
        programStart != null && programEnd != null &&
        (time.isAfter(programStart) || time == programStart) &&
        time.isBefore(programEnd)
    }
}

