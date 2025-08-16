package com.kybers.stream.domain.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EpgListing(
    val id: String,
    val title: String,
    val start: String,
    val stop: String,
    val description: String? = null,
    val channelId: String
)

data class EpgResponse(
    val epgListings: List<EpgListing>
)

// Modelos avanzados para ETAPA 6
data class EpgProgram(
    val id: String,
    val streamId: String, // Channel stream_id from ETAPA 3
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val category: String? = null,
    val rating: String? = null,
    val language: String? = null,
    val isLive: Boolean = false
) {
    val durationMinutes: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
    
    val progressPercentage: Float
        get() {
            val now = LocalDateTime.now()
            return when {
                now.isBefore(startTime) -> 0f
                now.isAfter(endTime) -> 1f
                else -> {
                    val total = java.time.Duration.between(startTime, endTime).toMinutes()
                    val elapsed = java.time.Duration.between(startTime, now).toMinutes()
                    if (total > 0) elapsed.toFloat() / total.toFloat() else 0f
                }
            }
        }
    
    val isCurrentlyAiring: Boolean
        get() {
            val now = LocalDateTime.now()
            return !now.isBefore(startTime) && !now.isAfter(endTime)
        }
    
    fun getFormattedTime(use24HourFormat: Boolean = true, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val zonedStart = startTime.atZone(zoneId)
        val zonedEnd = endTime.atZone(zoneId)
        
        val formatter = if (use24HourFormat) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofPattern("h:mm a")
        }
        
        return "${zonedStart.format(formatter)} - ${zonedEnd.format(formatter)}"
    }
}

data class ChannelEpg(
    val streamId: String,
    val channelName: String,
    val currentProgram: EpgProgram?,
    val nextProgram: EpgProgram?,
    val todayPrograms: List<EpgProgram> = emptyList()
) {
    val nowNextSummary: String
        get() = buildString {
            currentProgram?.let { current ->
                append("Ahora: ${current.title}")
                nextProgram?.let { next ->
                    append(" | Siguiente: ${next.title}")
                }
            } ?: append("Sin información de programación")
        }
}

data class EpgTimeSlot(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val programs: List<EpgProgram>
) {
    val duration: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
}

enum class EpgSource {
    XMLTV,
    SHORT_EPG,
    MANUAL
}

data class EpgData(
    val source: EpgSource,
    val lastUpdated: LocalDateTime,
    val channels: List<ChannelEpg>,
    val totalPrograms: Int
)